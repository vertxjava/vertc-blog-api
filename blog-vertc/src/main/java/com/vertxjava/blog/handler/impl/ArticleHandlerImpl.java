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
    private static final String SQL_LIST_BY_PAGE = "select info from vertc_blog_article order by info->'id' desc limit ? offset ?";
    private static final String SQL_FIND_BY_ID = "select info from vertc_blog_article where (info->>'id')::bigint = ?";
    private static final String SQL_UPDATE_ARTICLE = "update vertc_blog_article set info = ? where (info->>'id')::bigint = ?";
    private static final String SQL_DELETE = "delete from vertc_blog_article where (info::jsonb->>'id')::bigint = ?";
    private static final String SQL_SWITCH_SHOW_STATUS = "update vertc_blog_article set info = info || ? where (info->>'id')::bigint = ?";
    private static final String SQL_COUNT = "select count(*) from vertc_blog_article";
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
            case "delete":
                delete(context);
                break;
            case "switchShowStatus":
                switchShowStatus(context);
                break;
            case "count":
                count(context);
                break;
            default:
                context.fail(404);

        }

    }

    private void add(RoutingContext context) {
        JsonObject params = context.getBodyAsJson();
        params.put("id", new Date().getTime());
        params.put("reads", 0);
        params.put("showStatus", false);
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
        int pageSize = Integer.parseInt(context.request().getParam("pageSize"));
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
                    context.response().end(r.result().get().getJsonObject(0).encodePrettily());
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
        params.put("showStatus", false);
        update(SQL_UPDATE_ARTICLE, new JsonArray().add(params.encode()).add(params.getLong("id"))).setHandler(r -> {
            if (r.succeeded()) {
                context.response().end();
            } else {
                context.fail(500);
            }
        });
    }

    private void delete(RoutingContext context) {
        long id = Long.parseLong(context.request().getParam("id"));
        delete(SQL_DELETE, new JsonArray().add(id)).setHandler(r -> {
            if (r.succeeded()) {
                context.response().end();
            } else {
                context.fail(500);
            }
        });
    }

    private void switchShowStatus(RoutingContext context) {
        long id = Long.parseLong(context.request().getParam("id"));
        boolean show = Boolean.parseBoolean(context.request().getParam("showStatus"));
        update(SQL_SWITCH_SHOW_STATUS, new JsonArray().add(new JsonObject().put("showStatus",!show).encode()).add(id)).setHandler(r -> {
            if (r.succeeded()) {
                context.response().end();
            } else {
                context.fail(500);
            }
        });
    }
    private void count(RoutingContext context) {
        query(SQL_COUNT).setHandler(r -> {
            if (r.succeeded()) {
                if (r.result().isPresent()) {
                    context.response().end(r.result().get().getJsonObject(0).encodePrettily());
                } else {
                    context.response().end(new JsonObject().encodePrettily());
                }
            } else {
                logger.info(r.cause().getLocalizedMessage());
                context.fail(500);
            }
        });
    }



}
