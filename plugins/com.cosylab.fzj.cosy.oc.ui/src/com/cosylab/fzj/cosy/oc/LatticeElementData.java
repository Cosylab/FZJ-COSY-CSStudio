package com.cosylab.fzj.cosy.oc;

import java.util.Objects;

/**
 * <code>LatticeElementData</code> represents the initial lattice element data for a single element.
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

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(name,position,type);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        LatticeElementData other = (LatticeElementData)obj;
        return Objects.equals(name,other.name) && Objects.equals(type,other.type)
                && Double.doubleToLongBits(position) == Double.doubleToLongBits(other.position);
    }
}
