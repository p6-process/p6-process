package org.lorislab.p6.process.dao.model;

import io.vertx.mutiny.sqlclient.Row;
import org.lorislab.vertx.sql.mapper.SqlMapper;

@SqlMapper
public interface ProcessInstanceMapper {

    ProcessInstance map(Row row);
}
