package org.aksw.jenax.sparql.exec.tracker.example;

import org.aksw.jenax.sparql.exec.tracker.system.TaskEventBroker;
import org.aksw.jenax.sparql.exec.tracker.system.TaskEventHistory;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sys.JenaSystem;

public class ExampleExecTracker {
    static { JenaSystem.init(); }

    public static void main(String[] args) {
        // QueryEngineRegistry.get().add(new QueryEngineFactoryExecTracker());
        // UpdateEngineRegistry.get().add(new UpdateEngineFactoryExecTracker());

        TaskEventBroker broker = TaskEventBroker.getOrCreate(ARQ.getContext());
        TaskEventHistory history = TaskEventHistory.getOrCreate(ARQ.getContext());
        history.connect(broker);
        // broker.connect(history);

        DatasetGraph dsg = DatasetGraphFactory.create();

        UpdateExec.dataset(dsg).update("PREFIX eg: <http://www.example.org/> INSERT DATA { eg:s eg:p eg:o }").execute();

        Table table = QueryExec.dataset(dsg).query("SELECT * { ?s ?p ?o }").table();
        RowSetOps.out(table.toRowSet());

        System.out.println(history);
    }
}
