package org.aksw.jena.exectracker.arq.system;

import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;

import org.aksw.jena.exectracker.arq.core.IteratorTracked;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.serializer.SerializationContext;

/**
 * Wraps a QueryIterator with full task lifecycle management including state tracking and exception
 * handling.
 */
public class QueryIteratorTask extends QueryIteratorPureWrapper<QueryIterator> implements HasBasicTaskExec {

    private BasicTaskInfoImpl<QueryIteratorTask> taskInfo;
    private Query query;

    /**
     * Create a new QueryIteratorTask.
     *
     * @param query the query being executed
     * @param qIter the base query iterator
     * @param listener listener for state change events
     */
    public QueryIteratorTask(
            Query query, QueryIterator qIter, TaskListener<? super QueryIteratorTask> listener) {
        super(qIter);
        this.query = query;
        taskInfo =
                new BasicTaskInfoImpl<>(this, () -> Objects.toString(query), Instant.now(), listener);
        taskInfo.transition(TaskState.STARTING, null);
        taskInfo.transition(TaskState.RUNNING, null);
    }

    @Override
    public boolean hasNext() {
        return IteratorTracked.track(taskInfo.getThrowableTracker(), super::hasNext);
    }

    @Override
    public Binding next() {
        return IteratorTracked.track(taskInfo.getThrowableTracker(), super::next);
    }

    @Override
    public Binding nextBinding() {
        return next();
    }

    @Override
    public void forEachRemaining(Consumer<? super Binding> action) {
        IteratorTracked.trackForEachRemaining(taskInfo.getThrowableTracker(), getDelegate(), action);
    }

    @Override
    public void cancel() {
        taskInfo.transition(TaskState.TERMINATING, () -> super.cancel());
    }

    @Override
    public void close() {
        taskInfo.transition(TaskState.TERMINATING, null);
        try {
            super.close();
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

    @Override
    public void output(IndentedWriter out) {
        out.write(Objects.toString(query));
    }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt) {
        out.write(Objects.toString(query));
    }

    @Override
    public String toString() {
        Throwable t = getTaskInfo().getThrowable().orElse(null);
        return Objects.toString(query) + (t == null ? "" : t);
    }
}
