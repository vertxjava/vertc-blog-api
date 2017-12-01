package com.vertxjava.blog.handler.impl;

import com.vertxjava.blog.common.service.DatabaseAccessHelper;
import com.vertxjava.blog.handler.ArticleHandler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ArticleHandlerImpl extends DatabaseAccessHelper implements ArticleHandler {
    private static final String SQL_ADD_ARTICLE = "insert into vertc_blog_article (info) values (?)";
    private static final String SQL_LIST_BY_PAGE = "select info from vertc_blog_article limit ? offset ?";
    private static final String SQL_FIND_BY_ID = "select info from vertc_blog_article where (info->>'id')::bigint = ?";
    private static final String SQL_UPDATE_ARTICLE = "update vertc_blog_article set info = ? where (info->>'id')::bigint = ?";
    private Logger logger = LoggerFactory.getLogger(ArticleHandlerImpl.class);

    public ArticleHandlerImpl(Vertx vertx, JsonObject config) {
        super(vertx, config.getJsonObject("pgsql.conn.conf"));
    }

    @Override
    public void handle(RoutingContext context) {
        String uri = context.request().uri();
        String action = null;
        if (uri.contains("?")) {
            action = context.request().uri().substring(context.request().uri().lastIndexOf("/") + 1, context.request().uri().lastIndexOf("?"));
        } else {
            action = context.request().uri().substring(context.request().uri().lastIndexOf("/") + 1);
        }
        switch (action) {
            case "add":
                add(context);
                break;
            case "listByPage":
                listByPage(context);
                break;
            case "findByid":
                findById(context);
                break;
            case "update":
                update(context);
                break;
            default:
                context.fail(404);

        }

    }

    private void add(RoutingContext context) {
        JsonObject params = context.getBodyAsJson();
        params.put("id", new Date().getTime());
        params.put("reads", 0);
        params.put("createDate", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        insert(SQL_ADD_ARTICLE, new JsonArray().add(params.encode())).setHandler(r -> {
            if (r.succeeded()) {
                context.response().end();
            } else {
                context.fail(500);
            }
        });
    }

    private void listByPage(RoutingContext context) {
        int page = Integer.parseInt(context.request().getParam("page"));
        int pageSize = 20;
        query(SQL_LIST_BY_PAGE, new JsonArray().add(pageSize).add(calcPage(page, pageSize))).setHandler(r -> {
            if (r.succeeded()) {
                if (r.result().isPresent()) {
                    context.response().end(r.result().get().encodePrettily());
                } else {
                    context.response().end(new JsonArray().encodePrettily());
                }
            } else {
                context.fail(500);
            }
        });
    }

    private void findById(RoutingContext context) {
        long id = Long.parseLong(context.request().getParam("id"));
        query(SQL_FIND_BY_ID, new JsonArray().add(id)).setHandler(r -> {
            if (r.succeeded()) {
                if (r.result().isPresent()) {
                    context.response().end(r.result().get().encodePrettily());
                } else {
                    context.response().end(new JsonObject().encodePrettily());
                }
            } else {
                context.fail(500);
            }
        });
    }

    private void update(RoutingContext context) {
        JsonObject params = context.getBodyAsJson();
        update(SQL_UPDATE_ARTICLE, new JsonArray().add(params.encode()).add(params.getLong("id"))).setHandler(r -> {
            if (r.succeeded()) {
                context.response().end();
            } else {
                context.fail(500);
            }
        });
    }

}
