/*
 * Copyright (c) 2017 Cosylab d.d.
 *
 * Contact Information:
 *   Cosylab d.d., Ljubljana, Slovenia
 *   http://www.cosylab.com
 */
package com.cosylab.fzj.cosy.oc.ui.model;

import com.cosylab.fzj.cosy.oc.LatticeElementData;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * <code>Corrector</code> represents s single corrector in the accelerator lattice. Corrector is identified by its
 * LatticeElementData and has a single value (correction kick).
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class Corrector extends LatticeElement {

    private final DoubleProperty correction = new SimpleDoubleProperty(this,"correction",0);

    /**
     * Constructs the new corrector with the given lattice element data.
     *
     * @param elementData the lattice element data
     */
    public Corrector(LatticeElementData elementData) {
        super(elementData);
    }

    /**
     * Returns the property providing the correction kick value that will be displayed on chart (either in mrad or mA).
     *
     * @return property providing the correction kick value
     */
    public DoubleProperty correctionProperty() {
        return correction;
    }
}
