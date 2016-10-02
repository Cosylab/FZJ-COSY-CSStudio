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
        Axis<X> xAxis = getXAxis();
        Axis<Y> yAxis = getYAxis();
        final double yAxisHeight = yAxis.getHeight();
        final double yAxisZeroPos = yAxis.getZeroPosition();
        final boolean isYAxisZeroPosVisible = !Double.isNaN(yAxisZeroPos);
        
        for (Series<X, Y> series : getData()) {
            Iterator<Data<X, Y>> it = getDisplayedDataIterator(series);
            while(it.hasNext()) {
                Data<X, Y> item = it.next();
                final double x = xAxis.getDisplayPosition(getCurrentDisplayedXValue(item));
                final double y = yAxis.getDisplayPosition(
                        yAxis.toRealValue(yAxis.toNumericValue(getCurrentDisplayedYValue(item))));
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                Node symbol = item.getNode();
                if (symbol != null) {
                    final double chartYPos = yAxis.toNumericValue(yAxis.getValueForDisplay(y));
                    final double yFillPos = isYAxisZeroPosVisible ? yAxisZeroPos :
                            chartYPos < 0 ? chartYPos - yAxisHeight : yAxisHeight;
                    final double w = symbol.prefWidth(-1);
                    final double h =  Math.abs(y - yFillPos);
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