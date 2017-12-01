package com.vertxjava.blog.common.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.List;
import java.util.Optional;

/**
 * postgresql access
 *
 * @author jack
 * @create 2017-09-01 14:06
 **/
public class DatabaseAccessHelper {

    private final AsyncSQLClient client;
    private Logger logger = LoggerFactory.getLogger(DatabaseAccessHelper.class);

    public DatabaseAccessHelper(Vertx vertx, JsonObject config) {
        this.client = PostgreSQLClient.createShared(vertx, config);
    }

    protected Future<Void> update(String sql) {
        return getConnection().compose(connection -> {
            Future<Void> future = Future.future();
            connection.update(sql, ar -> {
                if (ar.succeeded())
                    future.complete();
                else
                    future.fail(ar.cause());
                connection.close();
            });
            return future;
        });
    }

    protected Future<Void> update(String sql, JsonArray params) {
        return getConnection().compose(connection -> {
            Future<Void> future = Future.future();
            connection.updateWithParams(sql, params, ar -> {
                if (ar.succeeded())
                    future.complete();
                else
                    future.fail(ar.cause());
                connection.close();
            });
            return future;
        });
    }

    protected Future<Void> execute(String sql) {
        return getConnection().compose(connection -> {
            Future<Void> future = Future.future();
            connection.execute(sql, ar -> {
                if (ar.succeeded())
                    future.complete();
                else
                    future.fail(ar.cause());
                connection.close();
            });
            return future;
        });
    }

    protected Future<Optional<JsonArray>> query(String sql) {
        return getConnection().compose(connection -> {
            Future<Optional<JsonArray>> future = Future.future();
            connection.query(sql, ar -> {
                List<JsonObject> list = ar.result().getRows();
                if (list.isEmpty()) {
                    future.complete(Optional.empty());
                } else {
                    JsonArray result = new JsonArray();
                    list.forEach(jo -> {
                        if (jo.containsKey("info")) {
                            result.add(new JsonObject(jo.getString("info")));
                        } else {
                            result.add(jo);
                        }
                    });
                    future.complete(Optional.of(result));
                }
                connection.close();
            });
            return future;
        });
    }

    protected Future<Optional<JsonArray>> query(String sql, JsonArray params) {
        return getConnection().compose(connection -> {
            Future<Optional<JsonArray>> future = Future.future();
            connection.queryWithParams(sql, params, ar -> {
                List<JsonObject> list = ar.result().getRows();
                if (list.isEmpty()) {
                    future.complete(Optional.empty());
                } else {
                    JsonArray result = new JsonArray();
                    list.forEach(jo -> {
                        if (jo.containsKey("info")) {
                            result.add(new JsonObject(jo.getString("info")));
                        } else {
                            result.add(jo);
                        }
                    });
                    future.complete(Optional.of(result));
                }
                connection.close();
            });
            return future;
        });
    }

    protected Future<Void> delete(String sql) {
        return update(sql);
    }

    protected Future<Void> delete(String sql, JsonArray params) {
        return update(sql, params);
    }

    protected Future<Void> insert(String sql) {
        return update(sql);
    }

    protected Future<Void> insert(String sql, JsonArray params) {
        return update(sql, params);
    }

    /*protected void executeNoResult(JsonArray params, String sql, Handler<AsyncResult<Void>> resultHandler) {
        client.getConnection(connHandler(resultHandler, connection -> {
            if (params == null) {
                connection.update(sql, r -> {
                    if (r.succeeded()) {
                        resultHandler.handle(Future.succeededFuture());
                    } else {
                        resultHandler.handle(Future.failedFuture(r.cause()));
                    }
                    connection.close();
                });
            } else {
                connection.updateWithParams(sql, params, r -> {
                    if (r.succeeded()) {
                        resultHandler.handle(Future.succeededFuture());
                    } else {
                        resultHandler.handle(Future.failedFuture(r.cause()));
                    }
                    connection.close();
                });
            }
        }));
    }

    protected <R> void execute(JsonArray params, String sql, R ret, Handler<AsyncResult<R>> resultHandler) {
        client.getConnection(connHandler(resultHandler, connection -> {
            if (params == null) {
                connection.update(sql, r -> {
                    if (r.succeeded()) {
                        resultHandler.handle(Future.succeededFuture(ret));
                    } else {
                        resultHandler.handle(Future.failedFuture(r.cause()));
                    }
                    connection.close();
                });
            } else {
                connection.updateWithParams(sql, params, r -> {
                    if (r.succeeded()) {
                        resultHandler.handle(Future.succeededFuture(ret));
                    } else {
                        resultHandler.handle(Future.failedFuture(r.cause()));
                    }
                    connection.close();
                });
            }

        }));
    }

    protected <K> Future<Optional<JsonObject>> retrieveOne(K param, String sql) {
        return getConnection()
                .compose(connection -> {
                    Future<Optional<JsonObject>> future = Future.future();
                    if (param == null) {
                        connection.query(sql, r -> {
                            if (r.succeeded()) {
                                List<JsonObject> resList = r.result().getRows();
                                if (resList == null || resList.isEmpty()) {
                                    future.complete(Optional.empty());
                                } else {
                                    JsonObject jo = resList.get(0);
                                    if (jo.containsKey("info")) {
                                        jo = new JsonObject(jo.getString("info"));
                                    }
                                    future.complete(Optional.of(jo));
                                }
                            } else {
                                future.fail(r.cause());
                            }
                            connection.close();
                        });
                    } else {
                        connection.queryWithParams(sql, new JsonArray().add(param), r -> {
                            if (r.succeeded()) {
                                List<JsonObject> resList = r.result().getRows();
                                if (resList == null || resList.isEmpty()) {
                                    future.complete(Optional.empty());
                                } else {
                                    JsonObject jo = resList.get(0);
                                    if (jo.containsKey("info")) {
                                        jo = new JsonObject(jo.getString("info"));
                                    }
                                    future.complete(Optional.of(jo));
                                }
                            } else {
                                logger.error(r.cause().getLocalizedMessage());
                                future.fail(r.cause());
                            }
                            connection.close();
                        });
                    }
                    return future;
                });
    }*/

    protected int calcPage(int page, int limit) {
        if (page <= 0)
            return 0;
        return limit * (page - 1);
    }

    /*protected Future<Optional<JsonArray>> retrieveByPage(JsonArray params, String sql) {
        return getConnection().compose(connection -> {
            Future<Optional<JsonArray>> future = Future.future();
            connection.queryWithParams(sql, params, r -> {
                if (r.succeeded()) {
                    List<JsonObject> resList = r.result().getRows();
                    if (resList == null || resList.isEmpty()) {
                        future.complete(Optional.empty());
                    } else {
                        JsonArray ja = new JsonArray();
                        resList.forEach(entity -> {
                            if (entity.containsKey("info"))
                                ja.add(new JsonObject(entity.getString("info")));
                            else
                                ja.add(entity);
                        });
                        future.complete(Optional.of(ja));
                    }
                } else {
                    future.fail(r.cause());
                }
                connection.close();
            });
            return future;
        });
    }

    protected Future<Optional<JsonArray>> retrieveAll(String sql) {
        return getConnection().compose(connection -> {
            Future<Optional<JsonArray>> future = Future.future();
            connection.query(sql, r -> {
                if (r.succeeded()) {
                    List<JsonObject> resList = r.result().getRows();
                    if (resList == null || resList.isEmpty()) {
                        future.complete(Optional.empty());
                    } else {
                        JsonArray ja = new JsonArray();
                        resList.forEach(entity -> {
                            if (entity.containsKey("info"))
                                ja.add(new JsonObject(entity.getString("info")));
                            else
                                ja.add(entity);
                        });
                        future.complete(Optional.of(ja));
                    }
                } else {
                    future.fail(r.cause());
                }
                connection.close();
            });
            return future;
        });
    }

    protected <K> void removeOne(K id, String sql, Handler<AsyncResult<Void>> resultHandler) {
        client.getConnection(connHandler(resultHandler, connection -> {
            JsonArray params = new JsonArray().add(id);
            connection.updateWithParams(sql, params, r -> {
                if (r.succeeded()) {
                    resultHandler.handle(Future.succeededFuture());
                } else {
                    resultHandler.handle(Future.failedFuture(r.cause()));
                }
                connection.close();
            });
        }));
    }*/

   /* protected void removeAll(String sql, Handler<AsyncResult<Void>> resultHandler) {
        client.getConnection(connHandler(resultHandler, connection -> {
            connection.update(sql, r -> {
                if (r.succeeded()) {
                    resultHandler.handle(Future.succeededFuture());
                } else {
                    resultHandler.handle(Future.failedFuture(r.cause()));
                }
                connection.close();
            });
        }));
    }*/

   /* *//**
     * 异步生成connection
     *
     * @return generated handler
     *//*
    private <R> Handler<AsyncResult<SQLConnection>> connHandler(Handler<AsyncResult<R>> h1, Handler<SQLConnection> h2) {
        return conn -> {
            if (conn.succeeded()) {
                final SQLConnection connection = conn.result();
                h2.handle(connection);
            } else {
                h1.handle(Future.failedFuture(conn.cause()));
            }
        };
    }*/

    /**
     * 异步获取SQLConnection
     *
     * @return SQLConnection
     */
    private Future<SQLConnection> getConnection() {
        Future<SQLConnection> future = Future.future();
        client.getConnection(future.completer());
        return future;
    }


}

