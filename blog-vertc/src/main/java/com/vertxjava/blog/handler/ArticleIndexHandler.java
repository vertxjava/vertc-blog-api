package com.vertxjava.blog.handler;

import com.vertxjava.blog.handler.impl.ArticleHandlerImpl;
import com.vertxjava.blog.handler.impl.ArticleIndexHandlerImpl;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public interface ArticleIndexHandler extends Handler<RoutingContext> {
    static ArticleIndexHandler create(Vertx vertx, JsonObject config){
        return new ArticleIndexHandlerImpl(vertx,config);
    }
}
