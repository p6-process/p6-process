/*
 * Copyright 2019 lorislab.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lorislab.p6.process.stream.response;

import io.vertx.amqp.AmqpMessageBuilder;
import io.vertx.core.json.JsonObject;
import org.lorislab.p6.process.dao.model.ProcessToken;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface EventResponseService {

    void response(ProcessToken token, String correlationId);

    static AmqpMessageBuilder createMessageBuilder(ProcessToken token, String correlationId) {
        return io.vertx.amqp.AmqpMessage.create()
                .id(UUID.randomUUID().toString())
                .applicationProperties(EventResponseService.toJson(token))
                .correlationId(correlationId);
    }

    static JsonObject toJson(ProcessToken token) {
        Map<String, Object> tmp = new HashMap<>();
        tmp.put("guid", token.getGuid());
        tmp.put("name", token.getNodeName());
        tmp.put("processId", token.getProcessId());
        tmp.put("processVersion", token.getProcessVersion());
        return new JsonObject(tmp);
    }
}
