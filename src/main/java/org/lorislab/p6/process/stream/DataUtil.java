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

package org.lorislab.p6.process.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DataUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final ObjectWriter WRITER = OBJECT_MAPPER.writer();

    private static final ObjectReader READER = OBJECT_MAPPER.readerFor(Map.class);

    private DataUtil() {
        // private constructor
    }

    public static byte[] stringToJson(String data) {
        byte[] bytes = stringToByte(data);
        if (bytes == null) {
            return null;
        }
        try {
            Object tmp = READER.readValue(bytes);
            return WRITER.writeValueAsBytes(tmp);
        } catch (Exception ex) {
            throw new IllegalStateException("Error merge JSON object", ex);
        }
    }

    public static byte[] stringToByte(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }
        return data.getBytes(StandardCharsets.UTF_8);
    }

    public static String byteToString(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    public static byte[] merge(byte[] to, byte[] from) {
        try {
            if (to != null && from != null) {
                Map<String, Object> toMap = READER.readValue(to);
                Map<String, Object> fromMap = READER.readValue(from);
                toMap.putAll(fromMap);
                return WRITER.writeValueAsBytes(toMap);
            }
            if (to == null && from != null) {
                return from;
            }
            return to;
        } catch (Exception ex) {
            throw new IllegalStateException("Error merge JSON object", ex);
        }
    }

    public static byte[] serialize(Object data) {
        try {
            if (data != null) {
                return WRITER.writeValueAsBytes(data);
            }
            return null;
        } catch (Exception ex) {
            throw new IllegalStateException("Error serialize JSON object to byte array", ex);
        }
    }

    public static Map<String, Object> deserialize(byte[] data) {
        try {
            if (data != null) {
                return READER.readValue(data);
            }
            return null;
        } catch (Exception ex) {
            throw new IllegalStateException("Error serialize JSON object from byte array", ex);
        }
    }
}
