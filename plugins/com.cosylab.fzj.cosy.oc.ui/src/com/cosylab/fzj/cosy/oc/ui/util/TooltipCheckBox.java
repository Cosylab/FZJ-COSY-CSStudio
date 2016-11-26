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
