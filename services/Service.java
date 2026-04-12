package services;

import gateway.Request;
import gateway.Response;

/**
 * Service defines the contract for backend services used by the API Gateway.
 */
public interface Service {
    Response execute(Request request);
}
