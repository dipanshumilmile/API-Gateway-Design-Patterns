package chain;

import gateway.Request;
import gateway.Response;
import tracking.RequestTracker;

/**
 * RateLimitHandler limits the number of requests a single user can perform.
 */
public class RateLimitHandler extends Handler {
    private static final int MAX_REQUESTS_PER_USER = 5;

    @Override
    public Response handle(Request request) {
        int userRequests = RequestTracker.getInstance().incrementRequest(request.getUsername(), request.getServiceType());
        if (userRequests > MAX_REQUESTS_PER_USER) {
            System.out.printf("[RATE LIMIT] Denied (%d/%d requests)%n", userRequests, MAX_REQUESTS_PER_USER);
            return new Response(429, "Rate limit exceeded for " + request.getUsername());
        }

        System.out.printf("[RATE LIMIT] Allowed (%d/%d requests)%n", userRequests, MAX_REQUESTS_PER_USER);
        if (next != null) {
            return next.handle(request);
        }
        return new Response(404, "Rate limit passed but no next handler found");
    }
}
