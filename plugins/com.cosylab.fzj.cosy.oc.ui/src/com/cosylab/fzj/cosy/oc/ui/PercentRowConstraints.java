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
package com.cosylab.fzj.cosy.oc.ui;

import javafx.scene.layout.RowConstraints;

/**
 * <code>PercentRowConstraints</code> is a convenience class that exposes a {@link RowConstraints} constructor with a
 * percentage value.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
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
