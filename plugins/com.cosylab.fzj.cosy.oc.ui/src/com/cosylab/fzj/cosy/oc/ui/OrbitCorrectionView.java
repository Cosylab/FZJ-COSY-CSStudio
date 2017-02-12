package com.cosylab.fzj.cosy.oc.ui;

import static org.csstudio.ui.fx.util.FXUtilities.setGridConstraints;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.eclipse.fx.ui.workbench3.FXViewPart;

import com.cosylab.fzj.cosy.oc.LatticeElementType;
import com.cosylab.fzj.cosy.oc.Preferences;
import com.cosylab.fzj.cosy.oc.ui.model.BPM;
import com.cosylab.fzj.cosy.oc.ui.model.Corrector;
import com.cosylab.fzj.cosy.oc.ui.model.SeriesType;
import com.cosylab.fzj.cosy.oc.ui.util.SymmetricAxis;
import com.cosylab.fzj.cosy.oc.ui.util.TooltipCheckBox;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * <code>OrbitCorrectionView</code> is an {@link FXViewPart} implementation for displaying orbit correction. It provides
 * charts that displays orbit, corrections and lattice.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class OrbitCorrectionView extends FXViewPart {

    /** The ID of this view */
    static final String ID = "com.cosylab.fzj.cosy.oc.ui.orbitcorrection";

    private static enum ChartType {
        ORBIT, CORRECTIONS, LATTICE
    }

    static StringConverter<Number> TICK_LABEL_FORMATTER = new StringConverter<Number>() {

        private final DecimalFormat format = new DecimalFormat("###");

        @Override
        public Number fromString(String string) {
            return Double.NaN;
        }

        @Override
        public String toString(Number object) {
            double val = object.doubleValue();
            if (val < 0 || val > 184) {
                return "";
            } else {
                return format.format(val);
            }
        }
    };
    private CheckBox hOrbitCheckBox, vOrbitCheckBox, hGoldenOrbitCheckBox, vGoldenOrbitCheckBox;
    private CheckBox hCorrectorsCheckBox, vCorrectorsCheckBox;
    private CheckBox bpmLatticeCheckBox, hCorrectorsLatticeCheckBox, vCorrectorsLatticeCheckBox;
    private LineChart<Number,Number> orbitChart, latticeChart;
    private CorrectionsChart<Number,Number> correctionsChart;
    private ZoomableLineChart orbitZoom, correctionsZoom, latticeZoom;
    private OrbitCorrectionController controller;
    private FileChooser fileChooser;
    private Scene scene;
    private static boolean tooltipDelaySet = false;

    private static void initializeTooltipDelay() {
        if (tooltipDelaySet) return;
        tooltipDelaySet = true;
        try {
            Tooltip obj = new Tooltip();
            Class<?> clazz = Arrays.stream(obj.getClass().getDeclaredClasses())
                    .filter(c -> c.getName().contains("TooltipBehavior")).findFirst().orElse(null);
            if (clazz == null) return;
            Constructor<?> constructor = clazz.getDeclaredConstructor(Duration.class,Duration.class,Duration.class,
                    boolean.class);
            constructor.setAccessible(true);
            Object tooltipBehavior = constructor.newInstance(new Duration(250),new Duration(7000),new Duration(200),
                    false);
            Field fieldBehavior = obj.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            fieldBehavior.set(obj,tooltipBehavior);
        } catch (Exception e) {
            //ignore
        }
    }

    /**
     * Constructs new orbit correction view.
     */
    public OrbitCorrectionView() {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(0,new ExtensionFilter("All Files","*.*"));
        fileChooser.setInitialDirectory(Preferences.getInstance().getInitialDirectory());
        controller = new OrbitCorrectionController();
        controller.addLaticeUpdateCallback(this::recreateAllCharts);
    }

    /**
     * @return the controller used by this view.
     */
    public OrbitCorrectionController getController() {
        return controller;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.fx.ui.workbench3.FXViewPart#createFxScene()
     */
    @Override
    protected Scene createFxScene() {
        initializeTooltipDelay();
        scene = new Scene(createContentPane());
        scene.getStylesheets().add(OrbitCorrectionView.class.getResource("style.css").toExternalForm());
        Arrays.stream(LatticeElementType.values()).forEach(this::recreateAllCharts);
        controller.allConnectedProperty().addListener((a, o, n) -> setConnectedState(n));
        setConnectedState(controller.allConnectedProperty().get());
        return scene;
    }

    private void setConnectedState(boolean connected) {
        Effect effectChart = connected ? null : new ColorAdjust(0.0,0.8,0.2,0);
        orbitChart.setEffect(effectChart);
        correctionsChart.setEffect(effectChart);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.fx.ui.workbench3.FXViewPart#setFxFocus()
     */
    @Override
    protected void setFxFocus() {
        orbitChart.requestFocus();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        controller.dispose();
    }

    /**
     * @return grid pane with charts and controls.
     */
    private Parent createContentPane() {
        GridPane contentPane = new GridPane();
        contentPane.getStyleClass().add("root");
        //        contentPane.getRowConstraints().addAll(new RowConstraints(),
        //                new RowConstraints(310,Region.USE_COMPUTED_SIZE,310));
        GridPane chartsPane = createCharts();
        //GridPane controlsPane = createControls();
        setFullResizable(chartsPane);
        contentPane.add(chartsPane,0,0);
        //        contentPane.add(controlsPane,0,1);
        return contentPane;
    }

    /**
     * @return grid pane with orbit chart, correction chart and lattice chart.
     */
    private GridPane createCharts() {
        GridPane charts = new GridPane();
        String s = System.getProperty("os.name","nix").toLowerCase();
        final int c = s.contains("win") ? 180 : 230;
        charts.getColumnConstraints().setAll(new ColumnConstraints(50,Region.USE_COMPUTED_SIZE,50),
                new ColumnConstraints(),new ColumnConstraints(c,Region.USE_COMPUTED_SIZE,c));
        Region orbitNode = createOrbitChart();
        GridPane orbitLegendNode = createOrbitChartLegend();
        GridPane.setMargin(orbitLegendNode,new Insets(0,10,0,10));
        setGridConstraints(orbitNode,true,true,Priority.ALWAYS,Priority.ALWAYS);
        setGridConstraints(orbitLegendNode,true,false,HPos.LEFT,VPos.TOP,Priority.NEVER,Priority.ALWAYS);
        Region correctionNode = createCorrectionsChart();
        GridPane correctionLegendNode = createCorrectionsChartLegend();
        GridPane.setMargin(correctionLegendNode,new Insets(0,10,0,10));
        setGridConstraints(correctionNode,true,true,Priority.ALWAYS,Priority.ALWAYS);
        setGridConstraints(correctionLegendNode,true,false,HPos.LEFT,VPos.TOP,Priority.NEVER,Priority.ALWAYS);
        Region latticeNode = createLatticeChart();
        GridPane latticeLegendNode = createLatticeChartLegend();
        GridPane.setMargin(latticeLegendNode,new Insets(0,10,0,10));
        setGridConstraints(latticeNode,true,false,Priority.ALWAYS,Priority.NEVER);
        setGridConstraints(latticeLegendNode,true,false,HPos.LEFT,VPos.TOP,Priority.NEVER,Priority.NEVER);
        charts.add(createVerticalAxis(orbitChart,"Position [mm]"),0,0);
        charts.add(orbitNode,1,0);
        charts.add(orbitLegendNode,2,0);
        charts.add(createVerticalAxis(correctionsChart,
                controller.mradProperty().get() ? "Kick Angle [mrad]" : "Kick Strength [A]"),0,1);
        controller.mradProperty().addListener((a, o, n) -> {
            GridPane pane = (GridPane)charts.getChildren().get(3);
            ((NumberAxis)pane.getChildren().get(0)).setLabel(n ? "Kick Angle [mrad]" : "Kick Strength [A]");
        });
        charts.add(correctionNode,1,1);
        charts.add(correctionLegendNode,2,1);
        charts.add(latticeNode,1,2);
        charts.add(latticeLegendNode,2,2);
        //        setFullResizable(orbitNode,correctionNode,orbitLegendNode,correctionLegendNode);
        configureChartSynchronisation();
        charts.setMinWidth(0);
        charts.setMaxWidth(Integer.MAX_VALUE);
        return charts;
    }

    /**
     * Synchronise the lower and upper bound of all three charts, when zoom is called.
     */
    private void configureChartSynchronisation() {
        // synchronise the lower and upper bounds of all three charts
        ((NumberAxis)orbitChart.getXAxis()).lowerBoundProperty().addListener((a, o, n) -> {
            correctionsZoom.doHorizontalZoom(n.doubleValue(),((NumberAxis)orbitChart.getXAxis()).getUpperBound());
            latticeZoom.doHorizontalZoom(n.doubleValue(),((NumberAxis)orbitChart.getXAxis()).getUpperBound());
        });
        ((NumberAxis)correctionsChart.getXAxis()).lowerBoundProperty().addListener((a, o, n) -> {
            orbitZoom.doHorizontalZoom(n.doubleValue(),((NumberAxis)correctionsChart.getXAxis()).getUpperBound());
            latticeZoom.doHorizontalZoom(n.doubleValue(),((NumberAxis)correctionsChart.getXAxis()).getUpperBound());
        });
        ((NumberAxis)latticeChart.getXAxis()).lowerBoundProperty().addListener((a, o, n) -> {
            correctionsZoom.doHorizontalZoom(n.doubleValue(),((NumberAxis)latticeChart.getXAxis()).getUpperBound());
            orbitZoom.doHorizontalZoom(n.doubleValue(),((NumberAxis)latticeChart.getXAxis()).getUpperBound());
        });
        ((NumberAxis)orbitChart.getXAxis()).upperBoundProperty().addListener((a, o, n) -> {
            correctionsZoom.doHorizontalZoom(((NumberAxis)orbitChart.getXAxis()).getLowerBound(),n.doubleValue());
            latticeZoom.doHorizontalZoom(((NumberAxis)orbitChart.getXAxis()).getLowerBound(),n.doubleValue());
        });
        ((NumberAxis)correctionsChart.getXAxis()).upperBoundProperty().addListener((a, o, n) -> {
            orbitZoom.doHorizontalZoom(((NumberAxis)correctionsChart.getXAxis()).getLowerBound(),n.doubleValue());
            latticeZoom.doHorizontalZoom(((NumberAxis)correctionsChart.getXAxis()).getLowerBound(),n.doubleValue());
        });
        ((NumberAxis)latticeChart.getXAxis()).upperBoundProperty().addListener((a, o, n) -> {
            correctionsZoom.doHorizontalZoom(((NumberAxis)latticeChart.getXAxis()).getLowerBound(),n.doubleValue());
            orbitZoom.doHorizontalZoom(((NumberAxis)latticeChart.getXAxis()).getLowerBound(),n.doubleValue());
        });
        ChangeListener<Boolean> zoomListener = (a, o, n) -> {
            // whenever default zoom is called, call it on all three charts
            if (n) {
                correctionsZoom.defaultZoom();
                latticeZoom.defaultZoom();
                orbitZoom.defaultZoom();
            }
        };
        orbitZoom.defaultZoomProperty().addListener(zoomListener);
        correctionsZoom.defaultZoomProperty().addListener(zoomListener);
        latticeZoom.defaultZoomProperty().addListener(zoomListener);
    }

    /**
     * Construct a standalone vertical chart axis, which receives the range of the vertical axis in the given chart.
     *
     * @param chart the chart to provide the axis bounds
     * @param label the label for the axis
     * @return a node containing the axis that can be added to pane next to a chart
     */
    private static Node createVerticalAxis(LineChart<Number,Number> chart, String label) {
        NumberAxis axis = new NumberAxis();
        axis.autoRangingProperty().set(false);
        axis.setSide(Side.LEFT);
        axis.upperBoundProperty().bind(((ValueAxis<Number>)chart.getYAxis()).upperBoundProperty());
        axis.lowerBoundProperty().bind(((ValueAxis<Number>)chart.getYAxis()).lowerBoundProperty());
        axis.tickUnitProperty().bind(((SymmetricAxis)chart.getYAxis()).tickUnitProperty());
        axis.upperBoundProperty().addListener((a, o, n) -> axis.layout());
        axis.lowerBoundProperty().addListener((a, o, n) -> axis.layout());
        axis.tickUnitProperty().addListener((a, o, n) -> axis.layout());
        axis.setLabel(label);
        axis.setTickMarkVisible(true);
        axis.setMinorTickVisible(false);
        axis.setTickLabelsVisible(true);
        axis.setTickLabelGap(2);
        GridPane axisPane = new GridPane();
        setGridConstraints(axis,true,true,Priority.ALWAYS,Priority.ALWAYS);
        setGridConstraints(axisPane,true,true,Priority.ALWAYS,Priority.ALWAYS);
        axisPane.setPadding(new Insets(10,0,3,0));
        axisPane.add(axis,0,0);
        axisPane.translateXProperty().set(15);
        return axisPane;
    }

    private void recreateAllCharts(LatticeElementType type) {
        if (scene == null) {
            return;
        }
        if (type == LatticeElementType.HORIZONTAL_BPM) {
            if (hOrbitCheckBox.isSelected()) {
                addSeries(ChartType.ORBIT,SeriesType.HORIZONTAL_ORBIT,false);
            }
            if (hGoldenOrbitCheckBox.isSelected()) {
                addSeries(ChartType.ORBIT,SeriesType.GOLDEN_HORIZONTAL_ORBIT,false);
            }
            if (bpmLatticeCheckBox.isSelected()) {
                addSeries(ChartType.LATTICE,SeriesType.BPM,false);
            }
        } else if (type == LatticeElementType.VERTICAL_BPM) {
            if (vOrbitCheckBox.isSelected()) {
                addSeries(ChartType.ORBIT,SeriesType.VERTICAL_ORBIT,false);
            }
            if (vGoldenOrbitCheckBox.isSelected()) {
                addSeries(ChartType.ORBIT,SeriesType.GOLDEN_VERTICAL_ORBIT,false);
            }
            if (bpmLatticeCheckBox.isSelected()) {
                addSeries(ChartType.LATTICE,SeriesType.BPM,false);
            }
        } else if (type == LatticeElementType.HORIZONTAL_CORRECTOR) {
            if (hCorrectorsCheckBox.isSelected()) {
                addSeries(ChartType.CORRECTIONS,SeriesType.HORIZONTAL_CORRECTORS_CORRECTION,false);
            }
            if (hCorrectorsCheckBox.isSelected()) {
                addSeries(ChartType.LATTICE,SeriesType.HORIZONTAL_CORRECTORS,false);
            }
        } else if (type == LatticeElementType.VERTICAL_CORRECTOR) {
            if (vCorrectorsCheckBox.isSelected()) {
                addSeries(ChartType.CORRECTIONS,SeriesType.VERTICAL_CORRECTORS_CORRECTION,false);
            }
            if (vCorrectorsCheckBox.isSelected()) {
                addSeries(ChartType.LATTICE,SeriesType.VERTICAL_CORRECTORS,false);
            }
        }
    }

    /**
     * @return region with configured orbit chart.
     */
    private Region createOrbitChart() {
        ValueAxis<Number> xAxis = new NumberAxis(-5,185,5);
        xAxis.setTickLabelFormatter(TICK_LABEL_FORMATTER);
        xAxis.setMinorTickCount(0);
        ValueAxis<Number> yAxis = new SymmetricAxis();
        yAxis.setAnimated(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setMinorTickVisible(false);
        yAxis.autoRangingProperty().set(true);
        orbitChart = new LineChart<>(xAxis,yAxis);
        orbitChart.setLegendVisible(false);
        orbitChart.setAnimated(false);
        orbitChart.getStyleClass().add("orbit-chart");
        orbitChart.setVerticalZeroLineVisible(false);
        addSeries(ChartType.ORBIT,SeriesType.HORIZONTAL_ORBIT,false);
        addSeries(ChartType.ORBIT,SeriesType.VERTICAL_ORBIT,false);
        addSeries(ChartType.ORBIT,SeriesType.GOLDEN_HORIZONTAL_ORBIT,false);
        addSeries(ChartType.ORBIT,SeriesType.GOLDEN_VERTICAL_ORBIT,false);
        orbitZoom = new ZoomableLineChart(orbitChart,false,true,true);
        setMinMax(orbitChart,orbitZoom);
        return orbitZoom;
    }

    /**
     * @return region with configured corrections chart.
     */
    private Region createCorrectionsChart() {
        ValueAxis<Number> xAxis = new NumberAxis(-5,185,5);
        xAxis.setMinorTickCount(0);
        xAxis.setTickLabelFormatter(TICK_LABEL_FORMATTER);
        ValueAxis<Number> yAxis = new SymmetricAxis();
        yAxis.setAnimated(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setMinorTickVisible(false);
        yAxis.autoRangingProperty().set(true);
        correctionsChart = new CorrectionsChart<Number,Number>(xAxis,yAxis);
        correctionsChart.setLegendVisible(false);
        correctionsChart.setAnimated(false);
        correctionsChart.setVerticalZeroLineVisible(false);
        correctionsChart.getStyleClass().add("corrections-chart");
        addSeries(ChartType.CORRECTIONS,SeriesType.HORIZONTAL_CORRECTORS_CORRECTION,false);
        addSeries(ChartType.CORRECTIONS,SeriesType.VERTICAL_CORRECTORS_CORRECTION,false);
        correctionsZoom = new ZoomableLineChart(correctionsChart,false,true,true);
        correctionsZoom.setMinWidth(0);
        correctionsZoom.setMaxWidth(Integer.MAX_VALUE);
        correctionsZoom.setMinHeight(0);
        correctionsZoom.setMaxHeight(Integer.MAX_VALUE);
        setMinMax(correctionsZoom,correctionsChart);
        return correctionsZoom;
    }

    /**
     * @return region with configured lattice chart.
     */
    private Region createLatticeChart() {
        ValueAxis<Number> xAxis = new NumberAxis(-5,185,5);
        xAxis.setMinorTickCount(0);
        xAxis.setTickLabelFormatter(TICK_LABEL_FORMATTER);
        xAxis.setLabel("Position in the ring [m]");
        ValueAxis<Number> yAxis = new SymmetricAxis(-50,50,10);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setMinorTickVisible(false);
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(true);
        latticeChart = new LineChart<>(xAxis,yAxis);
        latticeChart.setLegendVisible(false);
        latticeChart.setAnimated(false);
        latticeChart.getStyleClass().add("lattice-chart");
        addSeries(ChartType.LATTICE,SeriesType.BPM,false);
        addSeries(ChartType.LATTICE,SeriesType.HORIZONTAL_CORRECTORS,false);
        addSeries(ChartType.LATTICE,SeriesType.VERTICAL_CORRECTORS,false);
        latticeZoom = new ZoomableLineChart(latticeChart,false,true,false);
        latticeZoom.setMinWidth(0);
        latticeZoom.setMaxWidth(Integer.MAX_VALUE);
        final int height = 100;
        latticeChart.setMaxHeight(height);
        latticeChart.setMinHeight(height);
        latticeChart.setPrefHeight(height);
        return latticeZoom;
    }

    /**
     * @return grid pane with orbit chart legend.
     */
    private GridPane createOrbitChartLegend() {
        GridPane legend = new GridPane();
        legend.setVgap(5);
        legend.setPadding(new Insets(10,0,0,0));
        final Function<SeriesType,EventHandler<ActionEvent>> actionSupplier = seriesType -> e -> {
            if (((TooltipCheckBox)e.getSource()).isSelected()) {
                addSeries(ChartType.ORBIT,seriesType,false);
            } else {
                removeSeries(ChartType.ORBIT,seriesType);
            }
        };
        hOrbitCheckBox = new TooltipCheckBox("Horizontal Orbit");
        hOrbitCheckBox.setSelected(true);
        hOrbitCheckBox.setOnAction(actionSupplier.apply(SeriesType.HORIZONTAL_ORBIT));
        hOrbitCheckBox.getStyleClass().add("horizontal-check-box");
        vOrbitCheckBox = new TooltipCheckBox("Vertical Orbit");
        vOrbitCheckBox.setSelected(true);
        vOrbitCheckBox.setOnAction(actionSupplier.apply(SeriesType.VERTICAL_ORBIT));
        vOrbitCheckBox.getStyleClass().add("vertical-check-box");
        hGoldenOrbitCheckBox = new TooltipCheckBox("Golden Horizontal Orbit");
        hGoldenOrbitCheckBox.setSelected(true);
        hGoldenOrbitCheckBox.setOnAction(actionSupplier.apply(SeriesType.GOLDEN_HORIZONTAL_ORBIT));
        hGoldenOrbitCheckBox.getStyleClass().add("golden-horizontal-check-box");
        vGoldenOrbitCheckBox = new TooltipCheckBox("Golden Vertical Orbit");
        vGoldenOrbitCheckBox.setSelected(true);
        vGoldenOrbitCheckBox.setOnAction(actionSupplier.apply(SeriesType.GOLDEN_VERTICAL_ORBIT));
        vGoldenOrbitCheckBox.getStyleClass().add("golden-vertical-check-box");
        legend.add(hOrbitCheckBox,0,0);
        legend.add(vOrbitCheckBox,0,1);
        legend.add(hGoldenOrbitCheckBox,0,2);
        legend.add(vGoldenOrbitCheckBox,0,3);
        setMinMax(legend,hOrbitCheckBox,vOrbitCheckBox,hGoldenOrbitCheckBox,vGoldenOrbitCheckBox);
        return legend;
    }

    /**
     * @return grid pane with corrections chart legend.
     */
    private GridPane createCorrectionsChartLegend() {
        GridPane legend = new GridPane();
        legend.setPadding(new Insets(10,0,0,0));
        legend.setVgap(10);
        GridPane checkBoxes = new GridPane();
        checkBoxes.setVgap(5);
        hCorrectorsCheckBox = new TooltipCheckBox("Horizontal Correctors");
        hCorrectorsCheckBox.setSelected(true);
        hCorrectorsCheckBox.setOnAction(e -> {
            if (hCorrectorsCheckBox.isSelected()) {
                addSeries(ChartType.CORRECTIONS,SeriesType.HORIZONTAL_CORRECTORS_CORRECTION,false);
            } else {
                removeSeries(ChartType.CORRECTIONS,SeriesType.HORIZONTAL_CORRECTORS_CORRECTION);
            }
        });
        hCorrectorsCheckBox.getStyleClass().add("horizontal-check-box");
        vCorrectorsCheckBox = new TooltipCheckBox("Vertical Correctors");
        vCorrectorsCheckBox.setSelected(true);
        vCorrectorsCheckBox.setOnAction(e -> {
            if (vCorrectorsCheckBox.isSelected()) {
                addSeries(ChartType.CORRECTIONS,SeriesType.VERTICAL_CORRECTORS_CORRECTION,false);
            } else {
                removeSeries(ChartType.CORRECTIONS,SeriesType.VERTICAL_CORRECTORS_CORRECTION);
            }
        });
        vCorrectorsCheckBox.getStyleClass().add("vertical-check-box");
        checkBoxes.add(hCorrectorsCheckBox,0,0);
        checkBoxes.add(vCorrectorsCheckBox,0,1);
        legend.add(checkBoxes,0,0);
        setMinMax(legend,hCorrectorsCheckBox,vCorrectorsCheckBox);
        return legend;
    }

    /**
     * @return grid pane with lattice chart legend.
     */
    private GridPane createLatticeChartLegend() {
        GridPane legend = new GridPane();
        legend.setPadding(new Insets(10,0,0,0));
        legend.setVgap(5);
        bpmLatticeCheckBox = new TooltipCheckBox("BPMs");
        bpmLatticeCheckBox.setSelected(true);
        bpmLatticeCheckBox.setOnAction(e -> {
            if (bpmLatticeCheckBox.isSelected()) {
                addSeries(ChartType.LATTICE,SeriesType.BPM,false);
            } else {
                removeSeries(ChartType.LATTICE,SeriesType.BPM);
            }
        });
        bpmLatticeCheckBox.getStyleClass().add("bpm-check-box");
        hCorrectorsLatticeCheckBox = new TooltipCheckBox("Horizontal Correctors");
        hCorrectorsLatticeCheckBox.setSelected(true);
        hCorrectorsLatticeCheckBox.setOnAction(e -> {
            if (hCorrectorsLatticeCheckBox.isSelected()) {
                addSeries(ChartType.LATTICE,SeriesType.HORIZONTAL_CORRECTORS,false);
            } else {
                removeSeries(ChartType.LATTICE,SeriesType.HORIZONTAL_CORRECTORS);
            }
        });
        hCorrectorsLatticeCheckBox.getStyleClass().add("horizontal-check-box");
        vCorrectorsLatticeCheckBox = new TooltipCheckBox("Vertical Correctors");
        vCorrectorsLatticeCheckBox.setSelected(true);
        vCorrectorsLatticeCheckBox.setOnAction(e -> {
            if (vCorrectorsLatticeCheckBox.isSelected()) {
                addSeries(ChartType.LATTICE,SeriesType.VERTICAL_CORRECTORS,false);
            } else {
                removeSeries(ChartType.LATTICE,SeriesType.VERTICAL_CORRECTORS);
            }
        });
        vCorrectorsLatticeCheckBox.getStyleClass().add("vertical-check-box");
        legend.add(bpmLatticeCheckBox,0,0);
        legend.add(hCorrectorsLatticeCheckBox,0,1);
        legend.add(vCorrectorsLatticeCheckBox,0,2);
        setMinMax(legend,bpmLatticeCheckBox,hCorrectorsLatticeCheckBox,vCorrectorsLatticeCheckBox);
        return legend;
    }

    /**
     * Sets given regions full resizable.
     */
    static void setFullResizable(Region... components) {
        for (Region component : components) {
            setGridConstraints(component,true,true,Priority.ALWAYS,Priority.ALWAYS);
            component.setMinSize(0,0);
            component.setMaxSize(Integer.MAX_VALUE,Integer.MAX_VALUE);
        }
    }

    /**
     * Sets the min and max width and height of the components to 0 and MAX_VALUE respectively.
     *
     * @param components the components to set the min and max of
     */
    static void setMinMax(Region... components) {
        for (Region component : components) {
            component.setMinWidth(0);
            component.setMaxWidth(Integer.MAX_VALUE);
            component.setMinHeight(0);
            component.setMaxHeight(Integer.MAX_VALUE);
        }
    }

    /**
     * Adds given series type to the given chart. Series could also be empty if the given empty flag is set to true.
     */
    private void addSeries(ChartType chartType, SeriesType seriesType, boolean empty) {
        LineChart<Number,Number> chart = null;
        if (chartType == ChartType.ORBIT) {
            chart = orbitChart;
        } else if (chartType == ChartType.CORRECTIONS) {
            chart = correctionsChart;
        } else if (chartType == ChartType.LATTICE) {
            chart = latticeChart;
        } else {
            return;
        }
        Series<Number,Number> series = empty ? new Series<>("Empty Series",FXCollections.emptyObservableList())
                : new Series<>(seriesType.getSeriesName(),getData(chartType,seriesType));
        if (chart.getData().size() > seriesType.getSeriesIndex()) {
            chart.getData().set(seriesType.getSeriesIndex(),series);
        } else {
            chart.getData().add(series);
        }
    }

    /**
     * Removes the given series from the given chart.
     *
     * @param chartType chart type
     * @param seriesType series type
     */
    private void removeSeries(ChartType chartType, SeriesType seriesType) {
        ObservableList<Series<Number,Number>> data = null;
        if (chartType == ChartType.ORBIT) {
            data = orbitChart.getData();
        } else if (chartType == ChartType.CORRECTIONS) {
            data = correctionsChart.getData();
        } else if (chartType == ChartType.LATTICE) {
            data = latticeChart.getData();
        } else {
            return;
        }
        data.stream().filter(s -> s.getName().equals(seriesType.getSeriesName())).findFirst()
                .ifPresent(s -> addSeries(chartType,seriesType,true));
    }

    /**
     * @param chartType chart type
     * @param seriesType series type
     * @return data from the given chart type
     */
    private ObservableList<Data<Number,Number>> getData(ChartType chartType, SeriesType seriesType) {
        ObservableList<Data<Number,Number>> data = FXCollections.observableArrayList();
        if (chartType == ChartType.ORBIT) {
            final Function<BPM,DoubleProperty> property;
            List<BPM> bpms;
            if (seriesType == SeriesType.VERTICAL_ORBIT) {
                property = bpm -> bpm.positionProperty();
                bpms = controller.getVerticalBPMs();
            } else if (seriesType == SeriesType.HORIZONTAL_ORBIT) {
                property = bpm -> bpm.positionProperty();
                bpms = controller.getHorizontalBPMs();
            } else if (seriesType == SeriesType.GOLDEN_VERTICAL_ORBIT) {
                property = bpm -> bpm.goldenPositionProperty();
                bpms = controller.getVerticalBPMs();
            } else if (seriesType == SeriesType.GOLDEN_HORIZONTAL_ORBIT) {
                property = bpm -> bpm.goldenPositionProperty();
                bpms = controller.getHorizontalBPMs();
            } else {
                return data;
            }
            bpms.stream().filter(bpm -> bpm.enabledProperty().get()).forEach(bpm -> {
                Data<Number,Number> dataPoint = new Data<>();
                dataPoint.XValueProperty().bind(bpm.locationProperty());
                dataPoint.YValueProperty().bind(property.apply(bpm));
                dataPoint.nodeProperty().addListener((a, o, n) -> {
                    if (n != null) {
                        Tooltip tooltip = new Tooltip(bpm.nameProperty().get());
                        tooltip.textProperty()
                                .bind(dataPoint.YValueProperty().asString(bpm.nameProperty().get() + ": %3.2f"));
                        Tooltip.install(n,tooltip);
                    }
                });
                data.add(dataPoint);
            });
        } else if (chartType == ChartType.CORRECTIONS) {
            List<Corrector> correctors;
            if (seriesType == SeriesType.HORIZONTAL_CORRECTORS_CORRECTION) {
                correctors = controller.getHorizontalCorrectors();
            } else if (seriesType == SeriesType.VERTICAL_CORRECTORS_CORRECTION) {
                correctors = controller.getVerticalCorrectors();
            } else {
                return data;
            }
            correctors.stream().filter(corrector -> corrector.enabledProperty().get()).forEach(corrector -> {
                Data<Number,Number> dataPoint = new Data<>();
                dataPoint.XValueProperty().bind(corrector.locationProperty());
                dataPoint.YValueProperty().bind(corrector.correctionProperty());
                dataPoint.nodeProperty().addListener((a, o, n) -> {
                    if (n != null) {
                        Tooltip tooltip = new Tooltip(corrector.nameProperty().get());
                        tooltip.textProperty()
                                .bind(dataPoint.YValueProperty().asString(corrector.nameProperty().get() + ": %3.2f"));
                        Tooltip.install(n,tooltip);
                    }
                });
                data.add(dataPoint);
            });
        } else if (chartType == ChartType.LATTICE) {
            if (seriesType == SeriesType.BPM) {
                List<BPM> allBPMs = new ArrayList<>();
                allBPMs.addAll(controller.getVerticalBPMs());
                allBPMs.addAll(controller.getHorizontalBPMs());
                allBPMs.stream().filter(bpm -> bpm.enabledProperty().get()).sorted().forEach(bpm -> {
                    Data<Number,Number> dataPoint = new Data<>(bpm.locationProperty().get(),0d);
                    dataPoint.XValueProperty().bind(bpm.locationProperty());
                    dataPoint.nodeProperty().addListener((a, o, n) -> {
                        if (n != null) {
                            Tooltip.install(n,new Tooltip(bpm.nameProperty().get()));
                        }
                    });
                    data.add(dataPoint);
                });
            } else {
                List<Corrector> correctors;
                if (seriesType == SeriesType.HORIZONTAL_CORRECTORS) {
                    correctors = controller.getHorizontalCorrectors();
                } else if (seriesType == SeriesType.VERTICAL_CORRECTORS) {
                    correctors = controller.getVerticalCorrectors();
                } else {
                    return data;
                }
                correctors.stream().filter(corrector -> corrector.enabledProperty().get()).forEach(corrector -> {
                    Data<Number,Number> dataPoint = new Data<>(corrector.locationProperty().get(),0);
                    dataPoint.XValueProperty().bind(corrector.locationProperty());
                    dataPoint.nodeProperty().addListener((a, o, n) -> {
                        if (n != null) {
                            Tooltip.install(n,new Tooltip(corrector.nameProperty().get()));
                        }
                    });
                    data.add(dataPoint);
                });
            }
        }
        return data;
    }
}
