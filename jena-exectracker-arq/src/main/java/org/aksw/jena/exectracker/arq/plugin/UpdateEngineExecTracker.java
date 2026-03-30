package org.aksw.jena.exectracker.arq.plugin;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aksw.jenax.sparql.exec.tracker.system.BasicTaskInfo;
import org.aksw.jenax.sparql.exec.tracker.system.BasicTaskInfoImpl;
import org.aksw.jenax.sparql.exec.tracker.system.HasBasicTaskExec;
import org.aksw.jenax.sparql.exec.tracker.system.TaskListener;
import org.aksw.jenax.sparql.exec.tracker.system.TaskState;
import org.apache.jena.sparql.modify.UpdateEngine;
import org.apache.jena.sparql.util.Context;

public class UpdateEngineExecTracker
    extends UpdateEngineWrapperBase
    implements HasBasicTaskExec
{
    private BasicTaskInfoImpl<UpdateEngineExecTracker> taskInfo;
    private Context context;

    public UpdateEngineExecTracker(UpdateEngine delegate, Context context, TaskListener<? super UpdateEngineExecTracker> listener) {
        super(delegate);
        this.context = context;
        this.taskInfo = new BasicTaskInfoImpl<>(this, () -> "# Update Request", Instant.now(), listener);
    }

    @Override
    public void startRequest() {
        taskInfo.transition(this, TaskState.STARTING, null);
        taskInfo.transition(this, TaskState.RUNNING, () -> { super.startRequest(); });
    }

    @Override
    public void finishRequest() {
        try {
            super.finishRequest();
        } finally {
            taskInfo.transition(this, TaskState.TERMINATED, null);
        }
    }

    @Override
    public BasicTaskInfo getTaskInfo() {
        return taskInfo;
    }

    @Override
    public void abort() {
        AtomicBoolean cancelSignal = context == null ? null : Context.getOrSetCancelSignal(context);
        if (cancelSignal != null) {
            cancelSignal.set(true);
            taskInfo.transition(this, TaskState.TERMINATING, null);
        }
    }
}
