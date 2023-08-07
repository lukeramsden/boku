package com.lukeramsden.boku.service.httpapi;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
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

        router.route().handler(context ->
        {
            String address = context.request().connection().remoteAddress().toString();
            MultiMap queryParams = context.queryParams();
            String name = queryParams.contains("name") ? queryParams.get("name") : "unknown";
            context.json(
                    new JsonObject()
                            .put("name", name)
                            .put("address", address)
                            .put("message", "Hello " + name + " connected from " + address)
            );
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