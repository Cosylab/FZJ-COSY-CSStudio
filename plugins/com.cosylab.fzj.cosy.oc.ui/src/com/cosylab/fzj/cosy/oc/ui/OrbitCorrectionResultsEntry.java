package com.cosylab.fzj.cosy.oc.ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * <code>OrbitCorrectionResultsEntry</code> represents the orbit correction results table entry.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 *
 */
public class OrbitCorrectionResultsEntry {

    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final DoubleProperty min = new SimpleDoubleProperty(this, "min");
    private final DoubleProperty max = new SimpleDoubleProperty(this, "max");
    private final DoubleProperty avg = new SimpleDoubleProperty(this, "avg");
    private final DoubleProperty rms = new SimpleDoubleProperty(this, "rms");
    private final DoubleProperty std = new SimpleDoubleProperty(this, "std");

    /**
     * Constructs the new corrector with the entry given name.
     *
     * @param name the entry name
     */
    public OrbitCorrectionResultsEntry(String name) {
        this.name.set(name);
    }

    /**
     * @return the property providing the entry name value.
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * @return the property providing the entry min value.
     */
    public DoubleProperty minProperty() {
        return min;
    }

    /**
     * @return the property providing the entry max value.
     */
    public DoubleProperty maxProperty() {
        return max;
    }

    /**
     * @return the property providing the entry avg value.
     */
    public DoubleProperty avgProperty() {
        return avg;
    }

    /**
     * @return the property providing the entry rms value.
     */
    public DoubleProperty rmsProperty() {
        return rms;
    }

    /**
     * @return the property providing the entry std value.
     */
    public DoubleProperty stdProperty() {
        return std;
    }
}