package org.aksw.jena.exectracker.arq.plugin;

import org.apache.jena.sparql.engine.Plan;

/** Base implementation of PlanWrapper providing delegate storage. */
public class PlanWrapperBase implements PlanWrapper {
    /** The delegate Plan. */
    protected Plan delegate;

    /**
     * Create a new PlanWrapperBase.
     *
     * @param delegate the delegate Plan
     */
    public PlanWrapperBase(Plan delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public Plan getDelegate() {
        return delegate;
    }
}
