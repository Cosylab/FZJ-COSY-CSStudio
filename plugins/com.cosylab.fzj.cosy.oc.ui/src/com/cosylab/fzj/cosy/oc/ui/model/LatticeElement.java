package com.cosylab.fzj.cosy.oc.ui.model;

import com.cosylab.fzj.cosy.oc.LatticeElementData;

public class LatticeElement {

    private LatticeElementData elementData;

    public LatticeElement(LatticeElementData elementData) {
        this.elementData = elementData;
    }

    public LatticeElementData getElementData() {
        return elementData;
    }
}
