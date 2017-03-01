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
package com.cosylab.fzj.cosy.oc.ui.util;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;

/**
 * <code>TooltipCheckBox</code> is a version of checkbox which provides the tooltip identical to the text of the check
 * box.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */
public class TooltipCheckBox extends CheckBox {

    /**
     * Construct a new check box with the given text and tooltip.
     *
     * @param text the text to use as the checkbox text and tooltip
     */
    public TooltipCheckBox(String text) {
        super(text);
        setTooltip(new Tooltip(text));
    }
}
