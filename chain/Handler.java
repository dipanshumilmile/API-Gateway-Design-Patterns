package chain;

import gateway.Request;
import gateway.Response;

/**
 * Handler is the base class for the chain of responsibility.
 */
public abstract class Handler {
    protected Handler next;

    public void setNext(Handler next) {
        this.next = next;
    }

    public abstract Response handle(Request request);
}
