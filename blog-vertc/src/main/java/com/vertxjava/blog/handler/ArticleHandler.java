package com.vertxjava.blog.handler;

import com.vertxjava.blog.handler.impl.ArticleHandlerImpl;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public interface ArticleHandler extends Handler<RoutingContext> {
    static ArticleHandler create(Vertx vertx, JsonObject config){
        return new ArticleHandlerImpl(vertx,config);
    }
}
