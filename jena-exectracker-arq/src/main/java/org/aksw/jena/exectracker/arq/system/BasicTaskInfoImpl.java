package org.aksw.jena.exectracker.arq.system;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.aksw.jena.exectracker.arq.core.ThrowableTracker;
import org.aksw.jena.exectracker.arq.core.ThrowableTrackerFirst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link BasicTaskInfo} that manages task state transitions and notifies
 * listeners. Handles timing tracking and exception collection.
 *
 * @param <T> The type of the owner task
 */
public class BasicTaskInfoImpl<T extends HasBasicTaskExec> implements BasicTaskInfo {
    private static final Logger logger = LoggerFactory.getLogger(BasicTaskInfoImpl.class);

    /** The owner task instance. */
    protected final T owner;

    /** Supplier for the task label. */
    protected Supplier<String> labelSupplier;

    /** Listener notified of state change events. */
    protected TaskListener<? super T> listener;

    /** Tracker for exceptions encountered during execution. */
    protected ThrowableTracker throwableTracker;

    /** Time when the task was created. */
    protected Instant creationTime;

    /** Time when the task started, or null if not started. */
    protected Instant startTime = null;

    /** Time when the task was aborted, or null if not aborted. */
    protected Instant abortTime = null;

    /** Time when the task finished, or null if not finished. */
    protected Instant finishTime = null;

    /** Current task state. */
    protected TaskState currentState = TaskState.CREATED;

    /**
     * Create a new BasicTaskInfoImpl with current time as creation time.
     *
     * @param owner the owner task
     * @param labelSupplier supplier for the task label
     * @param listener listener for state change events
     */
    public BasicTaskInfoImpl(T owner, Supplier<String> labelSupplier, TaskListener<T> listener) {
        this(owner, labelSupplier, Instant.now(), listener);
    }

    /**
     * Create a new BasicTaskInfoImpl with specified creation time.
     *
     * @param owner the owner task
     * @param labelSupplier supplier for the task label
     * @param creationTime the creation time
     * @param listener listener notified of state change events
     */
    public BasicTaskInfoImpl(
            T owner,
            Supplier<String> labelSupplier,
            Instant creationTime,
            TaskListener<? super T> listener) {
        super();
        this.throwableTracker = new ThrowableTrackerFirst();
        this.owner = Objects.requireNonNull(owner);
        this.creationTime = Objects.requireNonNull(creationTime);
        this.listener = Objects.requireNonNull(listener);
        this.labelSupplier = labelSupplier;
    }

    @Override
    public TaskState getTaskState() {
        return currentState;
    }

    @Override
    public Instant getCreationTime() {
        return creationTime;
    }

    @Override
    public Optional<Instant> getAbortTime() {
        return Optional.ofNullable(abortTime);
    }

    @Override
    public Optional<Instant> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    @Override
    public Optional<Instant> getFinishTime() {
        return Optional.ofNullable(finishTime);
    }

    /**
     * Get the exception tracker.
     *
     * @return exception tracker
     */
    public ThrowableTracker getThrowableTracker() {
        return throwableTracker;
    }

    /**
     * Update the state and notify all listeners of the change.
     *
     * @param newState the new state
     * @param obj the owner object
     */
    public void advertiseStateChange(TaskState newState, T obj) {
        Objects.requireNonNull(newState);
        if (currentState == null || newState.ordinal() > currentState.ordinal()) {
            // State oldState = currentState;
            currentState = newState;
            updateTime(currentState);
            if (listener != null) {
                try {
                    listener.onStateChange(obj);
                } catch (Throwable e) {
                    logger.warn("Exception raised in listener.", e);
                }
            }
        }
    }

    /**
     * Update timing information based on the reached state.
     *
     * @param reachedState the state that was reached
     */
    protected void updateTime(TaskState reachedState) {
        switch (reachedState) {
            case CREATED:
                if (creationTime == null) {
                    creationTime = Instant.now();
                }
                break;
            case STARTING:
                if (startTime == null) {
                    startTime = Instant.now();
                }
                break;
            case RUNNING:
                // runningTime = Instant.now();
                break;
            case TERMINATING:
                break;
            case TERMINATED:
                if (finishTime == null) {
                    finishTime = Instant.now();
                }
                break;
            default:
                throw new IllegalStateException("should never come here");
        }
    }

    /** Mark the task as being aborted and transition to TERMINATING state. */
    public void cancel() {
        if (abortTime == null) {
            this.abortTime = Instant.now();
        }
        transition(owner, TaskState.TERMINATING, null);
    }

    /**
     * Transition to the target state with the given action.
     *
     * @param targetState the target state
     * @param action the action to execute
     */
    public void transition(TaskState targetState, Runnable action) {
        transition(owner, targetState, action);
    }

    /**
     * Run the given action. On success, transitions to the specified target state. On failure,
     * transitions to {@link TaskState#TERMINATING} and re-throws the encountered exception. This
     * should cause a subsequent call to close() which transitions to {@link TaskState#TERMINATED}.
     *
     * @param obj the owner object
     * @param targetState the target state
     * @param action the action to execute
     */
    public void transition(T obj, TaskState targetState, Runnable action) {
        try {
            if (action != null) {
                action.run();
            }
            advertiseStateChange(targetState, obj);
        } catch (Throwable throwable) {
            throwable.addSuppressed(
                    new RuntimeException(
                            "Failure transitioning from " + currentState + " to " + targetState + ".",
                            throwable));
            getThrowableTracker().report(throwable);
            advertiseStateChange(TaskState.TERMINATING, obj);
            throw throwable;
        }
    }

    @Override
    public String getLabel() {
        String result = Optional.ofNullable(labelSupplier).map(Supplier::get).orElse("");
        return result;
    }

    @Override
    public String getStatusMessage() {
        return "";
    }

    @Override
    public Optional<Throwable> getThrowable() {
        return getThrowableTracker().getFirstThrowable();
    }
}
