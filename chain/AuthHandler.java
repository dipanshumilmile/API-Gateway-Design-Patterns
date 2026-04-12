package chain;

import config.RoleConfig;
import gateway.Request;
import gateway.Response;

/**
 * AuthHandler verifies the user role and access permissions for each service.
 */
public class AuthHandler extends Handler {
    @Override
    public Response handle(Request request) {
        if (request == null) {
            return new Response(404, "Invalid request provided");
        }

        String role = request.getRole();
        if (role.isBlank() || !RoleConfig.isValidRole(role)) {
            System.out.printf("[AUTH] User: %s | Role: %s -> Invalid role%n", request.getUsername(), role);
            return new Response(403, "Unauthorized role");
        }

        if (!RoleConfig.canAccess(role, request.getServiceType())) {
            System.out.printf("[AUTH] User: %s | Role: %s -> Access Denied%n", request.getUsername(), role.toUpperCase());
            return new Response(403, "Unauthorized Access");
        }

        System.out.printf("[AUTH] User: %s | Role: %s -> Access Granted%n", request.getUsername(), role.toUpperCase());
        if (next != null) {
            return next.handle(request);
        }
        return new Response(404, "No next handler to process request");
    }
}
