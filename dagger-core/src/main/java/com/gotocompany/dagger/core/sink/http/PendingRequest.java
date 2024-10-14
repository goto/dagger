import com.gotocompany.depot.config.HttpSinkConfig;
import com.gotocompany.depot.Sink;
import com.gotocompany.depot.SinkResponse;
import com.gotocompany.depot.message.Message;
import com.gotocompany.depot.error.ErrorInfo;
import com.gotocompany.depot.exception.SinkException;
import com.gotocompany.depot.http.HttpSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Tag;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.UUID;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.concurrent.ThreadLocalRandom;

public class PendingRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PendingRequest.class);

    private final HttpSinkConfig config;
    private final CompletableFuture<SinkResponse> future;
    private final List<Message> messages;
    private final AtomicInteger retryCount;
    private final long creationTime;
    private final String requestId;
    private final Map<String, Object> metadata;
    private final ReentrantLock lock;
    private final Condition retryCondition;
    private final Queue<RetryStrategy> retryStrategies;
    private final ConcurrentHashMap<String, Object> context;
    private final AtomicReference<RequestState> state;
    private final ScheduledExecutorService scheduler;
    private final HttpSink httpSink;
    private final Consumer<PendingRequest> onCompletionCallback;
    private final Predicate<Throwable> retryPredicate;
    private final CircuitBreaker circuitBreaker;
    private final MeterRegistry meterRegistry;
    private final Timer executionTimer;
    private final Counter retryCounter;
    private final Counter successCounter;
    private final Counter failureCounter;
    
    private enum RequestState {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
    
    private interface RetryStrategy {
        long getNextRetryDelay(int retryCount);
    }
    
    private class ExponentialBackoffStrategy implements RetryStrategy {
        @Override
        public long getNextRetryDelay(int retryCount) {
            return (long) (Math.pow(2, retryCount) * config.getBackoffMultiplier() * (1 + ThreadLocalRandom.current().nextDouble() * config.getJitterFactor()));
        }
    }
    
    private class LinearBackoffStrategy implements RetryStrategy {
        @Override
        public long getNextRetryDelay(int retryCount) {
            return retryCount * config.getBackoffMultiplier() * (1 + ThreadLocalRandom.current().nextDouble() * config.getJitterFactor());
        }
    }
    
    private class CircuitBreaker {
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicReference<CircuitState> state = new AtomicReference<>(CircuitState.CLOSED);
        private final long resetTimeout;
        private volatile long lastFailureTime;
        
        private enum CircuitState {
            CLOSED, OPEN, HALF_OPEN
        }
        
        public CircuitBreaker(long resetTimeout) {
            this.resetTimeout = resetTimeout;
        }
        
        public boolean allowRequest() {
            CircuitState currentState = state.get();
            if (currentState == CircuitState.CLOSED) {
                return true;
            } else if (currentState == CircuitState.OPEN) {
                if (System.currentTimeMillis() - lastFailureTime > resetTimeout) {
                    if (state.compareAndSet(CircuitState.OPEN, CircuitState.HALF_OPEN)) {
                        LOGGER.info("Circuit breaker transitioning to HALF_OPEN state for request: {}", requestId);
                        return true;
                    }
                }
                return false;
            } else {
                return true;
            }
        }
        
        public void recordSuccess() {
            failureCount.set(0);
            state.set(CircuitState.CLOSED);
            LOGGER.debug("Circuit breaker recorded success for request: {}", requestId);
        }
        
        public void recordFailure() {
            failureCount.incrementAndGet();
            lastFailureTime = System.currentTimeMillis();
            if (failureCount.get() > config.getMaxRetries()) {
                state.set(CircuitState.OPEN);
                LOGGER.warn("Circuit breaker opened for request: {}", requestId);
            }
        }
    }
    
    private PendingRequest(Builder builder) {
        this.config = builder.config;
        this.future = new CompletableFuture<>();
        this.messages = builder.messages;
        this.retryCount = new AtomicInteger(0);
        this.creationTime = System.currentTimeMillis();
        this.requestId = UUID.randomUUID().toString();
        this.metadata = new ConcurrentHashMap<>(builder.metadata);
        this.lock = new ReentrantLock();
        this.retryCondition = lock.newCondition();
        this.retryStrategies = new LinkedList<>(builder.retryStrategies);
        this.context = new ConcurrentHashMap<>();
        this.state = new AtomicReference<>(RequestState.PENDING);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.httpSink = builder.httpSink;
        this.onCompletionCallback = builder.onCompletionCallback;
        this.retryPredicate = builder.retryPredicate;
        this.circuitBreaker = new CircuitBreaker(config.getCircuitBreakerResetTimeout());
        this.meterRegistry = builder.meterRegistry;
        
        List<Tag> tags = List.of(Tag.of("requestId", requestId));
        this.executionTimer = Timer.builder("http.request.execution")
                .tags(tags)
                .register(meterRegistry);
        this.retryCounter = Counter.builder("http.request.retries")
                .tags(tags)
                .register(meterRegistry);
        this.successCounter = Counter.builder("http.request.success")
                .tags(tags)
                .register(meterRegistry);
        this.failureCounter = Counter.builder("http.request.failure")
                .tags(tags)
                .register(meterRegistry);
        
        LOGGER.info("Created PendingRequest with ID: {}", requestId);
    }
    
    public CompletableFuture<SinkResponse> getFuture() {
        return future;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }
    
    public long getElapsedTime() {
        return System.currentTimeMillis() - creationTime;
    }
    
    public void execute() {
        if (!state.compareAndSet(RequestState.PENDING, RequestState.IN_PROGRESS)) {
            LOGGER.debug("Request {} is already in progress or completed", requestId);
            return;
        }
        
        if (!circuitBreaker.allowRequest()) {
            LOGGER.warn("Circuit breaker is open for request: {}", requestId);
            handleFailure(new RuntimeException("Circuit breaker is open"));
            return;
        }
        
        LOGGER.info("Executing request: {}", requestId);
        executionTimer.record(() -> {
            try {
                SinkResponse response = httpSink.pushToSink(messages);
                handleSuccess(response);
            } catch (SinkException e) {
                handleFailure(e);
            }
        });
    }
    
    private void handleSuccess(SinkResponse result) {
        LOGGER.info("Request {} completed successfully", requestId);
        circuitBreaker.recordSuccess();
        state.set(RequestState.COMPLETED);
        future.complete(result);
        onCompletionCallback.accept(this);
        successCounter.increment();
    }
    
    private void handleFailure(Throwable throwable) {
        LOGGER.warn("Request {} failed: {}", requestId, throwable.getMessage());
        circuitBreaker.recordFailure();
        failureCounter.increment();
        if (retryCount.incrementAndGet() <= config.getMaxRetries() && retryPredicate.test(throwable)) {
            LOGGER.info("Scheduling retry {} for request {}", retryCount.get(), requestId);
            retryCounter.increment();
            scheduleRetry();
        } else {
            LOGGER.error("Request {} failed permanently after {} retries", requestId, retryCount.get() - 1);
            state.set(RequestState.FAILED);
            future.completeExceptionally(throwable);
            onCompletionCallback.accept(this);
        }
    }
    
    private void scheduleRetry() {
        lock.lock();
        try {
            RetryStrategy strategy = retryStrategies.poll();
            if (strategy == null) {
                strategy = new ExponentialBackoffStrategy();
            }
            long delay = strategy.getNextRetryDelay(retryCount.get());
            LOGGER.debug("Scheduling retry for request {} with delay {}ms", requestId, delay);
            scheduler.schedule(this::execute, delay, TimeUnit.MILLISECONDS);
            retryCondition.await(delay, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.warn("Retry scheduling interrupted for request {}", requestId);
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }
    
    public static class Builder {
        private HttpSinkConfig config;
        private List<Message> messages;
        private Map<String, Object> metadata = new HashMap<>();
        private List<RetryStrategy> retryStrategies = new ArrayList<>();
        private HttpSink httpSink;
        private Consumer<PendingRequest> onCompletionCallback = request -> {};
        private Predicate<Throwable> retryPredicate = throwable -> true;
        private MeterRegistry meterRegistry;
        
        public Builder withConfig(HttpSinkConfig config) {
            this.config = config;
            return this;
        }
        
        public Builder withMessages(List<Message> messages) {
            this.messages = messages;
            return this;
        }
        
        public Builder withMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public Builder withRetryStrategy(RetryStrategy strategy) {
            this.retryStrategies.add(strategy);
            return this;
        }
        
        public Builder withHttpSink(HttpSink httpSink) {
            this.httpSink = httpSink;
            return this;
        }
        
        public Builder withOnCompletionCallback(Consumer<PendingRequest> callback) {
            this.onCompletionCallback = callback;
            return this;
        }
        
        public Builder withRetryPredicate(Predicate<Throwable> predicate) {
            this.retryPredicate = predicate;
            return this;
        }
        
        public Builder withMeterRegistry(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
            return this;
        }
        
        public PendingRequest build() {
            if (config == null || messages == null || httpSink == null || meterRegistry == null) {
                throw new IllegalStateException("Config, Messages, HttpSink, and MeterRegistry must be set");
            }
            return new PendingRequest(this);
        }
    }
    
    public static PendingRequest create(HttpSinkConfig config, List<Message> messages, HttpSink httpSink, MeterRegistry meterRegistry) {
        return new Builder()
                .withConfig(config)
                .withMessages(messages)
                .withHttpSink(httpSink)
                .withMeterRegistry(meterRegistry)
                .build();
    }
    
    public void cancel() {
        if (state.compareAndSet(RequestState.PENDING, RequestState.FAILED) ||
            state.compareAndSet(RequestState.IN_PROGRESS, RequestState.FAILED)) {
            LOGGER.info("Cancelling request: {}", requestId);
            future.cancel(true);
            onCompletionCallback.accept(this);
        }
    }
    
    public boolean isCancelled() {
        return future.isCancelled();
    }
    
    public boolean isDone() {
        return future.isDone();
    }
    
    public RequestState getState() {
        return state.get();
    }
    
    public void addContext(String key, Object value) {
        context.put(key, value);
        LOGGER.debug("Added context for request {}: {} = {}", requestId, key, value);
    }
    
    public Optional<Object> getContext(String key) {
        return Optional.ofNullable(context.get(key));
    }
    
    public Map<String, Object> getAllContext() {
        return new HashMap<>(context);
    }
    
    public int getRetryCount() {
        return retryCount.get();
    }
    
    public void forceRetry() {
        lock.lock();
        try {
            if (state.get() == RequestState.FAILED) {
                LOGGER.info("Forcing retry for failed request: {}", requestId);
                state.set(RequestState.PENDING);
                retryCount.set(0);
                execute();
            }
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public String toString() {
        return String.format("PendingRequest{requestId=%s, state=%s, retryCount=%d, elapsedTime=%d}",
                requestId, state.get(), retryCount.get(), getElapsedTime());
    }
    
    private static class RetryContextHolder {
        private static final ThreadLocal<Map<String, Object>> retryContext = ThreadLocal.withInitial(HashMap::new);
        
        public static void set(String key, Object value) {
            retryContext.get().put(key, value);
        }
        
        public static Object get(String key) {
            return retryContext.get().get(key);
        }
        
        public static void clear() {
            retryContext.get().clear();
        }
    }
    
    public void setRetryContext(String key, Object value) {
        RetryContextHolder.set(key, value);
        LOGGER.debug("Set retry context for request {}: {} = {}", requestId, key, value);
    }
    
    public Object getRetryContext(String key) {
        return RetryContextHolder.get(key);
    }
    
    public void clearRetryContext() {
        RetryContextHolder.clear();
        LOGGER.debug("Cleared retry context for request {}", requestId);
    }
}
