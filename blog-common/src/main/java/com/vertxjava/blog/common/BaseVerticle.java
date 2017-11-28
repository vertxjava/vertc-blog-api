package com.vertxjava.blog.common;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;

import java.util.HashSet;
import java.util.Set;

/**
 * The base class contains some common methods
 *
 * @author jack
 * @create 2017-09-01 11:41
 **/
public class BaseVerticle extends AbstractVerticle{

    /**
     * create http server
     * @param router router
     * @param host host
     * @param port port
     * @return void
     */
    protected Future<Void> createHttpServer(Router router, String host, int port) {
        Future<HttpServer> httpServerFuture = Future.future();
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, host, httpServerFuture.completer());
        return httpServerFuture.map(r -> null);
    }

    /**
     * Enable CORS support
     * @param router router
     */
    protected void enableCorsSupport(Router router) {
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("Authorization");
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");
        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);
        router.route().handler(CorsHandler.create("*")
                .allowedHeaders(allowHeaders)
                .allowedMethods(allowMethods));
    }

    /**
     * enable local session
     * @param router router
     */
    protected void enableLocalSession(Router router) {
        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(
                LocalSessionStore.create(vertx)).setSessionTimeout(24*60*60*1000));
    }

    /**
     * enable clister session
     * @param router router
     */
    protected void enableClusteredSession(Router router) {
        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(
                ClusteredSessionStore.create(vertx)));
    }

}

