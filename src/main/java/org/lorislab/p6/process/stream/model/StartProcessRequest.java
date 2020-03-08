package org.lorislab.p6.process.stream.model;

import java.util.Map;

public class StartProcessRequest {

    public String processId;

    public String processVersion;

    public String guid;

    public Map<String, Object> data;
}
