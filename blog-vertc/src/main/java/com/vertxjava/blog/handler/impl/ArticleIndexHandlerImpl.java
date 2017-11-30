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

import java.util.Objects;

public class ArticleIndexHandlerImpl extends PgsqlAccessWrapper implements ArticleIndexHandler {
    private Logger logger = LoggerFactory.getLogger(ArticleIndexHandlerImpl.class);
    private static final String SQL_ADD_ARTICLE = "insert into vertc_blog_article (info) values (?)";
    private static final String SQL_LIST_BY_PAGE = "select info from vertc_blog_article order by info->'id' desc limit ? offset ?";
    private static final String SQL_LIST_BY_PAGE_CATEGORY  = "select info from vertc_blog_article where info->'category' = '\"?\"' order by info->'id' desc limit ? offset ?";
    private static final String SQL_FIND_BY_ID = "select info from vertc_blog_article where (info->>'id')::bigint = ?";
    private static final String SQL_COUNT = "select count(*) from vertc_blog_article";
    //private static final String SQL_UPDATE_READS = "update vertc_blog_article set info=jsonb_set(info,'{reads}','2',true) where (info->>'id')::bigint = 1511785189743;";
    private static final String SQL_CURRENT_READS = "select info from vertc_blog_article where (info::jsonb->>'id')::bigint = ?";
    private static final String SQL_UPDATE_READS = "update vertc_blog_article set info = ? where (info->>'id')::bigint = ?;";
    private static final String SQL_GROUP_BY_CATEGORY = "select count(*) as count,info::jsonb->>'category' as category from vertc_blog_article group by category order by count desc";
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
            case "categoryCount":categoryCount(context);break;
            default:context.fail(404);

        }

    }

    private void listByPage(RoutingContext context){
        int page = Integer.parseInt(context.request().getParam("page"));
        int pageSize = Integer.parseInt(context.request().getParam("pageSize"));
        String category = context.request().getParam("category");
        String sql = SQL_LIST_BY_PAGE;
        JsonArray params = new JsonArray();
        if (!Objects.equals(category, "")){
            sql = SQL_LIST_BY_PAGE_CATEGORY;
            params.add(category).add(pageSize).add(calcPage(page, pageSize));
        }else{
            params.add(pageSize).add(calcPage(page, pageSize));
        }
        retrieveByPage(params,sql).setHandler(r -> {
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
        // 先查询当前阅读数
        final long id = Long.parseLong(context.request().getParam("id"));
        retrieveOne(id,SQL_CURRENT_READS).compose(json_reads -> {
            Future<Void> future = Future.future();
            JsonObject data = new JsonObject();
            int reads = 0;
            if (json_reads.isPresent()){
                reads =json_reads.get().getInteger("reads")+1;
                data = json_reads.get();
                data.put("reads",reads);
            }
            executeNoResult(new JsonArray().add(data.encode()).add(id),SQL_UPDATE_READS,r -> {
                if (r.succeeded())
                    future.complete();
                else
                    future.fail(r.cause());
            });
            return future;
        });
    }

    private void categoryCount(RoutingContext context){
        retrieveAll(SQL_GROUP_BY_CATEGORY).setHandler(r -> {
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

}
