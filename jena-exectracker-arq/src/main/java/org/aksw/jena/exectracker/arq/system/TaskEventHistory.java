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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TaskEventHistory - Event broker that stores the last n events in a bounded history.
 *
 * <p>A broker (sink + source) that stores the last n events in a bounded history.
 */
public class TaskEventHistory extends TaskEventBroker {
    /** Constructor that creates a new TaskEventHistory instance. */
    public TaskEventHistory() {}

    private static final Logger logger = LoggerFactory.getLogger(TaskEventHistory.class);

    /** Next serial number to assign to a task. */
    protected AtomicLong nextSerial = new AtomicLong();

    /** Maps task ID to its serial number. */
    protected Map<Long, Long> taskIdToSerial = new ConcurrentHashMap<>();

    /** Maps serial number to task instance. */
    protected ConcurrentNavigableMap<Long, HasBasicTaskExec> serialToTask =
            new ConcurrentSkipListMap<>();

    /** Maximum number of history entries to retain. */
    protected int maxHistorySize = 1000;

    /** Circular buffer of historical task events. */
    protected ConcurrentLinkedDeque<Entry<Long, HasBasicTaskExec>> history =
            new ConcurrentLinkedDeque<>();

    /**
     * Get task by task ID.
     *
     * @param taskId the task ID
     * @return the task, or null if not found
     */
    public HasBasicTaskExec getByTaskId(long taskId) {
        Long serial = taskIdToSerial.get(taskId);
        HasBasicTaskExec result = getTaskBySerialId(serial);
        return result;
    }

    /**
     * Get task by serial ID.
     *
     * @param serialId the serial ID
     * @return the task, or null if not found
     */
    public HasBasicTaskExec getTaskBySerialId(long serialId) {
        return serialToTask.get(serialId);
    }

    /**
     * Get active tasks map.
     *
     * @return map of active tasks
     */
    public ConcurrentNavigableMap<Long, HasBasicTaskExec> getActiveTasks() {
        return serialToTask;
    }

    /**
     * Get history of completed tasks.
     *
     * @return history deque
     */
    public ConcurrentLinkedDeque<Entry<Long, HasBasicTaskExec>> getHistory() {
        return history;
    }

    /**
     * Set maximum history size and trim if necessary.
     *
     * @param maxHistorySize maximum number of history entries
     */
    public void setMaxHistorySize(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
        trimHistory();
    }

    @Override
    public void onStateChange(HasBasicTaskExec task) {
        switch (task.getTaskInfo().getTaskState()) {
            case STARTING:
                put(task);
                break;
            case TERMINATED:
                remove(task);
                break;
            default:
                break;
        }
    }

    /**
     * Get identity hash code of task as its ID.
     *
     * @param task the task
     * @return task ID
     */
    public long getId(HasBasicTaskExec task) {
        long id = System.identityHashCode(task);
        return id;
    }

    /**
     * Get serial ID for a task.
     *
     * @param taskId the task ID
     * @return serial ID, or null if not found
     */
    public Long getSerialId(long taskId) {
        return taskIdToSerial.get(taskId);
    }

    /**
     * Register a new task for tracking.
     *
     * @param newTask the task to track
     */
    public void put(HasBasicTaskExec newTask) {
        long taskId = getId(newTask);
        boolean[] accepted = {false};
        taskIdToSerial.compute(
                taskId,
                (_taskId, oldSerial) -> {
                    if (oldSerial != null) {
                        HasBasicTaskExec oldTask = serialToTask.get(oldSerial);
                        if (oldTask != newTask) {
                            // Distinct tasks with the same id - should never happen.
                            logger.warn("Rejected task tracking because of a hash clash.");
                        } else {
                            logger.warn("Task was already added.");
                        }
                        return oldSerial;
                    }

                    accepted[0] = true;
                    long r = nextSerial.incrementAndGet();
                    serialToTask.put(r, newTask);
                    return r;
                });
        if (accepted[0]) {
            advertiseStateChange(newTask);
        }
    }

    /** Trim history to maxHistorySize. */
    protected void trimHistory() {
        if (history.size() > maxHistorySize) {
            Iterator<Entry<Long, HasBasicTaskExec>> it = history.descendingIterator();
            while (history.size() >= maxHistorySize && it.hasNext()) {
                // Note: No need to clean up taskIdToSerial here.
                //       Before items are added to the history, their serial id mapping is removed.
                it.next();
                it.remove();
            }
        }
    }

    /**
     * Remove a task from tracking.
     *
     * @param task the task to remove
     * @return true if task was found and removed
     */
    public boolean remove(HasBasicTaskExec task) {
        long searchTaskId = getId(task);

        // Advertise state change before removing the task entry!
        Long foundTaskId = taskIdToSerial.get(searchTaskId);
        if (foundTaskId != null) {
            advertiseStateChange(task);
        }

        Long[] foundSerial = {null};
        taskIdToSerial.compute(
                searchTaskId,
                (_taskId, serial) -> {
                    if (serial != null) {
                        serialToTask.compute(
                                serial,
                                (s, oldTask) -> {
                                    if (oldTask == task) {
                                        foundSerial[0] = s;
                                        return null;
                                    }
                                    return oldTask;
                                });
                        return foundSerial[0] != null ? null : serial;
                    }
                    return serial;
                });

        boolean result = foundSerial[0] != null;
        if (result) {
            history.addFirst(Map.entry(foundSerial[0], task));
            trimHistory();
        }
        return result;
    }

    /** Clear all history. */
    public void clear() {
        history.clear();
    }

    @Override
    public String toString() {
        return "Active: " + serialToTask.size() + ", History: " + history.size() + "/" + maxHistorySize;
    }

    // --- ARQ Integration ---

    /** Symbol for TaskEventHistory in context. */
    public static final Symbol symTaskEventHistory = SystemARQ.allocSymbol("taskEventHistory");

    /**
     * Get TaskEventHistory from context.
     *
     * @param context the context
     * @return TaskEventHistory, or null if not registered
     */
    public static TaskEventHistory get(Context context) {
        return context == null ? null : context.get(symTaskEventHistory);
    }

    /**
     * Get or create TaskEventHistory in context.
     *
     * @param context the context
     * @return TaskEventHistory
     */
    public static TaskEventHistory getOrCreate(Context context) {
        TaskEventHistory result =
                context.computeIfAbsent(symTaskEventHistory, sym -> new TaskEventHistory());
        return result;
    }

    /**
     * Get TaskEventHistory from context or throw exception.
     *
     * @param context the context
     * @return TaskEventHistory
     * @throws NoSuchElementException if not registered
     */
    public static TaskEventHistory require(Context context) {
        TaskEventHistory result = get(context);
        if (result == null) {
            throw new NoSuchElementException("No TaskEventHistory registered in context");
        }
        return result;
    }
}
