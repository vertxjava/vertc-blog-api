package com.vertxjava.blog.handler.impl;

import com.vertxjava.blog.common.service.PgsqlAccessWrapper;
import com.vertxjava.blog.handler.ArticleIndexHandler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

public class ArticleIndexHandlerImpl extends PgsqlAccessWrapper implements ArticleIndexHandler {
    private Logger logger = LoggerFactory.getLogger(ArticleIndexHandlerImpl.class);
    private static final String SQL_ADD_ARTICLE = "insert into vertc_blog_article (info) values (?)";
    private static final String SQL_LIST_BY_PAGE = "select info from vertc_blog_article limit ? offset ?";
    private static final String SQL_FIND_BY_ID = "select info from vertc_blog_article where (info->>'id')::bigint = ?";
    public ArticleIndexHandlerImpl(Vertx vertx, JsonObject config) {
        super(vertx,config.getJsonObject("pgsql.conn.conf"));
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
            case "listByPage":
                listByPage(context);break;
            case "detail":
                detail(context);break;
            default:context.fail(404);


        }

    }

    private void listByPage(RoutingContext context){
        int page = Integer.parseInt(context.request().getParam("page"));
        int pageSize = 20;
        retrieveByPage(page,pageSize,SQL_LIST_BY_PAGE).setHandler(r -> {
            if (r.succeeded()){
                if (r.result().isPresent()){
                    context.response().end(r.result().get().encodePrettily());
                }else{
                    context.response().end(new JsonArray().encodePrettily());
                }
            }else{
                context.fail(500);
            }
        });
    }

    private void detail(RoutingContext context){
        long id = Long.parseLong(context.request().getParam("id"));
        retrieveOne(id,SQL_FIND_BY_ID).setHandler(r -> {
            if (r.succeeded()){
                if (r.result().isPresent()){
                    context.response().end(r.result().get().encodePrettily());
                }else{
                    context.response().end(new JsonObject().encodePrettily());
                }
            }else{
                context.fail(500);
            }
        });
    }


}
