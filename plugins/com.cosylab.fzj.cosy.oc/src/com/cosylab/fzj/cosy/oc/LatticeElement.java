package com.cosylab.fzj.cosy.oc;

import java.util.HashMap;
import java.util.Map;

public class LatticeElement {

    private final String name;
    private final double position;
    private final LatticeElementType type;

    private Map<String, String> pvs = new HashMap<>();

    public LatticeElement(String name, double position, LatticeElementType type) {
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

    public void addPV(LatticeElementPVType type, String pv) {
        String pvName = new StringBuilder(name).append(':').append(type.toString()).toString();
        pvs.put(pvName, pv);
    }

    public void removePV(String name) {
        pvs.remove(name);
    }

    public Map<String, String> getPVs() {
        return pvs;
    }
}