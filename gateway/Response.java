package gateway;

/**
 * Response carries the API result status and message.
 */
public class Response {
    private final int statusCode;
    private final String message;

    public Response(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message != null ? message : "";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
