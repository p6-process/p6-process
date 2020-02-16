package org.lorislab.p6.process.flow.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class ProcessDefinitionLoader {

    private static ObjectMapper MAPPER = new ObjectMapper();

    private static ObjectReader READER = MAPPER.readerFor(ProcessDefinitionModel.class);

    public static ProcessDefinitionModel load(String data) {
        ProcessDefinitionModel pd;
        try {
            pd = READER.readValue(data);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return pd;
    }
}
