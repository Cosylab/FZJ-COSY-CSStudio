package com.cosylab.fzj.cosy.oc.ui;

import javafx.scene.layout.ColumnConstraints;

/**
 * <code>PercentColumnConstraints</code> is a convenience class that exposes a {@link ColumnConstraints} constructor
 * with a percentage value.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */
public class PercentColumnConstraints extends ColumnConstraints {

    /**
     * Constructs an array of <code>n</code> constraints, where all constraints has equal percentage values, totalling
     * 100.
     * 
     * @param n the number of constraints to construct
     * @return an array of constraints with equal percentage values
     */
    public static ColumnConstraints[] createEqualsConstraints(int n) {
        ColumnConstraints[] constraints = new PercentColumnConstraints[n];
        for (int i = 0; i < n; i++) {
            constraints[i] = new PercentColumnConstraints(100. / n);
        }
        return constraints;
    }

    /**
     * Construct a new column constraints object with the given percent value.
     * 
     * @param percent the width percentage
     */
    public PercentColumnConstraints(double percent) {
        setPercentWidth(percent);
    }
}
