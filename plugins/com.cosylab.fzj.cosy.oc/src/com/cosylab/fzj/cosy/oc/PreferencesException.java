package com.cosylab.fzj.cosy.oc;

public class PreferencesException extends RuntimeException {

    private static final long serialVersionUID = -3105716121426808758L;

    /**
     * Constructs a new exception.
     *
     * @param message the message of the exception
     * @param cause the root cause
     */
    public PreferencesException(String message, Throwable cause) {
        super(message,cause);
    }
}