package factory;

import services.AnalyticsService;
import services.NotificationService;
import services.OrderService;
import services.PaymentService;
import services.Service;
import services.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * ServiceFactory returns service instances using a dynamic registry.
 */
public class ServiceFactory {
    private static final Map<String, Supplier<Service>> registry = new HashMap<>();

    static {
        registerService("user", UserService::new);
        registerService("payment", PaymentService::new);
        registerService("order", OrderService::new);
        registerService("notification", NotificationService::new);
        registerService("analytics", AnalyticsService::new);
    }

    public static void registerService(String serviceType, Supplier<Service> supplier) {
        registry.put(serviceType.toLowerCase(), supplier);
    }

    public static Service getService(String serviceType) {
        if (serviceType == null) {
            return null;
        }
        Supplier<Service> supplier = registry.get(serviceType.toLowerCase());
        return supplier == null ? null : supplier.get();
    }

    public static Set<String> getRegisteredServices() {
        return registry.keySet();
    }
}
