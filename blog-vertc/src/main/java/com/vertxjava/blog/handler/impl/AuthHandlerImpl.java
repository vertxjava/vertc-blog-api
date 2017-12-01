package com.vertxjava.blog.handler.impl;

import com.vertxjava.blog.common.service.DatabaseAccessHelper;
import com.vertxjava.blog.handler.AuthHandler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

public class AuthHandlerImpl extends DatabaseAccessHelper implements AuthHandler {
    private static final String GET_USER = "select info from vertc_blog_user";
    private Logger logger = LoggerFactory.getLogger(AuthHandlerImpl.class);
    private JWTAuth jwtAuth;

    public AuthHandlerImpl(JWTAuth jwtAuth, Vertx vertx, JsonObject config) {
        super(vertx, config.getJsonObject("pgsql.conn.conf"));
        this.jwtAuth = jwtAuth;
    }

    @Override
    public void handle(RoutingContext context) {
        JsonObject params = context.getBodyAsJson();
        query(GET_USER).setHandler(r -> {
            if (r.succeeded()) {
                if (r.result().isPresent()) {
                    JsonObject data = r.result().get().getJsonObject(0);
                    if (data.equals(params)) {
                        String token = jwtAuth.generateToken(new JsonObject()
                                .put("sub", "paulo"), new JWTOptions()
                                .setExpiresInSeconds(7200L));
                        context.response().putHeader("content-type", "application/json;charset=utf-8")
                                .end(new JsonObject().put("token", token).put("status", "success").encode());
                    } else {
                        context.response().end(new JsonObject().put("status", "failed").put("error", "用户名或密码错误").encode());
                    }
                } else {
                    context.response().end(new JsonObject().put("status", "failed").put("error", "用户名或密码错误").encode());
                }
            } else {
                context.fail(500);
            }
        });

    }
}
