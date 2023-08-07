package com.lukeramsden.boku.service.httpapi;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

final class ResponseHelper
{
    private static final Logger LOGGER = LogManager.getLogger();

    private ResponseHelper()
    {
    }

    public static Handler<Throwable> errorMatcher(
            RoutingContext context,
            String internalServerErrorMsg,
            ErrorMatcherRow<?>... rows
    )
    {
        return throwable ->
        {
            for (ErrorMatcherRow<?> row : rows)
            {
                if (!row.clazz.isAssignableFrom(throwable.getClass()))
                {
                    continue;
                }

                final String errMsg = row.responseMessage.apply(row.clazz.cast(throwable));
                errResponse(context, row.responseStatusCode, errMsg);
                return;
            }

            LOGGER.error("Unhandled internal server error", throwable);
            errResponse(context, 500, internalServerErrorMsg);
        };
    }

    public static Handler<Void> noContentResponse(RoutingContext context)
    {
        return __ -> context.response().setStatusCode(204).end();
    }

    public static void errResponse(RoutingContext context, final int statusCode, final String errMsg)
    {
        context.response().setStatusCode(statusCode);
        context.json(errJson(errMsg));
    }

    public static JsonObject errJson(final String err)
    {
        return new JsonObject().put("err", err);
    }

    public static Handler<RoutingContext> internalServerError()
    {
        return context -> context.response().setStatusCode(500).end();
    }

    public static final class ErrorMatcherRow<T extends Throwable>
    {
        private final Class<T> clazz;
        private final int responseStatusCode;
        private final Function<Throwable, String> responseMessage;

        private ErrorMatcherRow(
                Class<T> clazz,
                int responseStatusCode,
                Function<Throwable, String> responseMessage
        )
        {
            this.clazz = clazz;
            this.responseStatusCode = responseStatusCode;
            this.responseMessage = responseMessage;
        }

        public static <T extends Throwable> ErrorMatcherRow<T> matchError(
                Class<T> clazz,
                int responseStatusCode,
                String responseMessage
        )
        {
            return new ErrorMatcherRow<>(clazz, responseStatusCode, __ -> responseMessage);
        }

        public static <T extends Throwable> ErrorMatcherRow<T> matchError(
                Class<T> clazz,
                int responseStatusCode,
                ResponseMessageSupplier<T> responseMessage
        )
        {
            return new ErrorMatcherRow<>(clazz, responseStatusCode, err ->
                    responseMessage.apply(clazz.cast(err)));
        }

        public interface ResponseMessageSupplier<T extends Throwable>
        {
            String apply(T throwable);
        }
    }
}
