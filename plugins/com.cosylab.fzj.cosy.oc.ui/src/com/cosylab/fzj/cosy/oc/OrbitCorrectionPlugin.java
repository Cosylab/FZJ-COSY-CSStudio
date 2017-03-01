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

import java.util.logging.Logger;

/**
 * <code>OrbitCorrectionPlugin</code> provides the plugin id and logger used by orbit correction.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class OrbitCorrectionPlugin {

    /** Plug-in ID */
    public static final String PLUGIN_ID = "com.cosylab.fzj.cosy.oc";
    /** The common logger */
    public static final Logger LOGGER = Logger.getLogger(OrbitCorrectionPlugin.class.getName());
}
