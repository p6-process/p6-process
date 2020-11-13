package org.lorislab.p6.process.dao.model;

import io.vertx.mutiny.sqlclient.Row;
import org.lorislab.p6.process.message.Message;
import org.lorislab.vertx.sql.mapper.SqlMapper;

@SqlMapper
public interface ProcessTokenMapper {

    ProcessToken map(Row row);
}
