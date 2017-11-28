package com.vertxjava.blog.handler;

import com.vertxjava.blog.handler.impl.AuthHandlerImpl;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;

public interface AuthHandler extends Handler<RoutingContext> {
    static AuthHandler create(JWTAuth jwtAuth, Vertx vertx, JsonObject config) {
        return new AuthHandlerImpl(jwtAuth, vertx, config);
    }
}
