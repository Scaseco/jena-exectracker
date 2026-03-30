package org.aksw.jena.exectracker.arq.plugin;

import org.apache.jena.sparql.modify.UpdateEngine;

public class UpdateEngineWrapperBase
    implements UpdateEngineWrapper
{
    protected UpdateEngine delegate;

    public UpdateEngineWrapperBase(UpdateEngine delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public UpdateEngine getDelegate() {
        return delegate;
    }
}
