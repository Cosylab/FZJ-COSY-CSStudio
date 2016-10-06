package com.cosylab.fzj.cosy.oc;

/**
 * <code>LatticeElementType</code> represents the lattice element type with the name.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public enum LatticeElementType {
    BPM("bpm"),
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
     * Returns lattice element type with the given name.
     *
     * @param elementTypeName lattice element type name
     *
     * @return lattice element with the given name.
     */
    public static LatticeElementType getElementType(String elementTypeName) {
        if (BPM.getElementTypeName().equals(elementTypeName)) {
            return BPM;
        } else if (HORIZONTAL_CORRECTOR.getElementTypeName().equals(elementTypeName)) {
            return HORIZONTAL_CORRECTOR;
        } else if (VERTICAL_CORRECTOR.getElementTypeName().equals(elementTypeName)) {
            return VERTICAL_CORRECTOR;
        } else if (HORIZONTAL_VERTICAL_CORRECTOR.getElementTypeName().equals(elementTypeName)) {
            return LatticeElementType.HORIZONTAL_VERTICAL_CORRECTOR;
        }
        return null;
    }
}