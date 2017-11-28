package com.vertxjava.blog.verticle;

import com.vertxjava.blog.common.BaseVerticle;
import com.vertxjava.blog.handler.ArticleHandler;
import com.vertxjava.blog.handler.ArticleIndexHandler;
import com.vertxjava.blog.handler.AuthHandler;
import com.vertxjava.blog.handler.CategoryHandler;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class VertcVerticle extends BaseVerticle {

    private static JWTAuth jwtAuth;
    private Logger logger = LoggerFactory.getLogger(VertcVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        createJwt();
        final Router router = Router.router(vertx);
        enableCorsSupport(router);
        configRouter(router);
        createHttpServer(router, config().getString("http.address"), config().getInteger("http.port")).setHandler(r -> {
            if (r.succeeded()) {
                logger.info("server start success,listen port " + config().getInteger("http.port"));
                startFuture.complete();
            } else {
                logger.error("server start failed,cause is " + r.cause().getMessage());
                startFuture.fail(r.cause());
            }
        });
    }

    private void configRouter(Router router) {
        router.route().handler(BodyHandler.create());
        router.route("/api/index/article/*").handler(ArticleIndexHandler.create(vertx, config()));
        router.route("/api/manage/*").handler(JWTAuthHandler.create(jwtAuth));
        router.post("/api/auth").handler(AuthHandler.create(jwtAuth, vertx, config()));
        router.route("/api/manage/article/*").handler(ArticleHandler.create(vertx, config()));
        router.route("/api/manage/category/*").handler(CategoryHandler.create(vertx,config()));
        router.route("/api/*").failureHandler(context -> context.response().setStatusCode(context.statusCode()).end());
    }

    private void createJwt() {
        JWTAuthOptions config = new JWTAuthOptions()
                .setKeyStore(new KeyStoreOptions()
                        .setPath("keystore.jceks")
                        .setPassword("secret"));
        jwtAuth = JWTAuth.create(vertx, config);
    }

}

