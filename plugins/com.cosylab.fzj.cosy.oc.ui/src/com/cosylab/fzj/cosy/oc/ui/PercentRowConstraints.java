package com.cosylab.fzj.cosy.oc.ui;

import javafx.scene.layout.RowConstraints;

/**
 * <code>PercentRowConstraints</code> is a convenience class that exposes a {@link RowConstraints} constructor with a
 * percentage value.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public class PercentRowConstraints extends RowConstraints {

    /**
     * Constructs an array of <code>n</code> constraints, where all constraints has equal percentage values, totalling
     * 100.
     *
     * @param n the number of constraints to construct
     * @return an array of constraints with equal percentage values
     */
    public static RowConstraints[] createEqualsConstraints(int n) {
        RowConstraints[] constraints = new PercentRowConstraints[n];
        for (int i = 0; i < n; i++) {
            constraints[i] = new PercentRowConstraints(100. / n);
        }
        return constraints;
    }

    /**
     * Construct a new row constraints object with the given percent value.
     *
     * @param percent the height percentage
     */
    public PercentRowConstraints(double percent) {
        setPercentHeight(percent);
    }

}
