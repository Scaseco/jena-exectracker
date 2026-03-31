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

package org.aksw.jena.exectracker.arq.unused;

import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.update.UpdateRequest;

/** UpdateExecBase - Base implementation for tracked SPARQL update execution. */
public abstract class UpdateExecBase implements UpdateExec {
    /** The string representation of the update request. */
    protected String updateRequestString;

    /** The parsed update request. */
    protected UpdateRequest updateRequest;

    /**
     * Create a new UpdateExecBase.
     *
     * @param updateRequest parsed update request
     * @param updateRequestString string representation of update request
     */
    public UpdateExecBase(UpdateRequest updateRequest, String updateRequestString) {
        super();
        // this.datasetGraph = datasetGraph;
        this.updateRequest = updateRequest;
        this.updateRequestString = updateRequestString;
    }

    //    public DatasetGraph getDatasetGraph() {
    //        return datasetGraph;
    //    }

    /**
     * Get parsed update request.
     *
     * @return update request
     */
    @Override
    public UpdateRequest getUpdateRequest() {
        return updateRequest;
    }

    /**
     * Get string representation of update request.
     *
     * @return update request string
     */
    @Override
    public String getUpdateRequestString() {
        return updateRequestString;
    }
}
