package com.vertxjava.blog.handler.impl;

import com.vertxjava.blog.common.service.DatabaseAccessHelper;
import com.vertxjava.blog.handler.CategoryHandler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

public class CategoryHandlerImpl extends DatabaseAccessHelper implements CategoryHandler {

    private static final String ALL = "select info from vertc_blog_category";
    private Logger logger = LoggerFactory.getLogger(CategoryHandlerImpl.class);

    public CategoryHandlerImpl(Vertx vertx, JsonObject config) {
        super(vertx, config.getJsonObject("pgsql.conn.conf"));
    }

    @Override
    public void handle(RoutingContext context) {
        String uri = context.request().uri();
        String action = null;
        if (uri.contains("?")){
            action = context.request().uri().substring(context.request().uri().lastIndexOf("/") + 1,context.request().uri().lastIndexOf("?"));
        }else{
            action = context.request().uri().substring(context.request().uri().lastIndexOf("/") + 1);
        }
        switch (action) {
            case "all":
                all(context);
                break;
        }
    }

    private void all(RoutingContext context) {
        query(ALL).setHandler(r ->{
            if (r.succeeded()) {
                if (r.result().isPresent()){
                    context.response().end(r.result().get().encodePrettily());
                }else{
                    context.response().end(new JsonArray().encode());

                }
            } else {
                context.fail(500);
                logger.error(r.cause());
            }
        });
    }

}
