package org.aksw.jena.exectracker.arq.plugin;

import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sys.JenaSubsystemLifecycle;

/** JenaPluginExecTracker - Jena subsystem lifecycle plugin for execution tracking. */
public class JenaPluginExecTracker implements JenaSubsystemLifecycle {
    /** Constructor that creates a new JenaPluginExecTracker instance. */
    public JenaPluginExecTracker() {}

    @Override
    public void start() {
        QueryEngineRegistry queryReg = QueryEngineRegistry.get();
        init(queryReg);

        UpdateEngineRegistry updateReg = UpdateEngineRegistry.get();
        init(updateReg);
    }

    @Override
    public void stop() {}

    /**
     * Initialize the query engine registry with the exec tracker factory.
     *
     * @param reg the query engine registry
     */
    public static void init(QueryEngineRegistry reg) {
        reg.add(new QueryEngineFactoryExecTracker());
    }

    /**
     * Initialize the update engine registry with the exec tracker factory.
     *
     * @param reg the update engine registry
     */
    public static void init(UpdateEngineRegistry reg) {
        reg.add(new UpdateEngineFactoryExecTracker());
    }

    @Override
    public int level() {
        return 1000000;
    }
}
