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

    public StringProperty getName() {
        return name;
    }
    public DoubleProperty getMin() {
        return min;
    }
    public DoubleProperty getMax() {
        return max;
    }
    public DoubleProperty getAvg() {
        return avg;
    }
    public DoubleProperty getRms() {
        return rms;
    }
    public DoubleProperty getStd() {
        return std;
    }
}