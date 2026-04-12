package chain;

import commands.CommandInvoker;
import commands.ServiceCommand;
import factory.ServiceFactory;
import gateway.Request;
import gateway.Response;
import services.Service;

/**
 * RoutingHandler delegates the request to the correct backend service.
 */
public class RoutingHandler extends Handler {
    private final CommandInvoker invoker = new CommandInvoker();

    @Override
    public Response handle(Request request) {
        Service service = ServiceFactory.getService(request.getServiceType());
        if (service == null) {
            return new Response(404, "Service not found: " + request.getServiceType());
        }

        Response response = invoker.execute(new ServiceCommand(service, request));
        System.out.printf("[SERVICE] %s Executed%n", service.getClass().getSimpleName());
        return response;
    }
}