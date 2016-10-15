package com.cosylab.fzj.cosy.oc.ui.model;

/**
 * <code>SeriesType</code> represents the type of the series with the index and name.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 *
 */
public enum SeriesType {
    HORIZONTAL_ORBIT(0, "Horizontal Orbit"),
    VERTICAL_ORBIT(1, "Vertical Orbit"),
    GOLDEN_HORIZONTAL_ORBIT(2, "Golden Horizontal Orbit"),
    GOLDEN_VERTICAL_ORBIT(3, "Golden Vertical Orbit"),
    HORIZONTAL_CORRECTORS_CORRECTION(0, "Horizontal Correctors Correction"),
    VERTICAL_CORRECTORS_CORRECTION(1, "Vertical Correctors Correction"),
    BPM(0, "BPM"),
    HORIZONTAL_CORRECTORS(1, "Horizontal Correctors"),
    VERTICAL_CORRECTORS(2, "Vertical Correctors");

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
     * @return the series index.
     */
    public int getSeriesIndex() {
        return seriesIndex;
    }

    /**
     * @return the series name.
     */
    public String getSeriesName() {
        return seriesName;
    }
}
