/*
 * Copyright (c) 2017 Cosylab d.d.
 *
 * Contact Information:
 *   Cosylab d.d., Ljubljana, Slovenia
 *   http://www.cosylab.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Eclipse Public License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * For more information about the license please refer to the LICENSE
 * file included in the distribution.
 */
package com.cosylab.fzj.cosy.oc;

import java.util.Arrays;

/**
 * <code>LatticeElementType</code> represents the lattice element type with the name.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public enum LatticeElementType {
    HORIZONTAL_BPM("bpmh"),
    VERTICAL_BPM("bpmv"),
    HORIZONTAL_CORRECTOR("horizontal"),
    VERTICAL_CORRECTOR("vertical"),
    HORIZONTAL_VERTICAL_CORRECTOR("horizontal/vertical");

    private String elementTypeName;

    /**
     * Constructs the lattice element type with the name.
     *
     * @param elementTypeName the lattice element type name
     */
    private LatticeElementType(String elementTypeName) {
        this.elementTypeName = elementTypeName;
    }

    /**
     * @return the lattice element type name.
     */
    public String getElementTypeName() {
        return elementTypeName;
    }

    /**
     * Returns true if this type represents a BPM or false if it represents a corrector.
     *
     * @return true if this is bpm or false if corrector
     */
    public boolean isBPM() {
        return this == HORIZONTAL_BPM || this == VERTICAL_BPM;
    }

    /**
     * Returns lattice element type with the given name.
     *
     * @param elementTypeName lattice element type name
     * @return lattice element with the given name.
     */
    public static LatticeElementType getElementType(String elementTypeName) {
        return Arrays.stream(values()).filter(v -> v.getElementTypeName().equals(elementTypeName)).findFirst()
                .orElse(null);
    }
}
