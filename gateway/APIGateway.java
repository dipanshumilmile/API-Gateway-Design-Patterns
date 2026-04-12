package gateway;

import chain.AuthHandler;
import chain.Handler;
import chain.LoggingHandler;
import chain.RateLimitHandler;
import chain.ValidationHandler;
import chain.RoutingHandler;

/**
 * APIGateway acts as a facade for authentication, validation, logging, rate limiting, and routing.
 */
public class APIGateway {
    private final Handler entryPoint;

    public APIGateway() {
        AuthHandler authHandler = new AuthHandler();
        ValidationHandler validationHandler = new ValidationHandler();
        LoggingHandler loggingHandler = new LoggingHandler();
        RateLimitHandler rateLimitHandler = new RateLimitHandler();
        RoutingHandler routingHandler = new RoutingHandler();

        authHandler.setNext(validationHandler);
        validationHandler.setNext(loggingHandler);
        loggingHandler.setNext(rateLimitHandler);
        rateLimitHandler.setNext(routingHandler);

        this.entryPoint = authHandler;
    }

    public Response handleRequest(Request request) {
        if (request == null) {
            return new Response(404, "Invalid request: request object is null");
        }
        return entryPoint.handle(request);
    }
}
