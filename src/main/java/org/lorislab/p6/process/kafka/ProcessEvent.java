package org.lorislab.p6.process.kafka;

import java.util.Map;

public class ProcessEvent {

    public ProcessEventType type;

    public String processInstanceId;

    public String processName;

    public String processVersion;

    public Map<String, Object> data;
}
