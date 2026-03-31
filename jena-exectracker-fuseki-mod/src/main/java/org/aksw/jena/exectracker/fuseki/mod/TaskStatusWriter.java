/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aksw.jena.exectracker.fuseki.mod;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Map.Entry;

import org.aksw.jena.exectracker.arq.system.HasBasicTaskExec;
import org.aksw.jena.exectracker.arq.system.TaskEventHistory;
import org.apache.commons.lang3.exception.ExceptionUtils;

/** TaskStatusWriter - Writes execution tracker state as JSON. */
public class TaskStatusWriter {

    /** Maximum number of history entries to write. */
    protected int maxHistorySize;

    /** Whether abort is allowed. */
    protected boolean allowAbort;

    /**
     * TaskStatusWriter constructor.
     *
     * @param maxHistorySize caps the number of completed tasks to write out
     * @param allowAbort whether abort is allowed
     */
    public TaskStatusWriter(int maxHistorySize, boolean allowAbort) {
        super();
        this.maxHistorySize = maxHistorySize;
        this.allowAbort = allowAbort;
    }

    /**
     * Write complete status object to JSON writer.
     *
     * @param writer JSON writer
     * @param execTracker task event history
     * @throws IOException if JSON writing fails
     */
    public void writeStatusObject(JsonWriter writer, TaskEventHistory execTracker)
            throws IOException {
        writer.beginObject();
        writeStatusMembers(writer, execTracker);
        writer.endObject();
    }

    /**
     * Write status members (running and completed tasks) to JSON writer.
     *
     * @param writer JSON writer
     * @param execTracker task event history
     * @throws IOException if JSON writing fails
     */
    public void writeStatusMembers(JsonWriter writer, TaskEventHistory execTracker)
            throws IOException {
        writer.name("runningTasks");
        writer.beginArray();
        for (Entry<Long, HasBasicTaskExec> entry : execTracker.getActiveTasks().entrySet()) {
            long id = entry.getKey();
            HasBasicTaskExec item = entry.getValue();
            writer.beginObject();
            writeStartRecordMembers(writer, id, item);
            writeCanAbort(writer, allowAbort);
            writer.endObject();
        }
        writer.endArray();

        writer.name("completedTasks");
        writer.beginArray();
        Iterable<Entry<Long, HasBasicTaskExec>> recentHistory =
                () -> execTracker.getHistory().stream().limit(maxHistorySize).iterator();
        for (Entry<Long, HasBasicTaskExec> entry : recentHistory) {
            long id = entry.getKey();
            HasBasicTaskExec item = entry.getValue();
            writeCompletionRecordObject(writer, id, item);
        }
        writer.endArray();
    }

    /**
     * Write start record object to JSON writer.
     *
     * @param writer JSON writer
     * @param id task ID
     * @param item task item
     * @throws IOException if JSON writing fails
     */
    public static void writeStartRecordObject(JsonWriter writer, Long id, HasBasicTaskExec item)
            throws IOException {
        writer.beginObject();
        writeStartRecordMembers(writer, id, item);
        writer.endObject();
    }

    /**
     * Write start record members to JSON writer.
     *
     * @param writer JSON writer
     * @param id task ID
     * @param item task item
     * @throws IOException if JSON writing fails
     */
    public static void writeStartRecordMembers(JsonWriter writer, Long id, HasBasicTaskExec item)
            throws IOException {
        writer.name("type");
        writer.value("StartRecord");

        writer.name("requestId");
        // long id = System.identityHashCode(item);
        writer.value(id);

        writer.name("payload");
        writePayloadObject(writer, item);

        writer.name("timestamp");
        writer.value(item.getTaskInfo().getStartTime().map(Instant::toEpochMilli).orElse(-1l));
    }

    /**
     * Write canAbort field to JSON writer.
     *
     * @param writer JSON writer
     * @param canAbort whether task can be aborted
     * @throws IOException if JSON writing fails
     */
    public static void writeCanAbort(JsonWriter writer, Boolean canAbort) throws IOException {
        if (canAbort != null) {
            writer.name("canAbort");
            writer.value(canAbort);
        }
    }

    /**
     * Write payload object to JSON writer.
     *
     * @param writer JSON writer
     * @param item task item
     * @throws IOException if JSON writing fails
     */
    public static void writePayloadObject(JsonWriter writer, HasBasicTaskExec item)
            throws IOException {
        writer.beginObject();
        writePayloadMembers(writer, item);
        writer.endObject();
    }

    /**
     * Write payload members to JSON writer.
     *
     * @param writer JSON writer
     * @param item task item
     * @throws IOException if JSON writing fails
     */
    public static void writePayloadMembers(JsonWriter writer, HasBasicTaskExec item)
            throws IOException {
        // XXX Change to description
        String label = item.getTaskInfo().getLabel();
        writer.name("label");
        writer.value(label);
    }

    /**
     * Write completion record object to JSON writer.
     *
     * @param writer JSON writer
     * @param id task ID
     * @param item task item
     * @throws IOException if JSON writing fails
     */
    public static void writeCompletionRecordObject(JsonWriter writer, long id, HasBasicTaskExec item)
            throws IOException {
        writer.beginObject();
        writeCompletionRecordMembers(writer, id, item);
        writer.endObject();
    }

    /**
     * Write completion record members to JSON writer.
     *
     * @param writer JSON writer
     * @param id task ID
     * @param item task item
     * @throws IOException if JSON writing fails
     */
    public static void writeCompletionRecordMembers(JsonWriter writer, long id, HasBasicTaskExec item)
            throws IOException {
        writer.name("type");
        writer.value("CompletionRecord");

        writer.name("startRecord");
        writeStartRecordObject(writer, id, item);

        Throwable throwable = item.getTaskInfo().getThrowable().orElse(null);
        if (throwable != null) {
            String errorMessage = ExceptionUtils.getStackTrace(throwable);
            writer.name("error");
            writer.value(errorMessage);
        }

        writer.name("timestamp");
        writer.value(item.getTaskInfo().getFinishTime().map(Instant::toEpochMilli).orElse(-1l));
    }
}
