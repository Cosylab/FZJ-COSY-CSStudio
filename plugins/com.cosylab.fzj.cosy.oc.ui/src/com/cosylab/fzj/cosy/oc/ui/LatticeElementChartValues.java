package com.cosylab.fzj.cosy.oc.ui;

import com.cosylab.fzj.cosy.oc.LatticeElementType;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

public class LatticeElementChartValues {

    private ObjectProperty<LatticeElementType> elementType = new SimpleObjectProperty<>(this, "elementType");
    private DoubleProperty positionValue = new SimpleDoubleProperty(this, "positionValue");
    private DoubleProperty horizontalOrbitValue = new SimpleDoubleProperty(this, "horizontalOrbitValue");
    private DoubleProperty verticalOrbitValue = new SimpleDoubleProperty(this, "verticalOrbitValue");
    private DoubleProperty goldenHorizontalOrbitValue = new SimpleDoubleProperty(this, "goldenHorizontalOrbitValue");
    private DoubleProperty goldenVerticalOrbitValue = new SimpleDoubleProperty(this, "goldenVerticalOrbitValue");
    private DoubleProperty horizontalCorrectionValue = new SimpleDoubleProperty(this, "horizontalCorrectionValue");
    private DoubleProperty verticalCorrectionValue = new SimpleDoubleProperty(this, "verticalCorrectionValue");

    public ObjectProperty<LatticeElementType> getElementType() {
        return elementType;
    }

    public DoubleProperty getPositionValue() {
        return positionValue;
    }

    public DoubleProperty getHorizontalOrbitValue() {
        return horizontalOrbitValue;
    }

    public DoubleProperty getVerticalOrbitValue() {
        return verticalOrbitValue;
    }

    public DoubleProperty getGoldenHorizontalOrbitValue() {
        return goldenHorizontalOrbitValue;
    }

    public DoubleProperty getGoldenVerticalOrbitValue() {
        return goldenVerticalOrbitValue;
    }

    public DoubleProperty getHorizontalCorrectionValue() {
        return horizontalCorrectionValue;
    }

    public DoubleProperty getVerticalCorrectionValue() {
        return verticalCorrectionValue;
    }
}