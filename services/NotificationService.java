package services;

import gateway.Request;
import gateway.Response;

/**
 * NotificationService demonstrates an extendable new service.
 */
public class NotificationService implements Service {
    @Override
    public Response execute(Request request) {
        if (request.getData().isBlank()) {
            return new Response(404, "Notification content missing");
        }
        String message = String.format("Notification sent to %s: %s", request.getUsername(), request.getData());
        return new Response(200, message);
    }
}