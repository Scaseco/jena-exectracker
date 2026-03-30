package org.aksw.jenax.sparql.exec.tracker.system;

public interface HasBasicTaskExec {
    BasicTaskInfo getTaskInfo();
    void abort();
}
