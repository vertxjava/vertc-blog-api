package com.vertxjava.blog;

import com.vertxjava.blog.verticle.VertcVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class App {

    private static Logger logger = LoggerFactory.getLogger(App.class);
    private static String deployId;
    private static Vertx vertx;

    public static void main(String[] args) {
        JsonObject conf = null;
        try {
            conf = new JsonObject(new Scanner(new File(args[0])).useDelimiter("\\A").next());
        } catch (DecodeException e) {
            logger.error("Configuration file does not contain a valid JSON object");
            return;
        } catch (FileNotFoundException e) {
            logger.error(e.toString());
            return;
        }
        vertx = Vertx.vertx();
        vertx.deployVerticle(VertcVerticle.class.getName(),new DeploymentOptions()
                .setInstances(new VertxOptions().getEventLoopPoolSize()).setConfig(conf), r -> {
            if (r.succeeded()){
                deployId = r.result();
            }
        });
    }

    /**
     * destroy the verticle
     */
    public static void destroy(){
        vertx.undeploy(deployId,r -> {
            if (r.succeeded()){
                logger.info("destroy VertcVerticle success");
                vertx.close();
                System.exit(0);
            }else{
                logger.error("destroy VertcVerticle failed,the cause is "+r.cause().getMessage());
            }
        });
    }

}

