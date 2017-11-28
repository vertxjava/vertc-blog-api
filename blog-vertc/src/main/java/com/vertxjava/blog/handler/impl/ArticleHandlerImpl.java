package com.vertxjava.blog.handler.impl;

import com.vertxjava.blog.common.service.PgsqlAccessWrapper;
import com.vertxjava.blog.handler.ArticleHandler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

public class ArticleHandlerImpl extends PgsqlAccessWrapper implements ArticleHandler {
    private Logger logger = LoggerFactory.getLogger(ArticleHandlerImpl.class);
    private static final String SQL_ADD_ARTICLE = "insert into vertc_blog_article (info) values (?)";
    private static final String SQL_LIST_BY_PAGE = "select info from vertc_blog_article limit ? offset ?";
    private static final String SQL_FIND_BY_ID = "select info from vertc_blog_article where (info->>'id')::bigint = ?";
    private static final String SQL_UPDATE_ARTICLE = "update vertc_blog_article set info = ? where (info->>'id')::bigint = ?";

    public ArticleHandlerImpl(Vertx vertx, JsonObject config) {
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
            case "add":
                add(context);break;
            case "listByPage":
                listByPage(context);break;
            case "findByid":
                findById(context);break;
            case "update":
                update(context);break;
            default:context.fail(404);

        }

    }

    private void add(RoutingContext context) {
        JsonObject params = context.getBodyAsJson();
        params.put("id",new Date().getTime());
        params.put("reads",0);
        params.put("createDate",new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        executeNoResult(new JsonArray().add(params.encode()),SQL_ADD_ARTICLE,r -> {
           if (r.succeeded()){
               context.response().end();
           }else{
               context.fail(500);
           }
        });
    }

    private void listByPage(RoutingContext context){
        int page = Integer.parseInt(context.request().getParam("page"));
        int pageSize = 20;
        retrieveByPage(page,pageSize,SQL_LIST_BY_PAGE).setHandler(r -> {
            if (r.succeeded()){
                if (r.result().isPresent()){
                    JsonArray result = new JsonArray();
                    JsonArray ja = r.result().get();
                    ja.forEach(r1 -> {
                        JsonObject jo = (JsonObject)r1;
                        jo.put("content",jo.getString("content").replaceAll("\n","<br>"));
                        result.add(jo);
                    });
                    context.response().end(result.encodePrettily());
                }else{
                    context.response().end(new JsonArray().encodePrettily());
                }
            }else{
                context.fail(500);
            }
        });
    }

    private void findById(RoutingContext context){
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

    private void update(RoutingContext context){
        JsonObject params = context.getBodyAsJson();
        executeNoResult(new JsonArray().add(params.encode()).add(params.getLong("id")),SQL_UPDATE_ARTICLE,r -> {
            if (r.succeeded()){
                context.response().end();
            }else{
                context.fail(500);
            }
        });
    }


}
