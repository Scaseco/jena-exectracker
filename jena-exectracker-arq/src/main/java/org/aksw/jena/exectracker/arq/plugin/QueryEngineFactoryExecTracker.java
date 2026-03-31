package org.aksw.jena.exectracker.arq.plugin;

import java.util.Objects;

import org.aksw.jena.exectracker.arq.core.ThrowableTracker;
import org.aksw.jena.exectracker.arq.core.ThrowableTrackerFirst;
import org.aksw.jena.exectracker.arq.system.QueryIteratorTask;
import org.aksw.jena.exectracker.arq.system.TaskEventBroker;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.util.Context;

/**
 * QueryEngineFactoryExecTracker - Factory for SPARQL query execution with tracking instrumentation.
 */
public class QueryEngineFactoryExecTracker implements QueryEngineFactory {
    /** Constructor that creates a new QueryEngineFactoryExecTracker instance. */
    public QueryEngineFactoryExecTracker() {}

    /**
     * Check if this factory can handle the given query execution request.
     *
     * @param query the query to execute
     * @param dataset the dataset to query
     * @param context the execution context
     * @return true if this factory accepts the request
     */
    @Override
    public boolean accept(Query query, DatasetGraph dataset, Context context) {
        if (context.isTrue(ExecTrackerConstants.symIsVisited)) {
            return false;
        }
        context.setTrue(ExecTrackerConstants.symIsVisited);
        QueryEngineFactory f = QueryEngineRegistry.findFactory(query, dataset, context);
        boolean isAccepted = f.accept(query, dataset, context);
        context.unset(ExecTrackerConstants.symIsVisited);
        return isAccepted;
    }

    /**
     * Create a Plan with tracking instrumentation.
     *
     * @param query the query to execute
     * @param dataset the dataset to query
     * @param inputBinding the initial binding
     * @param context the execution context
     * @return the Plan
     */
    @Override
    public Plan create(Query query, DatasetGraph dataset, Binding inputBinding, Context context) {
        context.setTrue(ExecTrackerConstants.symIsVisited);
        QueryEngineFactory f = QueryEngineRegistry.findFactory(query, dataset, context);
        Plan plan = f.create(query, dataset, inputBinding, context);
        context.unset(ExecTrackerConstants.symIsVisited);
        Plan result = new PlanTracked(plan, dataset, query, context);
        return result;
    }

    /**
     * Check if this factory can handle the given algebra operation.
     *
     * @param op the algebra operation
     * @param dataset the dataset to query
     * @param context the execution context
     * @return true if this factory accepts the request
     */
    @Override
    public boolean accept(Op op, DatasetGraph dataset, Context context) {
        if (context.isTrue(ExecTrackerConstants.symIsVisited)) {
            return false;
        }
        context.setTrue(ExecTrackerConstants.symIsVisited);
        QueryEngineFactory f = QueryEngineRegistry.findFactory(op, dataset, context);
        boolean isAccepted = f.accept(op, dataset, context);
        context.unset(ExecTrackerConstants.symIsVisited);
        return isAccepted;
    }

    /**
     * Create a Plan from an algebra operation with tracking instrumentation.
     *
     * @param op the algebra operation
     * @param dataset the dataset to query
     * @param inputBinding the initial binding
     * @param context the execution context
     * @return the Plan
     */
    @Override
    public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context) {
        context.setTrue(ExecTrackerConstants.symIsVisited);
        QueryEngineFactory f = QueryEngineRegistry.findFactory(op, dataset, context);
        Plan plan = f.create(op, dataset, inputBinding, context);
        context.unset(ExecTrackerConstants.symIsVisited);

        Query query;
        try {
            query = OpAsQuery.asQuery(op);
        } catch (Exception e) {
            // Op is not reversible into a query. Create a dummy query:
            // SELECT * {
            //   BIND("..." AS ?op)
            // }
            query = new Query();
            query.setQuerySelectType();
            query.setQueryResultStar(true);
            ElementBind bindElt =
                    new ElementBind(Var.alloc("nonReversibleOp"), NodeValue.makeString(Objects.toString(op)));
            ElementGroup groupElt = new ElementGroup();
            groupElt.addElement(bindElt);
            query.setQueryPattern(groupElt);
        }
        Plan result = new PlanTracked(plan, dataset, query, context);
        return result;
    }

    /** PlanTracked - Wraps Plan with tracking instrumentation. */
    private static class PlanTracked extends PlanWrapperBase {
        /** Dataset graph being queried. */
        protected DatasetGraph datasetGraph;

        /** Query being executed. */
        protected Query query;

        /** Execution context. */
        protected Context context;

        /**
         * Create a new PlanTracked with tracking instrumentation.
         *
         * @param delegate the delegate Plan
         * @param datasetGraph the dataset graph
         * @param query the query being executed
         * @param context the execution context
         */
        public PlanTracked(Plan delegate, DatasetGraph datasetGraph, Query query, Context context) {
            super(delegate);
            this.datasetGraph = Objects.requireNonNull(datasetGraph);
            this.query = Objects.requireNonNull(query);
            this.context = Objects.requireNonNull(context);
        }

        /**
         * Get dataset graph.
         *
         * @return dataset graph
         */
        public DatasetGraph getDatasetGraph() {
            return datasetGraph;
        }

        /**
         * Get context.
         *
         * @return context
         */
        public Context getContext() {
            return context;
        }

        /**
         * Get query iterator with tracking instrumentation.
         *
         * @return query iterator
         */
        @Override
        public QueryIterator iterator() {
            QueryIterator baseIt = super.iterator();
            ThrowableTracker throwableTracker = new ThrowableTrackerFirst();
            Context cxt = getContext();
            TaskEventBroker broker = TaskEventBroker.get(cxt);
            QueryIterator result =
                    (broker == null) ? baseIt : new QueryIteratorTask(query, baseIt, broker);
            return result;
        }
    }
}
