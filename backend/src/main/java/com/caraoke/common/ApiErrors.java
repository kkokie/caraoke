package com.caraoke.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Small factory for the handful of HTTP errors services throw. */
public final class ApiErrors {

    private ApiErrors() { }

    public static ResponseStatusException notFound(String msg) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
    }

    public static ResponseStatusException conflict(String msg) {
        return new ResponseStatusException(HttpStatus.CONFLICT, msg);
    }

    public static ResponseStatusException badRequest(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    public static ResponseStatusException forbidden(String msg) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, msg);
    }

    /** An upstream service we depend on (e.g. the music catalog) failed. */
    public static ResponseStatusException badGateway(String msg, Throwable cause) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, msg, cause);
    }
}
