package org.aksw.jenax.sparql.exec.tracker.system;

import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;

import org.aksw.jenax.sparql.exec.tracker.core.IteratorTracked;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIteratorWrapper;

public class QueryIteratorTask
    extends QueryIteratorWrapper
    implements HasBasicTaskExec
{
    private BasicTaskInfoImpl<QueryIteratorTask> taskInfo;

    public QueryIteratorTask(Query query, QueryIterator qIter, TaskListener<? super QueryIteratorTask> listener) {
        super(qIter);
        taskInfo = new BasicTaskInfoImpl<>(this, () -> Objects.toString(query), Instant.now(), listener);
        taskInfo.transition(TaskState.STARTING, null);
        taskInfo.transition(TaskState.RUNNING, null);
    }

    @Override
    protected boolean hasNextBinding() {
        return IteratorTracked.track(taskInfo.getThrowableTracker(), super::hasNextBinding);
    }

    @Override
    protected Binding moveToNextBinding() {
        return IteratorTracked.track(taskInfo.getThrowableTracker(), super::moveToNextBinding);
    }

    @Override
    public void forEachRemaining(Consumer<? super Binding> action) {
        IteratorTracked.trackForEachRemaining(taskInfo.getThrowableTracker(), iterator, action);
    }

    @Override
    public final void requestCancel() {
        taskInfo.transition(TaskState.TERMINATING, () -> super.requestCancel());
    }

    @Override
    public void closeIterator() {
        taskInfo.transition(TaskState.TERMINATING, null);
        try {
            super.closeIterator();
        } finally {
            taskInfo.transition(TaskState.TERMINATED, null);
        }
    }

    @Override
    public BasicTaskInfo getTaskInfo() {
        return taskInfo;
    }

    @Override
    public void abort() {
        cancel();
    }
}
