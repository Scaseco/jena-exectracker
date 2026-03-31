package org.aksw.jena.exectracker.arq.system;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.serializer.SerializationContext;

public class QueryIteratorPureWrapper<X extends QueryIterator>
    implements QueryIterator
{
    private X delegate;

    public QueryIteratorPureWrapper(X delegate) {
        this.delegate = delegate;
    }

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
