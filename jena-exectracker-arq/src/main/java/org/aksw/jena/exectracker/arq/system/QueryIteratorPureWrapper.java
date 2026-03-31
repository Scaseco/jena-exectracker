package org.aksw.jena.exectracker.arq.system;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.serializer.SerializationContext;

/**
 * A wrapper class that delegates all QueryIterator operations to an underlying delegate instance.
 *
 * @param <X> the type of the delegate QueryIterator
 */
public class QueryIteratorPureWrapper<X extends QueryIterator>
    implements QueryIterator
{
    private X delegate;

    /**
     * Creates a new wrapper instance that delegates to the specified QueryIterator.
     *
     * @param delegate the QueryIterator to wrap and delegate all operations to
     */
    public QueryIteratorPureWrapper(X delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns the underlying delegate QueryIterator.
     *
     * @return the delegate QueryIterator
     */
    protected X getDelegate() {
        return delegate;
    }

    @Override
    public boolean hasNext() {
        return getDelegate().hasNext();
    }

    @Override
    public Binding next() {
        return getDelegate().next();
    }

    @Override
    public void close() {
        getDelegate().close();
    }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt) {
        getDelegate().output(out, sCxt);
    }

    @Override
    public String toString(PrefixMapping pmap) {
        return getDelegate().toString(pmap);
    }

    @Override
    public void output(IndentedWriter out) {
        getDelegate().output(out);
    }

    @Override
    public Binding nextBinding() {
        return getDelegate().nextBinding();
    }

    @Override
    public void cancel() {
        getDelegate().cancel();
    }
}
