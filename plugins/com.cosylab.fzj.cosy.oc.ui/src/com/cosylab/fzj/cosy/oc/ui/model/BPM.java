package com.cosylab.fzj.cosy.oc.ui.model;

import com.cosylab.fzj.cosy.oc.LatticeElementData;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * <code>BPM</code> represents the bpm in the lattice. It is extended from the <code>LatticeElement</code> and provides
 * properties for providing horizontal orbit, vertical orbit, golden horizontal orbit or golden vertical orbit values.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 *
 */
public class BPM extends LatticeElement {

    private DoubleProperty horizontalOrbit = new SimpleDoubleProperty(this, "horizontalOrbit");
    private DoubleProperty verticalOrbit = new SimpleDoubleProperty(this, "verticalOrbit");
    private DoubleProperty goldenHorizontalOrbit = new SimpleDoubleProperty(this, "goldenHorizontalOrbit");
    private DoubleProperty goldenVerticalOrbit = new SimpleDoubleProperty(this, "goldenVerticalOrbit");

    /**
     * Constructs the new BPM with the given lattice element data.
     *
     * @param elementData the lattice element data
     */
    public BPM(LatticeElementData elementData) {
        super(elementData);
    }

    /**
     * @return the property providing the horizontal orbit value.
     */
    public DoubleProperty horizontalOrbitProperty() {
        return horizontalOrbit;
    }

    /**
     * @return the property providing the vertical orbit value.
     */
    public DoubleProperty verticalOrbitProperty() {
        return verticalOrbit;
    }

    /**
     * @return the property providing the golden horizontal orbit value.
     */
    public DoubleProperty goldenHorizontalOrbitProperty() {
        return goldenHorizontalOrbit;
    }

    /**
     * @return the property providing the golden vertical orbit value.
     */
    public DoubleProperty goldenVerticalOrbitProperty() {
        return goldenVerticalOrbit;
    }
}