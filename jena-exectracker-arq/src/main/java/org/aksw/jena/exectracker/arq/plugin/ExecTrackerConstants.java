package org.aksw.jena.exectracker.arq.plugin;

import org.apache.jena.sparql.util.Symbol;

/** ExecTrackerConstants - Configuration symbols for the execution tracker. */
public class ExecTrackerConstants {
    /** Constructor that creates a new ExecTrackerConstants instance. */
    public ExecTrackerConstants() {}

    /** Symbol to track whether a query has been visited by the exec tracker. */
    public static final Symbol symIsVisited = Symbol.create("exectracker.isVisited");
}
