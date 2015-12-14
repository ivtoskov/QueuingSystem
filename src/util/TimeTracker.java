package asl.util;

import java.lang.Override;

/**
 * A utility class that is used to track the time
 * spent in the different components of the system.
 */
public class TimeTracker {
    private long lastTracked;
    private long arrivalTime;
    private long middlewareWaitingTime;
    private long middlewareServiceTime;
    private long databaseWaitingTime;
    private long databaseServiceTime;
    private long responseTime;

    public TimeTracker() {
        reset();
    }

    public void reset() {
        arrivalTime = System.nanoTime();
        lastTracked = arrivalTime;
    }

    public void setMiddlewareWaitingTime() {
        long current = System.nanoTime();
        middlewareWaitingTime = current - lastTracked;
        lastTracked = current;
    }

    public void setMiddlewareServiceTime() {
        long current = System.nanoTime();
        middlewareServiceTime = current - lastTracked;
        lastTracked = current;
    }

    public void setDatabaseWaitingTime() {
        long current = System.nanoTime();
        databaseWaitingTime = current - lastTracked;
        lastTracked = current;
    }

    public void setDatabaseServiceTime() {
        long current = System.nanoTime();
        databaseServiceTime = current - lastTracked;
        lastTracked = current;
    }

    public void setResponseTime() {
        long current = System.nanoTime();
        responseTime = current - arrivalTime;
    }

    @Override
    public String toString() {
        return String.format("%.2f", middlewareWaitingTime / 1000000.0) +
                "," + String.format("%.2f", middlewareServiceTime / 1000000.0) +
                "," + String.format("%.2f", databaseWaitingTime / 1000000.0) +
                "," + String.format("%.2f", databaseServiceTime / 1000000.0) +
                "," + String.format("%.2f", responseTime / 1000000.0);
    }
}