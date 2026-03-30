package org.aksw.jenax.sparql.exec.tracker.system;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.aksw.jenax.sparql.exec.tracker.core.ThrowableTracker;
import org.aksw.jenax.sparql.exec.tracker.core.ThrowableTrackerFirst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicTaskInfoImpl<T extends HasBasicTaskExec>
    implements BasicTaskInfo
{
    private static final Logger logger = LoggerFactory.getLogger(BasicTaskInfoImpl.class);

    protected final T owner;
    protected Supplier<String> labelSupplier;
    protected TaskListener<? super T> listener;
    protected ThrowableTracker throwableTracker;
    protected Instant creationTime;
    protected Instant startTime = null;
    protected Instant abortTime = null;
    protected Instant finishTime = null;
    protected TaskState currentState = TaskState.CREATED;

    public BasicTaskInfoImpl(T owner, Supplier<String> labelSupplier, TaskListener<T> listener) {
        this(owner, labelSupplier, Instant.now(), listener);
    }

    public BasicTaskInfoImpl(T owner, Supplier<String> labelSupplier, Instant creationTime, TaskListener<? super T> listener) {
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

    public ThrowableTracker getThrowableTracker() {
        return throwableTracker;
    }

    /** Update state notifies all listeners of the change. */
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

    protected void updateTime(TaskState reachedState) {
        switch (reachedState) {
        case CREATED:
            if (creationTime == null) { creationTime = Instant.now(); }
            break;
        case STARTING:
            if (startTime == null) { startTime = Instant.now(); }
            break;
        case RUNNING:
            // runningTime = Instant.now();
            break;
        case TERMINATING:
            break;
        case TERMINATED:
            if (finishTime == null) { finishTime = Instant.now(); }
            break;
        default:
            throw new IllegalStateException("should never come here");
        }
    }

    public void cancel() {
        if (abortTime == null) {
            this.abortTime = Instant.now();
        }
        transition(owner, TaskState.TERMINATING, null);
    }

    public void transition(TaskState targetState, Runnable action) {
        transition(owner, targetState, action);
    }

    /**
     * Run the given action.
     *
     * On success, transitions to the specified target state.
     *
     * On failure, transitions to {@link TaskState#TERMINATING} and re-throws the encountered exception.
     * This should cause a subsequent call to close() which transitions to {@link TaskState#TERMINATED}.
     */
    public void transition(T obj, TaskState targetState, Runnable action) {
        try {
            if (action != null) {
                action.run();
            }
            advertiseStateChange(targetState, obj);
        } catch (Throwable throwable) {
            throwable.addSuppressed(new RuntimeException("Failure transitioning from " + currentState + " to " + targetState + ".", throwable));
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
