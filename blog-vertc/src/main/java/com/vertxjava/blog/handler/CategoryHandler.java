package com.vertxjava.blog.handler;

import com.vertxjava.blog.handler.impl.CategoryHandlerImpl;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public interface CategoryHandler extends Handler<RoutingContext> {
    static CategoryHandler create(Vertx vertx, JsonObject config) {
        return new CategoryHandlerImpl(vertx, config);
    }
}
