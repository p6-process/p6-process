package org.lorislab.p6.process.message;

import io.vertx.mutiny.sqlclient.Row;
import org.lorislab.vertx.sql.mapper.SqlMapper;

@SqlMapper
public interface MessageMapper {

    Message map(Row row);
}
