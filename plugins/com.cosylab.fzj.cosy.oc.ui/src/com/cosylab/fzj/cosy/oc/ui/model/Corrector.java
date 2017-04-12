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
package com.cosylab.fzj.cosy.oc.ui.model;

import com.cosylab.fzj.cosy.oc.LatticeElementData;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * <code>Corrector</code> represents s single corrector in the accelerator lattice. Corrector is identified by its
 * LatticeElementData and has a single value (correction kick).
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class Corrector extends LatticeElement {

    private final DoubleProperty correction = new SimpleDoubleProperty(this,"correction",0);
    private final IntegerProperty cutoff = new SimpleIntegerProperty(this,"cutoff",0);
    private final IntegerProperty cutoffWish = new SimpleIntegerProperty(this,"cutoffWish",0);
    private final BooleanProperty cutoffDifferent = new SimpleBooleanProperty(this,"cutoffDifferent",false);

    /**
     * Constructs the new corrector with the given lattice element data.
     *
     * @param elementData the lattice element data
     */
    public Corrector(LatticeElementData elementData) {
        super(elementData);
        cutoffDifferent.bind(cutoff.isNotEqualTo(cutoffWish));
    }

    /**
     * Returns the property providing the correction kick value that will be displayed on chart (either in mrad or mA).
     *
     * @return property providing the correction kick value
     */
    public DoubleProperty correctionProperty() {
        return correction;
    }

    /**
     * Returns the property providing the steerer cutoff value. The value at which the steerer will be disabled.
     *
     * @return property providing the steerer cutoff value
     */
    public IntegerProperty cutoffProperty() {
        return cutoff;
    }

    /**
     * Returns the property providing the wish for steerer cutoff value (the value being set).
     *
     * @return property providing the steerer cutoff wish value
     */
    public IntegerProperty cutoffWishProperty() {
        return cutoffWish;
    }

    /**
     * Returns the property describing whether the wish and the actual cut off are different (true) or
     * the same (false).
     *
     * @return true if actual and wished cut off values differ or true if they are equal
     */
    public ReadOnlyBooleanProperty cutoffDifferentProperty() {
        return cutoffDifferent;
    }

    @Override
    public void refresh() {
        super.refresh();
        cutoffWish.set(cutoff.get());
    }
}
