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

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.util.function.Consumer;

public class P6Application {

    /**
     * {@code "application/json"}
     */
    public static final String APPLICATION_JSON = "application/json";

    @Produces
    public Logger produceLogger(InjectionPoint injectionPoint) {
        return LoggerFactory.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
    }

    public static <T> Consumer<T> ok(RoutingContext rc) {
        return item ->  {
                if (item != null) {
                    rc.response().setStatusCode(HttpResponseStatus.OK.code()).end(Json.encodePrettily(item));
                } else {
                    rc.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
                }
            };
    }

    public static <T extends Throwable> Consumer<T> error(RoutingContext rc) {
        return failure -> {
            failure.printStackTrace();
            rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(failure.getMessage());
        };
    }
}