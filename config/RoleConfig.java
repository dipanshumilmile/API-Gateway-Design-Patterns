package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * RoleConfig loads role and permission rules from an external configuration file.
 */
public class RoleConfig {
    private static final Properties properties = new Properties();
    private static final Map<String, Set<String>> permissions = new HashMap<>();

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (InputStream stream = new FileInputStream("config/roles.properties")) {
            properties.load(stream);
            String rolesLine = properties.getProperty("roles", "admin,user,guest");
            for (String role : rolesLine.split(",")) {
                String cleanedRole = role.trim().toLowerCase();
                if (!cleanedRole.isEmpty()) {
                    String allowed = properties.getProperty(cleanedRole + ".services", "");
                    Set<String> serviceSet = new HashSet<>();
                    for (String service : allowed.split(",")) {
                        String cleanedService = service.trim().toLowerCase();
                        if (!cleanedService.isEmpty()) {
                            serviceSet.add(cleanedService);
                        }
                    }
                    permissions.put(cleanedRole, serviceSet);
                }
            }
        } catch (IOException e) {
            permissions.put("admin", new HashSet<>(Arrays.asList("user", "payment", "order", "notification", "analytics")));
            permissions.put("user", new HashSet<>(Arrays.asList("user", "payment", "order", "notification")));
            permissions.put("guest", new HashSet<>(Collections.singletonList("user")));
        }
    }

    public static boolean isValidRole(String role) {
        return role != null && permissions.containsKey(role.trim().toLowerCase());
    }

    public static boolean canAccess(String role, String serviceType) {
        if (role == null || serviceType == null) {
            return false;
        }
        Set<String> allowed = permissions.get(role.trim().toLowerCase());
        return allowed != null && allowed.contains(serviceType.trim().toLowerCase());
    }

    public static Set<String> getAllowedServices(String role) {
        return permissions.getOrDefault(role.trim().toLowerCase(), Collections.emptySet());
    }

    public static String getAvailableRoles() {
        return String.join(", ", permissions.keySet());
    }
}
