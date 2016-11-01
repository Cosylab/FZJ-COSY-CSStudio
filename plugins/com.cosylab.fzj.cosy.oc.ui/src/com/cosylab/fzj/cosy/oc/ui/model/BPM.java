package com.cosylab.fzj.cosy.oc.ui.model;

import com.cosylab.fzj.cosy.oc.LatticeElementData;

import javafx.beans.property.DoubleProperty;
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

    private final DoubleProperty position = new SimpleDoubleProperty(this, "position", 0);
    private final DoubleProperty goldenPosition = new SimpleDoubleProperty(this, "goldenPosition", 0);

    /**
     * Constructs the new BPM with the given lattice element data.
     *
     * @param elementData the lattice element data
     */
    public BPM(LatticeElementData elementData) {
        super(elementData);
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
     * Property providing the golden orbit position at this BPM.
     *
     * @return property providing the golden orbit position value
     */
    public DoubleProperty goldenPositionProperty() {
        return goldenPosition;
    }
}
