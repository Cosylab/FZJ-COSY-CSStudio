/*
 * Copyright (c) 2017 Cosylab d.d.
 *
 * Contact Information:
 *   Cosylab d.d., Ljubljana, Slovenia
 *   http://www.cosylab.com
 */
package com.cosylab.fzj.cosy.oc.ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * <code>OrbitCorrectionResultsEntry</code> represents the orbit correction results table entry (row).
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class OrbitCorrectionResultsEntry {

    private final StringProperty name = new SimpleStringProperty(this,"name");
    private final DoubleProperty min = new SimpleDoubleProperty(this,"min");
    private final DoubleProperty max = new SimpleDoubleProperty(this,"max");
    private final DoubleProperty avg = new SimpleDoubleProperty(this,"avg");
    private final DoubleProperty rms = new SimpleDoubleProperty(this,"rms");
    private final DoubleProperty std = new SimpleDoubleProperty(this,"std");

    /**
     * Constructs a new results entry with the given name.
     *
     * @param name the entry name
     */
    public OrbitCorrectionResultsEntry(String name) {
        this.name.set(name);
    }

    /**
     * Returns the property that provides the name of this entry.
     *
     * @return property providing the entry name value
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * Returns the property that provides the minimum column value.
     *
     * @return property providing the entry min value
     */
    public DoubleProperty minProperty() {
        return min;
    }

    /**
     * Returns the property that provides the maximum column value.
     *
     * @return property providing the entry max value
     */
    public DoubleProperty maxProperty() {
        return max;
    }

    /**
     * Returns the property that provides the average column value.
     *
     * @return property providing the entry avg value
     */
    public DoubleProperty avgProperty() {
        return avg;
    }

    /**
     * Returns the property that provides the root mean square column value.
     *
     * @return property providing the entry rms value
     */
    public DoubleProperty rmsProperty() {
        return rms;
    }

    /**
     * Returns the property that provides the standard deviation column value.
     *
     * @return property providing the entry std value
     */
    public DoubleProperty stdProperty() {
        return std;
    }
}
