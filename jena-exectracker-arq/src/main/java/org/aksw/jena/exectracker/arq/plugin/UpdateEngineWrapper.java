package org.aksw.jena.exectracker.arq.plugin;

import org.apache.jena.sparql.modify.UpdateEngine;
import org.apache.jena.sparql.modify.UpdateSink;

public interface UpdateEngineWrapper
    extends UpdateEngine
{
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