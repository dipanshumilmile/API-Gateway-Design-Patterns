package gateway;

/**
 * Request represents a client request with user details and service payload.
 */
public class Request {
    private final String username;
    private final String role;
    private final String serviceType;
    private final String data;

    public Request(String username, String role, String serviceType, String data) {
        this.username = username != null ? username.trim() : "";
        this.role = role != null ? role.trim().toLowerCase() : "";
        this.serviceType = serviceType != null ? serviceType.trim().toLowerCase() : "";
        this.data = data != null ? data.trim() : "";
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getData() {
        return data;
    }
}
