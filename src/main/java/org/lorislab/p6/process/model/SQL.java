package org.lorislab.p6.process.model;


import io.vertx.mutiny.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class SQL {

    public static Tuple tuple(Object ...data) {
        return Tuple.tuple(Arrays.asList(data));
    }

    public static Update update(String table) {
        return new Update(table);
    }

    public static Insert insert(String table) {
        return new Insert(table);
    }

    public static Select select(String table) {
        return new Select(table);
    }

    protected static class SqlOp {
        String sql;

        public String build() {
            log.info("SQL: " + sql);
            return sql;
        }
    }

    public static class Select extends SqlOp {

        String table;

        Select(String table) {
            this.table = table;
            sql = "SELECT ";
        }

        public Select from(String ... columns) {
            if (columns.length > 0) {
                sql += String.join(",", Arrays.asList(columns));
            } else {
                sql += "*";
            }
            sql += " FROM " + table;
            return this;
        }

        public Select where(String... columns) {
            sql += " WHERE ";
            sql += IntStream.rangeClosed(1, columns.length)
                    .boxed()
                    .map(x -> columns[x-1] +"=$" + x)
                    .collect(Collectors.joining(" AND "));
            return this;
        }

        public Select where(Op... ops) {
            sql += " WHERE ";
            sql += IntStream.rangeClosed(1, ops.length)
                    .boxed()
                    .map(x -> ops[x-1].get() +"$" + x)
                    .collect(Collectors.joining(" AND "));
            return this;
        }
    }

    public interface Op extends Supplier<String> {

    }

    public static Op equal(String column) {
        return () -> column + "=";
    }

    public static Op not(String column) {
        return () -> column + "<>";
    }

    public static class Update extends SqlOp {

        int count;

        Update(String table) {
            sql = "UPDATE " + table + " SET ";
        }

        public Update columns(String... columns) {
            count = columns.length;
            sql += IntStream.rangeClosed(1, count)
                    .boxed()
                    .map(x -> columns[x-1] +"=$" + x)
                    .collect(Collectors.joining(","));
            return this;
        }

        public Update where(String... columns) {
            sql += " WHERE ";
            int begin = count + 1;
            count = count + columns.length;
            sql += IntStream.rangeClosed(begin, count)
                    .boxed()
                    .map(x -> columns[x-begin] +"=$" + x)
                    .collect(Collectors.joining(" AND "));
            return this;
        }

        public Update returning(String... returning) {
            if (returning.length > 0) {
                sql += " RETURNING (";
                sql += String.join(",", Arrays.asList(returning));
                sql += ")";
            }
            return this;
        }
    }

    public static class Insert extends SqlOp {

        Insert(String table) {
            sql = "INSERT INTO " + table + " (";
        }

        public Insert columns(String... columns) {
            sql += String.join(",", Arrays.asList(columns));
            sql += ") VALUES (";
            sql += IntStream.rangeClosed(1, columns.length).boxed().map(x -> "$" + x).collect(Collectors.joining(","));
            sql += ")";
            return this;
        }

        public Insert returning(String... returning) {
            if (returning.length > 0) {
                sql += " RETURNING (";
                sql += String.join(",", Arrays.asList(returning));
                sql += ")";
            }
            return this;
        }
    }
}
