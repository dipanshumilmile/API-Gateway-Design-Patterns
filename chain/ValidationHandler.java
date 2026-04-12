package chain;

import gateway.Request;
import gateway.Response;

/**
 * ValidationHandler checks request payloads for emptiness and required fields.
 */
public class ValidationHandler extends Handler {
    @Override
    public Response handle(Request request) {
        if (request == null) {
            return new Response(404, "Invalid request received");
        }

        if (request.getUsername().isBlank()) {
            return new Response(400, "Validation failed: Username is required");
        }

        if (request.getRole().isBlank()) {
            return new Response(400, "Validation failed: Role is required");
        }

        if (request.getServiceType().isBlank()) {
            return new Response(400, "Validation failed: Service selection is required");
        }

        if (!request.getServiceType().equals("analytics") && request.getData().isBlank()) {
            return new Response(400, "Validation failed: Request details cannot be empty");
        }

        System.out.println("[VALIDATION] Input validated");
        if (next != null) {
            return next.handle(request);
        }
        return new Response(404, "Validation passed but no handler available");
    }
}
