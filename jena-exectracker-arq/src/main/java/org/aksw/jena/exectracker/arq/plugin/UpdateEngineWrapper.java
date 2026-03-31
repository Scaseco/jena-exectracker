package org.aksw.jena.exectracker.arq.plugin;

import org.apache.jena.sparql.modify.UpdateEngine;
import org.apache.jena.sparql.modify.UpdateSink;

/**
 * UpdateEngineWrapper - Wrapper interface for UpdateEngine that delegates to an underlying
 * instance.
 */
public interface UpdateEngineWrapper extends UpdateEngine {
    /**
     * Get the underlying delegate UpdateEngine.
     *
     * @return the delegate
     */
    UpdateEngine getDelegate();

    @Override
    default void startRequest() {
        getDelegate().startRequest();
    }

    @Override
    default void finishRequest() {
        getDelegate().finishRequest();
    }

    @Override
    default UpdateSink getUpdateSink() {
        return getDelegate().getUpdateSink();
    }
}
