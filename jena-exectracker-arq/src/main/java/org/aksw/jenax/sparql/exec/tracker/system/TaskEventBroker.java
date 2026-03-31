/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.aksw.jenax.sparql.exec.tracker.system;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * TaskEventBroker - A broker that acts as both sink and source for task state change events.
 *
 * <p>A broker can connect to other brokers using {@link #connect(TaskEventBroker)} and disconnect
 * from them all using {@link #disconnectFromAll()}.
 */
public class TaskEventBroker extends TaskEventSource implements TaskListener<HasBasicTaskExec> {
    /** Constructor that creates a new TaskEventBroker instance. */
    public TaskEventBroker() {}

    private Map<TaskListener<?>, Runnable> upstreamRegistrations = new ConcurrentHashMap<>();

    /**
     * Connect to an upstream TaskEventBroker.
     *
     * @param upstream the upstream broker
     * @return a runnable to unregister the connection
     */
    public Runnable connect(TaskEventBroker upstream) {
        Runnable unregisterFromBase = upstream.addListener(HasBasicTaskExec.class, this);
        Runnable unregisterFromThis =
                upstreamRegistrations.computeIfAbsent(
                        upstream,
                        u -> {
                            return () -> {
                                unregisterFromBase.run();
                                upstreamRegistrations.remove(upstream);
                            };
                        });
        return unregisterFromThis;
    }

    @Override
    public void onStateChange(HasBasicTaskExec task) {
        advertiseStateChange(task);
    }

    /** Disconnect from all upstream TaskEventBrokers. */
    public void disconnectFromAll() {
        upstreamRegistrations.values().forEach(Runnable::run);
    }

    //    public static QueryExec track(QueryExec queryExec) {
    //        Context cxt = queryExec.getContext();
    //        return track(cxt, queryExec);
    //    }
    //
    /**
     * If there is a taskTracker in the context then return a {@link QueryExecTaskBase}. Otherwise
     * return the provided query exec.
     */
    //    public static <T extends HasBasicTaskExec> track(Context cxt, T queryExec) {
    //        TaskEventBroker registry = get(cxt);
    //        HasBasicTaskExec result = (registry == null)
    //            ? queryExec
    //            : QueryExecTask.create(queryExec, registry);
    //        return result;
    //    }

    //    public static UpdateExec track(UpdateExec updateExec) {
    //        Context cxt = updateExec.getContext();
    //        return track(cxt, updateExec);
    //    }

    /**
     * If there is a taskTracker in the context then return a {@link QueryExecTaskBase}. Otherwise
     * return the provided query exec.
     */
    //    public static UpdateExec track(Context cxt, UpdateExec updateExec) {
    //        TaskEventBroker registry = get(cxt);
    //        return track(registry, updateExec);
    //    }
    //
    //    public static UpdateExec track(TaskEventBroker tracker, UpdateExec updateExec) {
    //        UpdateExec result = (tracker == null)
    //            ? updateExec
    //            : UpdateExecTask.create(updateExec, tracker);
    //        return result;
    //    }

    // ----- ARQ Integration -----

    /** Symbol for TaskEventBroker in context. */
    public static final Symbol symTaskEventBroker = SystemARQ.allocSymbol("taskEventBroker");

    /**
     * Get TaskEventBroker from dataset graph context.
     *
     * @param dsg the dataset graph
     * @return TaskEventBroker, or null if not registered
     */
    public static TaskEventBroker get(DatasetGraph dsg) {
        return dsg == null ? null : get(dsg.getContext());
    }

    /**
     * Get TaskEventBroker from context.
     *
     * @param context the context
     * @return TaskEventBroker, or null if not registered
     */
    public static TaskEventBroker get(Context context) {
        return context == null ? null : context.get(symTaskEventBroker);
    }

    /**
     * Remove TaskEventBroker from context.
     *
     * @param context the context
     */
    public static void remove(Context context) {
        if (context != null) {
            context.remove(symTaskEventBroker);
        }
    }

    /**
     * Get or create TaskEventBroker in context.
     *
     * @param context the context
     * @return TaskEventBroker
     */
    public static TaskEventBroker getOrCreate(Context context) {
        TaskEventBroker result =
                context.computeIfAbsent(symTaskEventBroker, sym -> new TaskEventBroker());
        return result;
    }

    /**
     * Get TaskEventBroker from context or throw exception.
     *
     * @param context the context
     * @return TaskEventBroker
     * @throws NoSuchElementException if not registered
     */
    public static TaskEventBroker require(Context context) {
        TaskEventBroker result = get(context);
        if (result == null) {
            throw new NoSuchElementException("No task event broker in context.");
        }
        return result;
    }
}
