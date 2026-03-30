package org.aksw.jenax.sparql.exec.tracker.plugin;

import org.aksw.jenax.sparql.exec.tracker.system.TaskEventBroker;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.modify.UpdateEngine;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sparql.util.Context;

public class UpdateEngineFactoryExecTracker
    implements UpdateEngineFactory
{
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

    @Override
    public UpdateEngine create(DatasetGraph dataset, Context context) {
        context.setTrue(ExecTrackerConstants.symIsVisited);
        UpdateEngineFactory f = UpdateEngineRegistry.findFactory(dataset, context);
        UpdateEngine baseEngine = f.create(dataset, context);
        context.unset(ExecTrackerConstants.symIsVisited);

        TaskEventBroker broker = TaskEventBroker.get(context);
        UpdateEngine result = (broker == null)
            ? baseEngine
            : new UpdateEngineExecTracker(baseEngine, context, broker);
        return result;
    }
}
