package org.aksw.jena.exectracker.arq.plugin;

import org.apache.jena.sparql.modify.UpdateEngine;

/**
 * UpdateEngineWrapperBase - Base implementation of UpdateEngineWrapper providing delegate storage.
 */
public class UpdateEngineWrapperBase implements UpdateEngineWrapper {
    /** The underlying delegate UpdateEngine. */
    protected UpdateEngine delegate;

    /**
     * Create a new UpdateEngineWrapperBase.
     *
     * @param delegate the delegate UpdateEngine
     */
    public UpdateEngineWrapperBase(UpdateEngine delegate) {
        super();
        this.delegate = delegate;
    }

    /**
     * Get the underlying delegate UpdateEngine.
     *
     * @return the delegate
     */
    @Override
    public UpdateEngine getDelegate() {
        return delegate;
    }
}
