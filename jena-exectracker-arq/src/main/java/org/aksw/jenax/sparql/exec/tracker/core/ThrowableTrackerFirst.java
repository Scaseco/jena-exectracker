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

package org.aksw.jenax.sparql.exec.tracker.core;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** ThrowableTrackerFirst - Tracks only the first throwable reported, ignoring subsequent ones. */
public class ThrowableTrackerFirst implements ThrowableTracker {
    /** ThrowableTrackerFirst constructor. */
    public ThrowableTrackerFirst() {}

    /** The first throwable reported, or null if none. */
    protected Throwable throwable = null;

    /**
     * Report a throwable, keeping only the first one.
     *
     * @param throwable the throwable to report
     */
    @Override
    public void report(Throwable throwable) {
        if (this.throwable == null) {
            this.throwable = throwable;
        }
        // Ignore any throwables after the first
    }

    /**
     * Get an iterator over reported throwables (at most one).
     *
     * @return iterator of throwables
     */
    @Override
    public Iterator<Throwable> getThrowables() {
        return throwable == null ? Collections.emptyIterator() : List.of(throwable).iterator();
    }
}
