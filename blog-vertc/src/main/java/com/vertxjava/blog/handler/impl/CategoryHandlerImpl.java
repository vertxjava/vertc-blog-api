package com.vertxjava.blog.handler.impl;

import com.vertxjava.blog.common.service.DatabaseAccessHelper;
import com.vertxjava.blog.handler.CategoryHandler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CategoryHandlerImpl extends DatabaseAccessHelper implements CategoryHandler {

    private static final String SQL_ALL = "select info from vertc_blog_category";
    private static final String SQL_INSERT = "insert into vertc_blog_category (info) values (?)";
    private static final String SQL_LIST_BY_PAGE = "select info from vertc_blog_category order by info->'id' desc limit ? offset ?";
    private static final String SQL_FIND_BY_ID = "select info from vertc_blog_category where (info->>'id')::bigint = ?";
    private static final String SQL_UPDATE_ARTICLE = "update vertc_blog_category set info = ? where (info->>'id')::bigint = ?";
    private static final String SQL_DELETE = "delete from vertc_blog_category where (info::jsonb->>'id')::bigint = ?";
    private static final String SQL_COUNT = "select count(*) from vertc_blog_category";
    private Logger logger = LoggerFactory.getLogger(CategoryHandlerImpl.class);

    public CategoryHandlerImpl(Vertx vertx, JsonObject config) {
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
            case "all":
                all(context);
                break;
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
            case "count":
                count(context);
                break;
            default:
                context.fail(404);
        }
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

    private void all(RoutingContext context) {
        query(SQL_ALL).setHandler(r -> {
            if (r.succeeded()) {
                if (r.result().isPresent()) {
                    context.response().end(r.result().get().encodePrettily());
                } else {
                    context.response().end(new JsonArray().encode());
                }
            } else {
                context.fail(500);
                logger.error(r.cause());
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

    private void add(RoutingContext context) {
        JsonObject params = context.getBodyAsJson();
        params.put("id", new Date().getTime());
        params.put("createDate", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        update(SQL_INSERT, new JsonArray().add(params.encode())).setHandler(r -> {
            if (r.succeeded()) {
                context.response().end();
            } else {
               context.fail(500);
            }
        });
    }

}
