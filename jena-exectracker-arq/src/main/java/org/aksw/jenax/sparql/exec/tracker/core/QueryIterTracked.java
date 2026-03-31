package org.aksw.jenax.sparql.exec.tracker.core;

import java.util.Objects;
import java.util.function.Consumer;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIteratorWrapper;

/** Simple wrapper for QueryIterator that tracks exceptions using ThrowableTracker. */
public class QueryIterTracked extends QueryIteratorWrapper {
    /** Exception tracker that receives reported throwables for exception tracking. */
    protected ThrowableTracker tracker;

    /**
     * Create a new QueryIterTracked.
     *
     * @param qIter the base query iterator
     * @param tracker the exception tracker
     */
    public QueryIterTracked(QueryIterator qIter, ThrowableTracker tracker) {
        super(qIter);
        this.tracker = Objects.requireNonNull(tracker);
    }

    @Override
    protected boolean hasNextBinding() {
        return IteratorTracked.trackBoolean(tracker, iterator::hasNext);
    }

    @Override
    protected Binding moveToNextBinding() {
        return IteratorTracked.track(tracker, iterator::next);
    }

    @Override
    public void forEachRemaining(Consumer<? super Binding> action) {
        IteratorTracked.trackForEachRemaining(tracker, iterator, action);
    }
}
