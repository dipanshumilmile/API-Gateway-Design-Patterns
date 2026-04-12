package tracking;

import gateway.Request;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * RequestTracker is a singleton that tracks request counts across the API Gateway.
 */
public class RequestTracker {
    private static final RequestTracker INSTANCE = new RequestTracker();
    private int totalRequests;
    private final Map<String, Integer> userRequestCounts = new HashMap<>();
    private final Map<String, Integer> serviceRequestCounts = new HashMap<>();

    private RequestTracker() {
    }

    public static RequestTracker getInstance() {
        return INSTANCE;
    }

    public synchronized int incrementRequest(String username, String serviceType) {
        if (username == null || username.isBlank()) {
            username = "unknown";
        }
        totalRequests++;
        userRequestCounts.put(username, userRequestCounts.getOrDefault(username, 0) + 1);
        serviceRequestCounts.put(serviceType, serviceRequestCounts.getOrDefault(serviceType, 0) + 1);
        return userRequestCounts.get(username);
    }

    public synchronized int getTotalRequests() {
        return totalRequests;
    }

    public synchronized int getUserRequestCount(String username) {
        return userRequestCounts.getOrDefault(username, 0);
    }

    public synchronized String getUsageSummary() {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add("Total requests: " + totalRequests);
        joiner.add("Requests per user:");
        userRequestCounts.forEach((user, count) -> joiner.add("  - " + user + ": " + count));
        joiner.add("Service usage:");
        serviceRequestCounts.forEach((service, count) -> joiner.add("  - " + service + ": " + count));
        return joiner.toString();
    }
}
