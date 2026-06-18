package com.gotocompany.dagger.core.metrics.reporters;

/**
 * The No op error reporter.
 */
public class NoOpErrorReporter implements ErrorReporter {
    /**
     * No-op implementation that intentionally ignores the fatal exception.
     *
     * @param exception the exception, which is ignored
     */
    @Override
    public void reportFatalException(Exception exception) {

    }

    /**
     * No-op implementation that intentionally ignores the non-fatal exception.
     *
     * @param exception the exception, which is ignored
     */
    @Override
    public void reportNonFatalException(Exception exception) {

    }
}
