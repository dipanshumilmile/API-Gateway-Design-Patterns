package services;

import gateway.Request;
import gateway.Response;
import tracking.RequestTracker;

/**
 * AnalyticsService provides usage statistics for the simulated API gateway.
 */
public class AnalyticsService implements Service {
    @Override
    public Response execute(Request request) {
        String summary = RequestTracker.getInstance().getUsageSummary();
        String message = "Analytics snapshot:\n" + summary;
        return new Response(200, message);
    }
}
