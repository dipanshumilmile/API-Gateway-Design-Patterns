package services;

import gateway.Request;
import gateway.Response;

/**
 * UserService handles user-related requests such as profile fetching.
 */
public class UserService implements Service {
    @Override
    public Response execute(Request request) {
        if (request.getData().isBlank()) {
            return new Response(404, "User request missing data");
        }
        String message = String.format("User Profile fetched for %s", request.getUsername());
        return new Response(200, message);
    }
}
