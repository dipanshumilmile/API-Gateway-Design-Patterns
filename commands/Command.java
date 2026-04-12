package commands;

import gateway.Response;

/**
 * Command defines the execution contract for service operations.
 */
public interface Command {
    Response execute();
}
