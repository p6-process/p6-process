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

    public static Tuple tuple(Object data) {
        return Tuple.of(data);
    }

    public static Tuple tuple(Object ...data) {
        return Tuple.tuple(Arrays.asList(data));
    }

    public interface For extends Supplier<String> {
    }

    public static For update() {
        return () -> "UPDATE";
    }
    public static For skipLocked() {
        return () -> "SKIP LOCKED";
    }

    public static String all() {
        return "*";
    }

    public static String count(String... columns) {
        return "COUNT(" + String.join(",", columns) + ")";
    }

    public static String count() {
        return count(all());
    }

    public static String value(String value) {
        return "'" + value + "'";
    }

    public static String alias(String column, String alias) {
        return column + " as " + alias;
    }

    public interface Order extends Supplier<String> {
    }

    public static Order asc(String column) {
        return () -> column + " ASC";
    }

    public interface Op extends Supplier<String> {
    }

    public static Op equal(String column) {
        return () -> column + "=";
    }

    public static Op equal(String column, String sql) {
        return () -> column + " = (" + sql + ")";
    }

    public static Op not(String column) {
        return () -> column + "<>";
    }

    public static Update update(String table) {
        return new Update(table);
    }

    public static Insert insert(String ... columns) {
        return new Insert(columns);
    }

    public static Select select(String col, String ... cols) {
        return new Select(col, cols);
    }

    public static Delete delete() {
        return new Delete();
    }

    protected static abstract class SqlOp {
        protected String sql(String sql) {
            log.debug("SQL: {}", sql);
            return sql;
        }
        public abstract String build();
        public abstract String build(String table);
    }

    public static class Select extends SqlOp {

        private String columns;

        private String table;

        private String where;

        private String orderBy;

        private String extend;

        private String limit;

        Select(String col, String ... cols) {
            select(col, cols);
        }

        public Select select(String col, String ... cols) {
            if (cols.length > 0) {
                this.columns = col + "," + String.join(",", Arrays.asList(cols));
            } else {
                this.columns = col;
            }
            return this;
        }

        @Override
        public String build() {
            return build(table);
        }

        @Override
        public String build(String table) {
            String sql = "SELECT " + columns + " FROM " + table;
            if (where != null) {
                sql += " WHERE " + where;
            }
            if (orderBy != null) {
                sql += " ORDER BY " + orderBy;
            }
            if (extend != null) {
                sql += " FOR " + extend;
            }
            if (limit != null) {
                sql += " LIMIT " + limit;
            }
            return sql(sql);
        }

        public Select from(String table) {
            this.table = table;
            return this;
        }

        public Select where(Op... ops) {
            where = IntStream.rangeClosed(1, ops.length)
                    .boxed()
                    .map(x -> ops[x-1].get() +"$" + x)
                    .collect(Collectors.joining(" AND "));
            return this;
        }

        public Select orderBy(Order ... orders) {
            this.orderBy = Arrays.stream(orders).map(Supplier::get).collect(Collectors.joining(","));
            return this;
        }

        public Select extend(For ... items) {
            this.extend = Arrays.stream(items).map(Supplier::get).collect(Collectors.joining(" "));
            return this;
        }

        public Select limit(String limit) {
            this.limit = limit;
            return this;
        }

        public Select limit(Long limit) {
           return limit("" + limit);
        }
    }

    public static class Update extends SqlOp {

        int count;

        String returning;

        String table;

        String columns;

        String where;

        Update(String table) {
            update(table);
        }

        @Override
        public String build() {
            return build(table);
        }

        @Override
        public String build(String table) {
            String sql = "UPDATE " + table + " SET " + columns;
            if (where != null) {
                sql += " WHERE " + where;
            }
            if (returning != null) {
                sql += " RETURNING " + returning;
            }
            return sql(sql);
        }

        public Update update(String table) {
            this.table = table;
            return this;
        }

        public Update set(String... columns) {
            this.count = columns.length;
            this.columns = IntStream.rangeClosed(1, count).boxed().map(x -> columns[x-1] +"=$" + x).collect(Collectors.joining(","));
            return this;
        }

        public Update where(Op... ops) {
            int begin = count + 1;
            where = IntStream.rangeClosed(begin, count + ops.length)
                    .boxed()
                    .map(x -> ops[x-begin].get() +"$" + x)
                    .collect(Collectors.joining(" AND "));
            return this;
        }

        public Update returning(String... returning) {
            this.returning = String.join(",", returning);
            return this;
        }
    }

    public static class Insert extends SqlOp {

        String columns;

        String values;

        String table;

        String returning;

        Insert(String... columns) {
            columns(columns);
        }

        @Override
        public String build(String table) {
            String sql = "INSERT INTO " + table + columns + " VALUES " + values;
            if (returning != null) {
                sql += " RETURNING " + returning;
            }
            return sql(sql);
        }

        @Override
        public String build() {
            return build(table);
        }

        public Insert into(String table) {
            this.table = table;
            return this;
        }

        public Insert columns(String... columns) {
            this.columns = Arrays.stream(columns).collect(Collectors.joining(",", " (", ")"));
            this.values = IntStream.rangeClosed(1, columns.length).boxed().map(x -> "$" + x).collect(Collectors.joining(",", "(", ")"));
            return this;
        }

        public Insert returning(String... returning) {
            this.returning = String.join(" ,", returning);
            return this;
        }
    }

    public static class Delete extends SqlOp {

        String table;

        String where;

        String returning;

        Delete() {
        }

        @Override
        public String build() {
            return build(table);
        }

        @Override
        public String build(String table) {
            String sql = "DELETE FROM " + table;
            if (where != null) {
                sql += " WHERE " + where;
            }
            if (returning != null) {
                sql += " RETURNING " + returning;
            }
            return sql(sql);
        }

        public Delete from(String table) {
            this.table = table;
            return this;
        }

        public Delete where(Op... ops) {
            where = IntStream.rangeClosed(1, ops.length)
                    .boxed()
                    .map(x -> ops[x-1].get() +"$" + x)
                    .collect(Collectors.joining(" AND "));
            return this;
        }

        public Delete where(Op op) {
            where = op.get();
            return this;
        }

        public Delete returning(String... returning) {
            this.returning = String.join(" ,", returning);
            return this;
        }
    }
}
