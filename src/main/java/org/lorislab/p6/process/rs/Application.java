/*
 * Copyright 2019 lorislab.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lorislab.p6.process.rs;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Transaction;
import org.lorislab.quarkus.log.cdi.LogParam;

import java.util.function.Consumer;

public class Application {

    /**
     * {@code "application/json"}
     */
    public static final String APPLICATION_JSON = "application/json";

    public interface ResponseStatus {
        int OK = 200;
        int ACCEPTED = 202;
        int BAD_REQUEST = 400;
        int NOT_FOUND = 404;
        int INTERNAL_SERVER_ERROR = 500;
    }

    public static <T> Consumer<T> accepted(RoutingContext rc) {
        return response(rc, ResponseStatus.ACCEPTED);
    }

    public static <T> Consumer<T> ok(RoutingContext rc) {
        return response(rc, ResponseStatus.OK);
    }

    public static <T> Consumer<T> response(RoutingContext rc, int code) {

        return item ->  {
                if (item != null) {
                    rc.response().putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                            .setStatusCode(code).end(Json.encodePrettily(item));
                } else {
                    rc.response().setStatusCode(ResponseStatus.NOT_FOUND).end();
                }
            };
    }

    public static <T extends Throwable> Consumer<T> error(RoutingContext rc) {
        return failure -> {
            failure.printStackTrace();
            rc.response().setStatusCode(ResponseStatus.INTERNAL_SERVER_ERROR).end(failure.getMessage());
        };
    }

    @LogParam(assignableFrom = {RoutingContext.class})
    public static String logRoutingContext(Object message) {
        RoutingContext r = (RoutingContext) message;
        return r.normalisedPath();
    }

    @LogParam(assignableFrom = {Transaction.class})
    public static String logTransaction(Object object) {
        return "tx:" + object.hashCode();
    }

    @LogParam(assignableFrom = {Row.class})
    public static String logRow(Object object) {
        return "row:" + object.hashCode();
    }
}
