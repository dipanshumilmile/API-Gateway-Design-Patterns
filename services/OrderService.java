package services;

import gateway.Request;
import gateway.Response;

/**
 * OrderService simulates order placement in the backend.
 */
public class OrderService implements Service {
    @Override
    public Response execute(Request request) {
        if (request.getData().isBlank()) {
            return new Response(404, "Order payload missing");
        }
        String message = String.format("Order placed by %s: %s", request.getUsername(), request.getData());
        return new Response(200, message);
    }
}