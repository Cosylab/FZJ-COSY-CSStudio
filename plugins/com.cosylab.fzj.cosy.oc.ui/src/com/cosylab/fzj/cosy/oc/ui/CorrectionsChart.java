package com.cosylab.fzj.cosy.oc.ui;

import java.util.Iterator;

import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;

public class CorrectionsChart<X,Y> extends LineChart<X,Y> {

    public CorrectionsChart(Axis<X> xAxis, Axis<Y> yAxis) {
        super(xAxis, yAxis);
    }

    @Override
    protected void layoutPlotChildren() {
        if (getData() == null) {
            return;
        }
        for (Series<X, Y> series : getData()) {
            for (Iterator<Data<X, Y>> it = getDisplayedDataIterator(series); it.hasNext(); ) {
                Data<X, Y> item = it.next();
                final double x = getXAxis().getDisplayPosition(getCurrentDisplayedXValue(item));
                final double y = getYAxis().getDisplayPosition(
                        getYAxis().toRealValue(getYAxis().toNumericValue(getCurrentDisplayedYValue(item))));
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                Node symbol = item.getNode();
                if (symbol != null) {
                    final double chartYPos = getYAxis().toNumericValue(getYAxis().getValueForDisplay(y));
                    final double yAxisZeroPos = getYAxis().getZeroPosition();
                    final boolean isYAxisZeroPosVisible = !Double.isNaN(yAxisZeroPos);
                    final double yAxisHeight = getYAxis().getHeight();
                    final double yFillPos = isYAxisZeroPosVisible ? yAxisZeroPos :
                            chartYPos < 0 ? chartYPos - yAxisHeight : yAxisHeight;

                    final double w = symbol.prefWidth(-1);
                    final double h =  Math.sqrt(Math.pow((y - yFillPos), 2));

                    if (chartYPos > 0) {
                        symbol.resizeRelocate(x-(w/2), y, w, h);
                    } else {
                        symbol.resizeRelocate(x-(w/2), yFillPos, w, h);
                    }
                }
            }
        }
    }
}