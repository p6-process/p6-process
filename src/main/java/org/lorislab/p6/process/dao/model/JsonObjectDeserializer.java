package org.lorislab.p6.process.dao.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.HashMap;

public class JsonObjectDeserializer extends JsonDeserializer<JsonObject> {

    private static final TypeReference<HashMap<String,Object>> TYPE_REF = new TypeReference<HashMap<String,Object>>() {};
    @Override
    public JsonObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
       return new JsonObject(p.getCodec().readValue(p, TYPE_REF));
    }
}
