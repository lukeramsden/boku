package com.lukeramsden.boku.service.httpapi;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class HttpApiServiceVerticle extends AbstractVerticle
{
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void start()
    {
        Router router = Router.router(vertx);

        router.get("/healthz").handler(context ->
        {
            context.response().putHeader("content-type", "text/plain");
            context.response().end("healthy");
        });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8888)
                .onSuccess(server ->
                        LOGGER.info(
                                "HTTP server started on port " + server.actualPort()
                        )
                );
    }
}