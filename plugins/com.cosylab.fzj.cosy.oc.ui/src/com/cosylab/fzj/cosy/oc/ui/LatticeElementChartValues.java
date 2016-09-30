package com.cosylab.fzj.cosy.oc.ui;

import com.cosylab.fzj.cosy.oc.LatticeElementType;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class LatticeElementChartValues {

    private ObjectProperty<LatticeElementType> elementType = new SimpleObjectProperty<>(this, "elementType");
    private ObjectProperty<Double> positionValue = new SimpleObjectProperty<>(this, "positionValue");
    private ObjectProperty<Double> horizontalOrbitValue = new SimpleObjectProperty<>(this, "horizontalOrbitValue");
    private ObjectProperty<Double> verticalOrbitValue = new SimpleObjectProperty<>(this, "verticalOrbitValue");
    private ObjectProperty<Double> goldenHorizontalOrbitValue = new SimpleObjectProperty<>(this, "goldenHorizontalOrbitValue");
    private ObjectProperty<Double> goldenVerticalOrbitValue = new SimpleObjectProperty<>(this, "goldenVerticalOrbitValue");
    private ObjectProperty<Double> horizontalCorrectionValue = new SimpleObjectProperty<>(this, "horizontalCorrectionValue");
    private ObjectProperty<Double> verticalCorrectionValue = new SimpleObjectProperty<>(this, "verticalCorrectionValue");

    public ObjectProperty<LatticeElementType> getElementType() {
        return elementType;
    }

    public ObjectProperty<Double> getPositionValue() {
        return positionValue;
    }

    public ObjectProperty<Double> getHorizontalOrbitValue() {
        return horizontalOrbitValue;
    }

    public ObjectProperty<Double> getVerticalOrbitValue() {
        return verticalOrbitValue;
    }

    public ObjectProperty<Double> getGoldenHorizontalOrbitValue() {
        return goldenHorizontalOrbitValue;
    }

    public ObjectProperty<Double> getGoldenVerticalOrbitValue() {
        return goldenVerticalOrbitValue;
    }

    public ObjectProperty<Double> getHorizontalCorrectionValue() {
        return horizontalCorrectionValue;
    }

    public ObjectProperty<Double> getVerticalCorrectionValue() {
        return verticalCorrectionValue;
    }
}