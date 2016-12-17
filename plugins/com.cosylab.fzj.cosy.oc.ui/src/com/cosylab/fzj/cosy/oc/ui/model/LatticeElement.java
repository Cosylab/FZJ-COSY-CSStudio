package com.cosylab.fzj.cosy.oc.ui.model;

import java.util.Objects;

import com.cosylab.fzj.cosy.oc.LatticeElementData;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * <code>LatticeElement</code> represents a single element in the accelerator lattice. This can be either a corrector
 * magnet or a beam position monitor. The natural order of the elements is defined by their position in the lattice. The
 * elements with position closer to the beginning of the accelerator are smaller than the ones that are further away.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public abstract class LatticeElement implements Comparable<LatticeElement> {

    private final BooleanProperty enabledWish = new SimpleBooleanProperty(this, "enabledWish", true);
    private final BooleanProperty enabled = new SimpleBooleanProperty(this,"enabled",true);
    private final StringProperty name = new SimpleStringProperty(this,"name","");
    private final DoubleProperty location = new SimpleDoubleProperty(this,"location",0d);
    private final BooleanProperty enableDifferent = new SimpleBooleanProperty(this,"enableDifferent",false);
    private final LatticeElementData elementData;

    /**
     * Constructs the lattice element for the lattice element data. The parameter describes the initial values for this
     * element, which are later used only by equals and hash code methods. If the name or the location is changed via
     * the properties, the encapsulated element is not updated.
     *
     * @param elementData the lattice element data
     */
    public LatticeElement(LatticeElementData elementData) {
        this.elementData = elementData;
        this.locationProperty().set(this.elementData.getPosition());
        this.nameProperty().set(this.elementData.getName());
        enableDifferent.bind(enabled.isNotEqualTo(enabledWish));
    }

    /**
     * Returns the lattice element data that this element was constructed from.
     *
     * @return lattice element data
     */
    public LatticeElementData getElementData() {
        return elementData;
    }

    /**
     * Returns the property providing the name of this element.
     *
     * @return name property
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * Returns the property providing the location of this element along the z axis.
     *
     * @return location property
     */
    public DoubleProperty locationProperty() {
        return location;
    }

    /**
     * Returns the property providing the enable state value (true for enable and false for disabled).
     *
     * @return property providing the enabled state
     */
    public BooleanProperty enabledProperty() {
        return enabled;
    }

    /**
     * Returns the property providing the enable state value (true for enable and false for disabled). This property is
     * to be used by the UI when the user wants to enable or disable the element
     *
     * @return property providing the enabled state
     */
    public BooleanProperty enabledWishProperty() {
        return enabledWish;
    }

    /**
     * Returns the property which defines whether the enabled state and enabled wish state are equal or different.
     *
     * @return property providing the XOR between enabled and enabled wish
     */
    public BooleanProperty enableDifferentProperty() {
        return enableDifferent;
    }

    /**
     * Refreshes the state by writing the enable value to enable wish property.
     */
    public void refresh() {
        enabledWish.set(enabled.get());
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(LatticeElement o) {
        int c = Double.compare(locationProperty().get(),o.locationProperty().get());
        if (c == 0) {
            c = nameProperty().get().compareTo(o.nameProperty().get());
        }
        if (c == 0) {
            c = elementData.getType().compareTo(o.elementData.getType());
        }
        return c;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(elementData.getName(),elementData.getPosition(),elementData.getType());
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
        LatticeElement other = (LatticeElement)obj;
        return Objects.equals(elementData,other.elementData);
    }
}
