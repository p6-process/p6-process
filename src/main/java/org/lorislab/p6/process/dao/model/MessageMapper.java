package org.lorislab.p6.process.dao.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.mutiny.sqlclient.Row;
import org.lorislab.vertx.sql.mapper.SqlMapper;

@SqlMapper
public interface MessageMapper {

    Message map(Row row);

}
