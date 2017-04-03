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
package com.cosylab.fzj.cosy.oc.ui.model;

/**
 * <code>SeriesType</code> represents all possible series plotted on any of the charts in the orbit correction view.
 * Each series is identified by an index and a human readable name.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 *
 */
public enum SeriesType {
    HORIZONTAL_ORBIT(0, "Horizontal Orbit"),
    VERTICAL_ORBIT(1, "Vertical Orbit"),
    GOLDEN_HORIZONTAL_ORBIT(2, "Golden Horizontal Orbit"),
    GOLDEN_VERTICAL_ORBIT(3, "Golden Vertical Orbit"),
    REFERENCE_HORIZONTAL_ORBIT(4, "Horizontal Reference Orbit"),
    REFERENCE_VERTICAL_ORBIT(5, "Vertical Reference Orbit"),
    DIFFERENCE_HORIZONTAL_ORBIT(6, "Horizontal Difference Orbit"),
    DIFFERENCE_VERTICAL_ORBIT(7, "Vertical Difference Orbit"),
    HORIZONTAL_CORRECTORS_CORRECTION(0, "Horizontal Correctors Correction"),
    VERTICAL_CORRECTORS_CORRECTION(1, "Vertical Correctors Correction"),
    BPM(0, "BPM"),
    HORIZONTAL_CORRECTORS(1, "Horizontal Correctors"),
    VERTICAL_CORRECTORS(2, "Vertical Correctors"),
    DIPOLES(3, "Dipoles"),
    QUADS(4, "Quadrupoles"),
    SEXTUPOLES(5, "Sextupoles");

    private int seriesIndex;
    private String seriesName;

    /**
     * Constructs the series type with the series index and name.
     *
     * @param seriesIndex the series index
     * @param seriesName the series name
     */
    private SeriesType(int seriesIndex, String seriesName) {
        this.seriesIndex = seriesIndex;
        this.seriesName = seriesName;
    }

    /**
     * Returns the unique series index. The index defines the index of the series in the chart dataset.
     *
     * @return series index
     */
    public int getSeriesIndex() {
        return seriesIndex;
    }

    /**
     * Returns the human readable series name.
     *
     * @return series name
     */
    public String getSeriesName() {
        return seriesName;
    }
}
