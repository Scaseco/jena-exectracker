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

import java.time.Instant;
import java.util.Optional;

/**
 * Interface defining the contract for basic task information tracking. Provides read-only access to
 * task lifecycle state, timing information, and error status.
 */
public interface BasicTaskInfo {
    /**
     * The state of the task.
     *
     * @return the current task state
     */
    TaskState getTaskState();

    /**
     * Time stamp for when the task object was created.
     *
     * @return the creation time
     */
    Instant getCreationTime();

    /**
     * Time stamp for when the task was started. Returns empty if not started yet.
     *
     * @return the start time, or empty if not started
     */
    Optional<Instant> getStartTime();

    /**
     * Time stamp for when the task completed. Returns empty if not finished yet.
     *
     * @return the finish time, or empty if not finished
     */
    Optional<Instant> getFinishTime();

    /**
     * Time stamp for when the task was cancelled. Returns empty if not aborted.
     *
     * @return the abort time, or empty if not aborted
     */
    Optional<Instant> getAbortTime();

    /**
     * Return a description suitable for presentation to users. This might be a less technical
     * description than what is returned by toString().
     *
     * @return the user-friendly label
     */
    String getLabel();

    /**
     * Get the current status message.
     *
     * @return the status message
     */
    String getStatusMessage();

    /**
     * If this method returns a non-null result then the task is considered to be failing or to have
     * failed. A non-null result does not imply that the task has already reached TERMINATED state.
     *
     * @return the tracked throwable, or empty if no error
     */
    Optional<Throwable> getThrowable();

    /**
     * Whether abort has been called.
     *
     * @return true if abort has been called, false otherwise
     */
    default boolean isAborting() {
        return getAbortTime().isPresent();
    }

    /**
     * Whether the task has reached the TERMINATED state.
     *
     * @return true if the task is terminated, false otherwise
     */
    default boolean isTerminated() {
        TaskState state = getTaskState();
        return TaskState.TERMINATED.equals(state);
    }
}
