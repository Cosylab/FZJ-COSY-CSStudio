package com.cosylab.fzj.cosy.oc;

public class LatticeElementData {

    private final String name;
    private final double position;
    private final LatticeElementType type;

    public LatticeElementData(String name, double position, LatticeElementType type) {
        this.name = name;
        this.position = position;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public double getPosition() {
        return position;
    }

    public LatticeElementType getType() {
        return type;
    }
}