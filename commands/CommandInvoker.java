package commands;

import gateway.Response;

/**
 * CommandInvoker executes commands for service actions.
 */
public class CommandInvoker {
    public Response execute(Command command) {
        return command.execute();
    }
}
