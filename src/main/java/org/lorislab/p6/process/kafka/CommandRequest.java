package org.lorislab.p6.process.kafka;

import java.util.Map;

public class CommandRequest {

    public String processInstanceId;

    public String processName;

    public String processVersion;

    public CommandType type;

    public Map<String, Object> data;
}
