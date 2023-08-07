package com.lukeramsden.boku.service.httpapi;

import com.lukeramsden.boku.service.accountstore.AccountStoreServiceException;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

final class ResponseHelper
{
    private ResponseHelper()
    {
    }

    public static Handler<Throwable> errorMatcher(
            RoutingContext context,
            String internalServerErrorMsg,
            ErrorMatcherRow... rows
    )
    {
        return throwable ->
        {
            for (ErrorMatcherRow row : rows)
            {
                if (row.clazz.isAssignableFrom(throwable.getClass()))
                {
                    errResponse(context, row.responseStatusCode, row.responseMessage);
                    return;
                }

                errResponse(context, 500, internalServerErrorMsg);
            }
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

    public static final class ErrorMatcherRow
    {
        private final Class<? extends AccountStoreServiceException> clazz;
        private final int responseStatusCode;
        private final String responseMessage;

        private ErrorMatcherRow(
                Class<? extends AccountStoreServiceException> clazz,
                int responseStatusCode,
                String responseMessage
        )
        {
            this.clazz = clazz;
            this.responseStatusCode = responseStatusCode;
            this.responseMessage = responseMessage;
        }

        public static ErrorMatcherRow matchError(
                Class<? extends AccountStoreServiceException> clazz,
                int responseStatusCode,
                String responseMessage
        )
        {
            return new ErrorMatcherRow(clazz, responseStatusCode, responseMessage);
        }
    }
}
