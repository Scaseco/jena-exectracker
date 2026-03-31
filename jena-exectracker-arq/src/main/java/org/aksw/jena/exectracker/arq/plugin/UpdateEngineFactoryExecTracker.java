package org.aksw.jena.exectracker.arq.plugin;

import org.aksw.jena.exectracker.arq.system.TaskEventBroker;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.modify.UpdateEngine;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sparql.util.Context;

/**
 * UpdateEngineFactoryExecTracker - Factory for SPARQL update execution with tracking
 * instrumentation.
 */
public class UpdateEngineFactoryExecTracker implements UpdateEngineFactory {
    /** Constructor that creates a new UpdateEngineFactoryExecTracker instance. */
    public UpdateEngineFactoryExecTracker() {}

    /**
     * Check if this factory can handle the given update execution request.
     *
     * @param dataset the dataset to update
     * @param context the execution context
     * @return true if this factory accepts the request
     */
    @Override
    public boolean accept(DatasetGraph dataset, Context context) {
        if (context.isTrue(ExecTrackerConstants.symIsVisited)) {
            return false;
        }
        context.setTrue(ExecTrackerConstants.symIsVisited);
        UpdateEngineFactory f = UpdateEngineRegistry.findFactory(dataset, context);
        boolean isAccepted = f.accept(dataset, context);
        context.unset(ExecTrackerConstants.symIsVisited);
        return isAccepted;
    }

    /**
     * Create an UpdateEngine with tracking instrumentation.
     *
     * @param dataset dataset graph
     * @param context execution context
     * @return the update engine
     */
    @Override
    public UpdateEngine create(DatasetGraph dataset, Context context) {
        context.setTrue(ExecTrackerConstants.symIsVisited);
        UpdateEngineFactory f = UpdateEngineRegistry.findFactory(dataset, context);
        UpdateEngine baseEngine = f.create(dataset, context);
        context.unset(ExecTrackerConstants.symIsVisited);

        TaskEventBroker broker = TaskEventBroker.get(context);
        UpdateEngine result =
                (broker == null) ? baseEngine : new UpdateEngineExecTracker(baseEngine, context, broker);
        return result;
    }
}
