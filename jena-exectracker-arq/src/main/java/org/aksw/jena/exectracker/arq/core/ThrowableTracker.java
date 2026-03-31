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

package org.aksw.jena.exectracker.arq.core;

import java.util.Iterator;
import java.util.Optional;

/** ThrowableTracker - Interface for tracking throwables encountered during execution. */
public interface ThrowableTracker {
    /**
     * Report a throwable that was encountered.
     *
     * @param throwable the throwable to report
     */
    void report(Throwable throwable);

    /**
     * Get all reported throwables.
     *
     * @return iterator of throwables
     */
    Iterator<Throwable> getThrowables();

    /**
     * Get the first throwable if any.
     *
     * @return optional first throwable
     */
    default Optional<Throwable> getFirstThrowable() {
        Iterator<Throwable> it = getThrowables();
        Throwable throwable = it.hasNext() ? it.next() : null;
        return Optional.ofNullable(throwable);
    }
}
