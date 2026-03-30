package org.aksw.jena.exectracker.arq.plugin;

import java.util.Objects;

import org.aksw.jenax.sparql.exec.tracker.core.ThrowableTracker;
import org.aksw.jenax.sparql.exec.tracker.core.ThrowableTrackerFirst;
import org.aksw.jenax.sparql.exec.tracker.system.QueryIteratorTask;
import org.aksw.jenax.sparql.exec.tracker.system.TaskEventBroker;
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

/** A query engine factory over {@link DatasetGraphOverRDFEngine}.*/
public class QueryEngineFactoryExecTracker
    implements QueryEngineFactory
{
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

    @Override
    public Plan create(Query query, DatasetGraph dataset, Binding inputBinding, Context context) {
        context.setTrue(ExecTrackerConstants.symIsVisited);
        QueryEngineFactory f = QueryEngineRegistry.findFactory(query, dataset, context);
        Plan plan = f.create(query, dataset, inputBinding, context);
        context.unset(ExecTrackerConstants.symIsVisited);
        Plan result = new PlanTracked(plan, dataset, query, context);
        return result;
    }

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
            ElementBind bindElt = new ElementBind(
                Var.alloc("nonReversibleOp"), NodeValue.makeString(Objects.toString(op)));
            ElementGroup groupElt = new ElementGroup();
            groupElt.addElement(bindElt);
            query.setQueryPattern(groupElt);
        }
        Plan result = new PlanTracked(plan, dataset, query, context);
        return result;
    }

    private static class PlanTracked
        extends PlanWrapperBase
    {
        protected DatasetGraph datasetGraph;
        protected Query query;
        protected Context context;

        public PlanTracked(Plan delegate, DatasetGraph datasetGraph, Query query, Context context) {
            super(delegate);
            this.datasetGraph = Objects.requireNonNull(datasetGraph);
            this.query = Objects.requireNonNull(query);
            this.context = Objects.requireNonNull(context);
        }

        public DatasetGraph getDatasetGraph() {
            return datasetGraph;
        }

        public Query getQuery() {
            return query;
        }

        public Context getContext() {
            return context;
        }

        @Override
        public QueryIterator iterator() {
            QueryIterator baseIt = super.iterator();
            ThrowableTracker throwableTracker = new ThrowableTrackerFirst();
            Context cxt = getContext();
            TaskEventBroker broker = TaskEventBroker.get(cxt);
            QueryIterator result = (broker == null)
                ? baseIt
                : new QueryIteratorTask(query, baseIt, broker);
            return result;
        }
    }
}
