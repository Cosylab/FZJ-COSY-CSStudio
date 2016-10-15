package com.cosylab.fzj.cosy.oc.ui.model;

import com.cosylab.fzj.cosy.oc.LatticeElementData;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * <code>Corrector</code> represents the corrector in the lattice. It is extended from the <code>LatticeElement</code>
 * and provides properties for providing horizontal correction, vertical correction or horizontal/vertical correction
 * values.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 *
 */
public class Corrector extends LatticeElement {

    private DoubleProperty horizontalCorrection = new SimpleDoubleProperty(this, "horizontalCorrection");
    private DoubleProperty verticalCorrection = new SimpleDoubleProperty(this, "verticalCorrection");
    private DoubleProperty horizontalVerticalCorrection = new SimpleDoubleProperty(this, "horizontalVerticalCorrection");

    /**
     * Constructs the new corrector with the given lattice element data.
     *
     * @param elementData the lattice element data
     */
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

    /**
     * @return the property providing the horizontal/vertical value.
     */
    public DoubleProperty horizontalVerticalCorrectionProperty() {
        return horizontalVerticalCorrection;
    }
}
