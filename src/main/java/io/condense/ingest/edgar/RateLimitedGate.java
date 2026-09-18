package io.condense.ingest.edgar;

import java.time.Duration;

class RateLimitedGate {

    private final long minimumIntervalNanos;

    private long lastCallNanos;

    RateLimitedGate(Duration minimumInterval) {
        this.minimumIntervalNanos = minimumInterval.toNanos();
    }

    synchronized void await() {
        long elapsed = System.nanoTime() - lastCallNanos;
        long remaining = minimumIntervalNanos - elapsed;
        if (remaining > 0) {
            try {
                Thread.sleep(Duration.ofNanos(remaining));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while rate limiting EDGAR requests", e);
            }
        }
        lastCallNanos = System.nanoTime();
    }
}
