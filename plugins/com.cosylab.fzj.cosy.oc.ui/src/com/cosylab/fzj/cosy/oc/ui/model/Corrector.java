package com.cosylab.fzj.cosy.oc.ui.model;

import com.cosylab.fzj.cosy.oc.LatticeElementData;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
* @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
*/
public class Corrector extends LatticeElement {

    private DoubleProperty horizontalCorrection = new SimpleDoubleProperty(this, "horizontalCorrection");
    private DoubleProperty verticalCorrection = new SimpleDoubleProperty(this, "verticalCorrection");

    public Corrector(LatticeElementData elementData) {
        super(elementData);
    }

    /**
     * @return the property providing the horizontal correction value.
     */
    public DoubleProperty horizontalCorrectionProperty() {
        return horizontalCorrection;
    }

    /**
     * @return the property providing the vertical correction value.
     */
    public DoubleProperty verticalCorrectionProperty() {
        return verticalCorrection;
    }
}