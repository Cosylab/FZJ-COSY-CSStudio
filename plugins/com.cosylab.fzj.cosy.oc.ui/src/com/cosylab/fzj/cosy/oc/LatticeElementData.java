package com.cosylab.fzj.cosy.oc;

/**
 * <code>LatticeElementData</code> represents the lattice element data read from the file.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class LatticeElementData {

    private final String name;
    private final double position;
    private final LatticeElementType type;

    /**
     * Constructs new lattice element data with the given name, position and type.
     *
     * @param name the lattice element name
     * @param position the lattice element position
     * @param type the lattice element type
     */
    public LatticeElementData(String name, double position, LatticeElementType type) {
        this.name = name;
        this.position = position;
        this.type = type;
    }

    /**
     * @return the lattice element name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the lattice element position.
     */
    public double getPosition() {
        return position;
    }

    /**
     * @return the lattice element type.
     */
    public LatticeElementType getType() {
        return type;
    }
}