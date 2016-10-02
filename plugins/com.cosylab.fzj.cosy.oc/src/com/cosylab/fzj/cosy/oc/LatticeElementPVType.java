package com.cosylab.fzj.cosy.oc;

public enum LatticeElementPVType {

    HORIZONTAL_ORBIT("horizontalOrbit"),
    VERTICAL_ORBIT("verticalOrbit"),
    GOLDEN_HORIZONTAL_ORBIT("goldenHorizontalOrbit"),
    GOLDEN_VERTICAL_ORBIT("goldenVerticalOrbit"),
    HORIZONTAL_CORRECTOR_CORRECTION("horizontalCorrectorCorrection"),
    VERTICAL_CORRECTOR_CORRECTION("verticalCorrectorCorrection");

    private String name;

    private LatticeElementPVType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}