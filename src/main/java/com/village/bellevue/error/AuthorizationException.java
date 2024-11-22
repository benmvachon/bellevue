package com.village.bellevue.error;

public class AuthorizationException extends Exception {

    public AuthorizationException() {
        super("User not authorized.");
    }

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(Throwable cause) {
        super(cause);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
