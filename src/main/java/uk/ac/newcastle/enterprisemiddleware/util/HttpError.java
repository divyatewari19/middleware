package uk.ac.newcastle.enterprisemiddleware.util;

import org.jboss.resteasy.reactive.ClientWebApplicationException;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to Handle generic HTTP error messages
 * */
public final class HttpError {
    public static final int BAD_REQUEST = 400;
    public static final int NOT_FOUND = 404;
    public static final int CONFLICT = 409;
    public static final int SERVICE_UNAVAILABLE = 503;
//    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final String FLIGHTBOOKING = "Flight Booking";
    public static final String TAXIBOOKING = "Taxi Booking";
    public static final String HOTELBOOKING = "Hotel Booking";

    public static final Map<Integer, Response.Status> errorToResponseStatusMap;
//    public static final Map<Integer, String> errorToMessageMap;
//    public static final Map<ServiceType, String> serviceToStringMap;
    static {
        errorToResponseStatusMap = Map.of(BAD_REQUEST, Response.Status.BAD_REQUEST,
                NOT_FOUND, Response.Status.NOT_FOUND,
                CONFLICT, Response.Status.CONFLICT,
//                INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR,
                SERVICE_UNAVAILABLE, Response.Status.SERVICE_UNAVAILABLE);
    }

    private static String getExceptionMessage(String serviceType, int errorCode) {
        String errorMessage;
        switch (errorCode) {
            case BAD_REQUEST: errorMessage = "Bad Request, status code 400. Please check the request data.";
                break;
            case NOT_FOUND: errorMessage = "Not Found, status code 404. Please check the request";
                break;
            case SERVICE_UNAVAILABLE: errorMessage = "Service down, status code 503. Please try again after sometime";
                break;
            case CONFLICT:
                errorMessage = "Conflict, status code 409. Booking supplied in request body conflicts with an existing Booking. Please try with another date or commodity";
                break;
            default: errorMessage = "Something went wrong!";
        }

        if (serviceType != null) {
            return "[ " + serviceType + " ] - " + errorMessage;
        }

        return errorMessage;
    }

    /**
     * Helper method to throw handled exception with reasons if exists, else throw generic exception with message
     *
     * @param reasonKey name of the reason for the exception, to show a generic message without a reason pass null
     * @param reasonMsg String message for the exception
     * @param e Exception
     * */
    public static RestServiceException throwServiceException(String serviceType, String reasonKey, String reasonMsg, ClientWebApplicationException e) throws RestServiceException {
        int errorCode = e.getResponse().getStatus();
        Response.Status responseStatus = errorToResponseStatusMap.get(errorCode);
        if (serviceType == null) {
            serviceType = "";
        }
        // unhandled exception
        if (responseStatus == null ) {
            String errorMsg = "[ " + serviceType + " ] - " + e.getCause().getMessage();
            throw new RestServiceException(errorMsg, e);
        }

        String errorMsg = getExceptionMessage(serviceType, errorCode);
        // Pass reason to exception if exists, else thow a generic error  message
        if (reasonKey != null) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put(reasonKey, reasonMsg);
            throw new RestServiceException(errorMsg, responseObj, responseStatus, e);
        } else if (errorMsg != null){
            throw new RestServiceException(errorMsg, responseStatus);
        } else  {
            errorMsg = "[ " + serviceType + " ] - " + e.getCause().getMessage();
            throw new RestServiceException(errorMsg, e);
        }
    }
}
