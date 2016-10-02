package com.cosylab.fzj.cosy.oc.ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class OrbitCorrectionResultsEntry {

    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final DoubleProperty min = new SimpleDoubleProperty(this, "min");
    private final DoubleProperty max = new SimpleDoubleProperty(this, "max");
    private final DoubleProperty avg = new SimpleDoubleProperty(this, "avg");
    private final DoubleProperty rms = new SimpleDoubleProperty(this, "rms");
    private final DoubleProperty std = new SimpleDoubleProperty(this, "std");

    public OrbitCorrectionResultsEntry(String name) {
        this.name.set(name);
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    public DoubleProperty minProperty() {
        return min;
    }
    public DoubleProperty maxProperty() {
        return max;
    }
    public DoubleProperty avgProperty() {
        return avg;
    }
    public DoubleProperty rmsProperty() {
        return rms;
    }
    public DoubleProperty stdProperty() {
        return std;
    }
}