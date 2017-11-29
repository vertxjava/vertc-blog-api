package com.vertxjava.blog.handler.impl;

import com.vertxjava.blog.common.service.PgsqlAccessWrapper;
import com.vertxjava.blog.handler.ArticleIndexHandler;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

public class ArticleIndexHandlerImpl extends PgsqlAccessWrapper implements ArticleIndexHandler {
    private Logger logger = LoggerFactory.getLogger(ArticleIndexHandlerImpl.class);
    private static final String SQL_ADD_ARTICLE = "insert into vertc_blog_article (info) values (?)";
    private static final String SQL_LIST_BY_PAGE = "select info from vertc_blog_article order by info->'id' desc limit ? offset ?";
    private static final String SQL_FIND_BY_ID = "select info from vertc_blog_article where (info->>'id')::bigint = ?";
    private static final String SQL_COUNT = "select count(*) from vertc_blog_article";
    private static final String SQL_READ = "update vertc_blog_article set info=jsonb_set(info,'{reads}','2',true) where (info->>'id')::bigint = 1511785189743;";
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
            case "listByPage":listByPage(context);break;
            case "detail":detail(context);break;
            case "count":count(context);break;
            case "read":read(context);break;
            default:context.fail(404);


        }

    }

    private void listByPage(RoutingContext context){
        int page = Integer.parseInt(context.request().getParam("page"));
        int pageSize = Integer.parseInt(context.request().getParam("pageSize"));
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

    private void count(RoutingContext context){
        retrieveOne(null,SQL_COUNT).setHandler(r -> {
            if (r.succeeded()){
                if (r.result().isPresent()){
                    context.response().end(r.result().get().encodePrettily());
                }else{
                    context.response().end(new JsonObject().encodePrettily());
                }
            }else{
                logger.info(r.cause().getLocalizedMessage());
                context.fail(500);
            }
        });
    }

    private void read(RoutingContext context){
        // 由于sql问题，暂时无法实现这个功能。
        //executeNoResult(null,);
    }


}
