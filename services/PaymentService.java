package services;

import gateway.Request;
import gateway.Response;

/**
 * PaymentService simulates payment processing for the API Gateway.
 */
public class PaymentService implements Service {
    @Override
    public Response execute(Request request) {
        if (request.getData().isBlank()) {
            return new Response(404, "Payment details missing");
        }
        String message = String.format("Payment processed for %s: %s", request.getUsername(), request.getData());
        return new Response(200, message);
    }
}