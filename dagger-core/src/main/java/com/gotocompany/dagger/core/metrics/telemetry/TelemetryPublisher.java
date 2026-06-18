package com.gotocompany.dagger.core.metrics.telemetry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The interface Telemetry publisher.
 */
public interface TelemetryPublisher {
    /**
     * Shared registry of {@link TelemetrySubscriber} instances notified when telemetry changes.
     *
     * <p>Being declared in an interface, this collection is implicitly {@code static}, so all
     * publishers share the same subscriber list.
     */
    List<TelemetrySubscriber> TELEMETRY_SUBSCRIBERS = new ArrayList<>();

    /**
     * Add subscriber.
     *
     * @param subscriber the subscriber
     */
    default void addSubscriber(TelemetrySubscriber subscriber) {
        TELEMETRY_SUBSCRIBERS.add(subscriber);
    }

    /**
     * Notify subscriber.
     *
     * @param subscriber the subscriber
     */
    default void notifySubscriber(TelemetrySubscriber subscriber) {
        TELEMETRY_SUBSCRIBERS.add(subscriber);
        notifySubscriber();
    }

    /**
     * Notify subscriber.
     */
    default void notifySubscriber() {
        preProcessBeforeNotifyingSubscriber();
        TELEMETRY_SUBSCRIBERS.forEach(telemetrySubscriber -> telemetrySubscriber.updated(this));
    }

    /**
     * Gets telemetry.
     *
     * @return the telemetry
     */
    Map<String, List<String>> getTelemetry();

    /**
     * Pre process before notifying subscriber.
     */
    default void preProcessBeforeNotifyingSubscriber() {
    }
}
