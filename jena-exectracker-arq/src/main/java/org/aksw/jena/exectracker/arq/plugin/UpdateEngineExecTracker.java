package org.aksw.jena.exectracker.arq.plugin;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aksw.jena.exectracker.arq.system.BasicTaskInfo;
import org.aksw.jena.exectracker.arq.system.BasicTaskInfoImpl;
import org.aksw.jena.exectracker.arq.system.HasBasicTaskExec;
import org.aksw.jena.exectracker.arq.system.TaskListener;
import org.aksw.jena.exectracker.arq.system.TaskState;
import org.apache.jena.sparql.modify.UpdateEngine;
import org.apache.jena.sparql.util.Context;

/** UpdateEngineExecTracker - Update engine with full task lifecycle tracking. */
public class UpdateEngineExecTracker extends UpdateEngineWrapperBase implements HasBasicTaskExec {
    private BasicTaskInfoImpl<UpdateEngineExecTracker> taskInfo;
    private Context context;

    /**
     * Create a new UpdateEngineExecTracker.
     *
     * @param delegate the delegate UpdateEngine
     * @param context the execution context
     * @param listener the task listener
     */
    public UpdateEngineExecTracker(
            UpdateEngine delegate,
            Context context,
            TaskListener<? super UpdateEngineExecTracker> listener) {
        super(delegate);
        this.context = context;
        this.taskInfo =
                new BasicTaskInfoImpl<>(this, () -> "# Update Request", Instant.now(), listener);
    }

    /** Start the update request with tracking. */
    @Override
    public void startRequest() {
        taskInfo.transition(this, TaskState.STARTING, null);
        taskInfo.transition(
                this,
                TaskState.RUNNING,
                () -> {
                    super.startRequest();
                });
    }

    /** Finish the update request with tracking. */
    @Override
    public void finishRequest() {
        try {
            super.finishRequest();
        } finally {
            taskInfo.transition(this, TaskState.TERMINATED, null);
        }
    }

    /**
     * Get task information.
     *
     * @return task info
     */
    @Override
    public BasicTaskInfo getTaskInfo() {
        return taskInfo;
    }

    /** Abort the current execution. */
    @Override
    public void abort() {
        AtomicBoolean cancelSignal = context == null ? null : Context.getOrSetCancelSignal(context);
        if (cancelSignal != null) {
            cancelSignal.set(true);
            taskInfo.transition(this, TaskState.TERMINATING, null);
        }
    }
}
