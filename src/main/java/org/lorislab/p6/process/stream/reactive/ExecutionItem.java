package org.lorislab.p6.process.stream.reactive;

import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.model.Node;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;
import io.vertx.mutiny.sqlclient.Transaction;

public class ExecutionItem {

    public Long jobId;

    public ProcessDefinitionRuntime pd;

    public ProcessToken token;

    public Node node;

    public Transaction tx;
}
