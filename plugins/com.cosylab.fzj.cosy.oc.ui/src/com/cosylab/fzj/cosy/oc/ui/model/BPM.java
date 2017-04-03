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
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * <code>BPM</code> represents a single beam position monitor. Each monitor can be enabled or disabled and has two
 * associated values: the current position and the golden orbit position. The direction which this position monitor
 * measures is defined by the LatticeElementData.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 *
 */
public class BPM extends LatticeElement {

    private final DoubleProperty position = new SimpleDoubleProperty(this,"position",0);
    private final DoubleProperty referencePosition = new SimpleDoubleProperty(this,"referencePosition",0);
    private final DoubleProperty differencePosition = new SimpleDoubleProperty(this,"differencePosition",0);
    private final DoubleProperty goldenPosition = new SimpleDoubleProperty(this,"goldenPosition",0);
    private final DoubleProperty goldenPositionWish = new SimpleDoubleProperty(this,"goldenPositionWish",0);
    private final BooleanProperty goldenDifferent = new SimpleBooleanProperty(this,"goldenDifferent",false);
    private final BooleanProperty inhibited = new SimpleBooleanProperty(false);

    /**
     * Constructs the new BPM with the given lattice element data.
     *
     * @param elementData the lattice element data
     */
    public BPM(LatticeElementData elementData) {
        super(elementData);
        goldenDifferent.bind(goldenPosition.isNotEqualTo(goldenPositionWish));
    }

    /**
     * Property providing the position measured at this BPM.
     *
     * @return property providing the position value
     */
    public DoubleProperty positionProperty() {
        return position;
    }

    /**
     * Property providing the reference position at this BPM.
     *
     * @return property providing the reference position
     */
    public DoubleProperty referencePositionProperty() {
        return referencePosition;
    }

    /**
     * Property providing the difference between the reference and current position at this BPM.
     *
     * @return property providing the difference in position
     */
    public DoubleProperty differencePositionProperty() {
        return differencePosition;
    }

    /**
     * Return the property that specifies if the BPM is inhibited or not. When inhibited, golden position value changes
     * from remote server should not be made.
     *
     * @return the property specifying if the BPM is inhibited or not
     */
    public BooleanProperty inhibitedProperty() {
        return inhibited;
    }

    /**
     * Property providing the golden orbit position at this BPM.
     *
     * @return property providing the golden orbit position value
     */
    public DoubleProperty goldenPositionProperty() {
        return goldenPosition;
    }

    /**
     * Property providing the golden orbit position at this BPM.
     *
     * @return property providing the golden orbit position value
     */
    public DoubleProperty goldenPositionWishProperty() {
        return goldenPositionWish;
    }

    /**
     * Returns the property describing whether the wish and the actual golden orbit are different (true) or
     * the same (false).
     *
     * @return true if actual and wished golden orbit differ or true if they are equal
     */
    public ReadOnlyBooleanProperty goldenDifferentProperty() {
        return goldenDifferent;
    }

    /**
     * Refresh the golden position wish to match the golden position.
     */
    public void refreshGolden() {
        goldenPositionWish.set(goldenPosition.get());
    }
}
