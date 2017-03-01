/*
 * Copyright (c) 2017 Cosylab d.d.
 *
 * Contact Information:
 *   Cosylab d.d., Ljubljana, Slovenia
 *   http://www.cosylab.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Eclipse Public License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * For more information about the license please refer to the LICENSE
 * file included in the distribution.
 */
package com.cosylab.fzj.cosy.oc;

/**
 * <code>PreferencesException</code> is thrown when an error occurs while reading the preferences.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
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
