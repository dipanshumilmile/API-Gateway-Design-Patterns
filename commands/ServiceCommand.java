package commands;

import gateway.Request;
import gateway.Response;
import services.Service;

/**
 * ServiceCommand wraps a specific service operation as a command.
 */
public class ServiceCommand implements Command {
    private final Service service;
    private final Request request;

    public ServiceCommand(Service service, Request request) {
        this.service = service;
        this.request = request;
    }

    @Override
    public Response execute() {
        return service.execute(request);
    }
}
