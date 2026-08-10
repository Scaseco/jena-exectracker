package org.aksw.jena.exectracker.arq.plugin;

import org.apache.jena.sparql.util.Symbol;

/** ExecTrackerConstants - Configuration symbols for the execution tracker. */
public class ExecTrackerConstants {
    /** Constructor that creates a new ExecTrackerConstants instance. */
    public ExecTrackerConstants() {}

    /** Symbol to track whether a query has been visited by the exec tracker. */
    public static final Symbol symIsVisited = Symbol.create("exectracker.isVisited");

//    public static final Symbol symIsVisitedQueryCreateOp = Symbol.create("exectracker.isVisited.query.create.op");
//    public static final Symbol symIsVisitedQueryAcceptOp = Symbol.create("exectracker.isVisited.query.accept.op");
//    public static final Symbol symIsVisitedQueryCreateQuery = Symbol.create("exectracker.isVisited.query.create.query");
//    public static final Symbol symIsVisitedQueryAcceptQuery = Symbol.create("exectracker.isVisited.query.accept.query");
//
//    public static final Symbol symIsVisitedUpdateAccept = Symbol.create("exectracker.isVisited.update.accept");
//    public static final Symbol symIsVisitedUpdateCreate = Symbol.create("exectracker.isVisited.update.create");
}
