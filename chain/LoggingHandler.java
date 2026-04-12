package chain;

import gateway.Request;
import gateway.Response;

import java.time.LocalDateTime;

/**
 * LoggingHandler records request details before routing.
 */
public class LoggingHandler extends Handler {
    @Override
    public Response handle(Request request) {
        LocalDateTime timestamp = LocalDateTime.now();
        System.out.printf("[LOG] %s | User: %s | Role: %s | Service: %s%n", timestamp, request.getUsername(), request.getRole().toUpperCase(), request.getServiceType());

        if (next != null) {
            return next.handle(request);
        }
        return new Response(404, "No route available after logging");
    }
}