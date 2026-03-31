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

package org.aksw.jena.exectracker.arq.system;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TaskEventSource - Base class for broadcasting task state change events to registered listeners.
 */
public class TaskEventSource {
    private static final Logger logger = LoggerFactory.getLogger(TaskEventSource.class);

    /** Map of listeners by type, converted to a common listener type. */
    protected Map<TaskListener<?>, TaskListener<HasBasicTaskExec>> listenersByType =
            Collections.synchronizedMap(new LinkedHashMap<>());

    /** Constructor that creates a new TaskEventSource instance. */
    public TaskEventSource() {}

    /**
     * Register a listener for task state changes of a specific type.
     *
     * @param clz the task type to listen for
     * @param listener the listener to register
     * @param <Y> the task type parameter
     * @return a runnable to unregister the listener
     */
    public <Y extends HasBasicTaskExec> Runnable addListener(
            Class<Y> clz, TaskListener<? super Y> listener) {
        listenersByType.compute(
                listener,
                (k, v) -> {
                    if (v != null) {
                        throw new RuntimeException("Listener already registered");
                    }
                    return new TaskListenerTypeAdapter<>(clz, listener);
                });
        return () -> listenersByType.remove(listener);
    }

    /**
     * Broadcast a task state change to all registered listeners.
     *
     * @param task the task that changed state
     */
    protected void advertiseStateChange(HasBasicTaskExec task) {
        for (TaskListener<HasBasicTaskExec> listener : listenersByType.values()) {
            try {
                listener.onStateChange(task);
            } catch (Throwable t) {
                logger.warn("Failure while notifying listener.", t);
            }
        }
    }

    /**
     * Adapter that converts generic task listeners to type-specific listeners.
     *
     * @param <Y> the task type parameter
     */
    class TaskListenerTypeAdapter<Y extends HasBasicTaskExec>
            implements TaskListener<HasBasicTaskExec> {
        protected Class<Y> clz;
        protected TaskListener<? super Y> delegate;

        /**
         * Create a new TaskListenerTypeAdapter.
         *
         * @param clz the task type to adapt
         * @param delegate the type-specific listener
         */
        public TaskListenerTypeAdapter(Class<Y> clz, TaskListener<? super Y> delegate) {
            super();
            this.clz = clz;
            this.delegate = delegate;
        }

        /**
         * Forward state change to delegate if task is instance of target type.
         *
         * @param task the task that changed state
         */
        @Override
        public void onStateChange(HasBasicTaskExec task) {
            if (clz.isInstance(task)) {
                Y obj = clz.cast(task);
                delegate.onStateChange(obj);
            }
        }
    }
}
