package com.cosylab.fzj.cosy.oc.ui.model;

import com.cosylab.fzj.cosy.oc.LatticeElementData;

/**
 * <code>LatticeElement</code> represents the lattice element.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 *
 */
public class LatticeElement {

    private LatticeElementData elementData;

    /**
     * Constructs the lattice element with the lattice element data.
     *
     * @param elementData the lattice element data
     */
    public LatticeElement(LatticeElementData elementData) {
        this.elementData = elementData;
    }

    /**
     * @return the lattice element data.
     */
    public LatticeElementData getElementData() {
        return elementData;
    }
}
