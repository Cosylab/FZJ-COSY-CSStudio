package com.cosylab.fzj.cosy.oc.ui;

import static org.csstudio.ui.fx.util.FXUtilities.setGridConstraints;

import java.util.List;

import org.csstudio.ui.fx.util.StaticTextArea;
import org.eclipse.fx.ui.workbench3.FXViewPart;

import com.cosylab.fzj.cosy.oc.LatticeElementType;
import com.cosylab.fzj.cosy.oc.ui.model.ChartType;
import com.cosylab.fzj.cosy.oc.ui.model.SeriesType;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

public class OrbitCorrectionView extends FXViewPart {

    private LineChart<Number, Number> orbitChart;
    private CorrectionsChart<Number, Number> correctionsChart;
    private LineChart<Number, Number> latticeChart;
    private ZoomableLineChart orbitZoom;
    private ZoomableLineChart correctionsZoom;
    private ZoomableLineChart latticeZoom;

    private OrbitCorrectionController controller;

    public OrbitCorrectionView() {
        try {
            controller = new OrbitCorrectionController();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected Scene createFxScene() {
        Scene scene = new Scene(createContentPane());
        scene.getStylesheets().add(OrbitCorrectionView.class.getResource("style.css").toExternalForm());
        return scene;
    }

    @Override
    protected void setFxFocus() {
        orbitChart.requestFocus();
    }

    @Override
    public void dispose() {
        super.dispose();
        controller.dispose();
    }

    private GridPane createContentPane() {
        GridPane contentPane = new GridPane();
        contentPane.getStyleClass().add("root");

        contentPane.getRowConstraints().addAll(new RowConstraints(),
                new RowConstraints(310, Region.USE_COMPUTED_SIZE, 310));

        GridPane chartsPane = createCharts();
        GridPane controlsPane = createControls();
        setFullResizable(chartsPane, controlsPane);
        contentPane.add(chartsPane, 0, 0);
        contentPane.add(controlsPane, 0, 1);

        return contentPane;
    }

    private GridPane createCharts() {
        GridPane charts = new GridPane();

        charts.getColumnConstraints().setAll(new ColumnConstraints(50, Region.USE_COMPUTED_SIZE, 50),
                new ColumnConstraints(), new ColumnConstraints(180, Region.USE_COMPUTED_SIZE, 180));

        Region orbitNode = createOrbitChart();
        GridPane orbitLegendNode = createOrbitChartLegend();
        GridPane.setMargin(orbitLegendNode, new Insets(0, 10, 0, 10));
        setGridConstraints(orbitNode, true, true, Priority.ALWAYS, Priority.ALWAYS);
        setGridConstraints(orbitLegendNode, true, false, HPos.LEFT, VPos.TOP, Priority.NEVER, Priority.ALWAYS);

        Region correctionNode = createCorrectionsChart();
        GridPane correctionLegendNode = createCorrectionsChartLegend();
        GridPane.setMargin(correctionLegendNode, new Insets(0, 10, 0, 10));
        setGridConstraints(correctionNode, true, true, Priority.ALWAYS, Priority.ALWAYS);
        setGridConstraints(correctionLegendNode, true, false, HPos.LEFT, VPos.TOP, Priority.NEVER, Priority.ALWAYS);

        Region latticeNode = createLatticeChart();
        GridPane latticeLegendNode = createLatticeChartLegend();
        GridPane.setMargin(latticeLegendNode, new Insets(0, 10, 0, 10));
        setGridConstraints(latticeNode, true, false, Priority.ALWAYS, Priority.NEVER);
        setGridConstraints(latticeLegendNode, true, false, HPos.LEFT, VPos.TOP, Priority.NEVER, Priority.NEVER);

        charts.add(createVerticalAxis(orbitChart, "Position [mm]"), 0, 0);
        charts.add(orbitNode, 1, 0);
        charts.add(orbitLegendNode, 2, 0);
        charts.add(createVerticalAxis(correctionsChart, "Kick Angle [mrad]"), 0, 1);
        charts.add(correctionNode, 1, 1);
        charts.add(correctionLegendNode, 2, 1);
        charts.add(latticeNode, 1, 2);
        charts.add(latticeLegendNode, 2, 2);

        configureChartSynchronisation();

        charts.setMinWidth(0);
        charts.setMaxWidth(Integer.MAX_VALUE);
        return charts;
    }

    private void configureChartSynchronisation() {
        // synchronise the lower and upper bounds of all three charts

        ((SpecialNumberAxis) orbitChart.getXAxis()).lowerBoundProperty().addListener((a, o, n) -> {
            correctionsZoom.doHorizontalZoom(n.doubleValue(), ((SpecialNumberAxis) orbitChart.getXAxis()).getUpperBound());
            latticeZoom.doHorizontalZoom(n.doubleValue(), ((SpecialNumberAxis) orbitChart.getXAxis()).getUpperBound());
        });
        ((SpecialNumberAxis) correctionsChart.getXAxis()).lowerBoundProperty().addListener((a, o, n) -> {
            orbitZoom.doHorizontalZoom(n.doubleValue(), ((SpecialNumberAxis) correctionsChart.getXAxis()).getUpperBound());
            latticeZoom.doHorizontalZoom(n.doubleValue(), ((SpecialNumberAxis) correctionsChart.getXAxis()).getUpperBound());
        });
        ((SpecialNumberAxis) latticeChart.getXAxis()).lowerBoundProperty().addListener((a, o, n) -> {
            correctionsZoom.doHorizontalZoom(n.doubleValue(), ((SpecialNumberAxis) latticeChart.getXAxis()).getUpperBound());
            orbitZoom.doHorizontalZoom(n.doubleValue(), ((SpecialNumberAxis) latticeChart.getXAxis()).getUpperBound());
        });
        ((SpecialNumberAxis) orbitChart.getXAxis()).upperBoundProperty().addListener((a, o, n) -> {
            correctionsZoom.doHorizontalZoom(((SpecialNumberAxis) orbitChart.getXAxis()).getLowerBound(), n.doubleValue());
            latticeZoom.doHorizontalZoom(((SpecialNumberAxis) orbitChart.getXAxis()).getLowerBound(), n.doubleValue());
        });
        ((SpecialNumberAxis) correctionsChart.getXAxis()).upperBoundProperty().addListener((a, o, n) -> {
            orbitZoom.doHorizontalZoom(((SpecialNumberAxis) correctionsChart.getXAxis()).getLowerBound(), n.doubleValue());
            latticeZoom.doHorizontalZoom(((SpecialNumberAxis) correctionsChart.getXAxis()).getLowerBound(), n.doubleValue());
        });
        ((SpecialNumberAxis) latticeChart.getXAxis()).upperBoundProperty().addListener((a, o, n) -> {
            correctionsZoom.doHorizontalZoom(((SpecialNumberAxis) latticeChart.getXAxis()).getLowerBound(), n.doubleValue());
            orbitZoom.doHorizontalZoom(((SpecialNumberAxis) latticeChart.getXAxis()).getLowerBound(), n.doubleValue());
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
     * @param chart
     *            the chart to provide the axis bounds
     * @param label
     *            the label for the axis
     * @return a node containing the axis that can be added to pane next to a chart
     */
    private static Node createVerticalAxis(LineChart<Number, Number> chart, String label) {
        NumberAxis axis = new NumberAxis();
        axis.autoRangingProperty().set(false);
        axis.setSide(Side.LEFT);
        axis.upperBoundProperty().bind(((NumberAxis) chart.getYAxis()).upperBoundProperty());
        axis.lowerBoundProperty().bind(((NumberAxis) chart.getYAxis()).lowerBoundProperty());
        axis.tickUnitProperty().bind(((NumberAxis) chart.getYAxis()).tickUnitProperty());
        axis.setLabel(label);
        axis.setTickMarkVisible(true);
        axis.setMinorTickVisible(false);
        axis.setTickLabelsVisible(true);
        axis.setTickLabelGap(2);
        GridPane axisPane = new GridPane();
        setGridConstraints(axis, true, true, Priority.ALWAYS, Priority.ALWAYS);
        setGridConstraints(axisPane, true, true, Priority.ALWAYS, Priority.ALWAYS);
        axisPane.setPadding(new Insets(10, 0, 3, 0));
        axisPane.add(axis, 0, 0);
        axisPane.translateXProperty().set(15);
        return axisPane;
    }

    private Region createOrbitChart() {
        final ValueAxis<Number> xAxis = new SpecialNumberAxis(-5, 185, 5);
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setAnimated(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setMinorTickVisible(false);
        yAxis.autoRangingProperty().set(true);

        orbitChart = new LineChart<>(xAxis, yAxis);
        orbitChart.setLegendVisible(false);
        orbitChart.getStyleClass().add("orbit-chart");

        addSeries(ChartType.ORBIT, SeriesType.HORIZONTAL_ORBIT, false);
        addSeries(ChartType.ORBIT, SeriesType.VERTICAL_ORBIT, false);
        addSeries(ChartType.ORBIT, SeriesType.GOLDEN_HORIZONTAL_ORBIT, false);
        addSeries(ChartType.ORBIT, SeriesType.GOLDEN_VERTICAL_ORBIT, false);

        orbitZoom = new ZoomableLineChart(orbitChart, false, true, true);
        orbitZoom.setMinWidth(0);
        orbitZoom.setMaxWidth(Integer.MAX_VALUE);
        orbitZoom.setMinHeight(0);
        orbitZoom.setMaxHeight(Integer.MAX_VALUE);

        return orbitZoom;
    }

    private Region createCorrectionsChart() {
        final ValueAxis<Number> xAxis = new SpecialNumberAxis(-5, 185, 5);
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setAnimated(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setMinorTickVisible(false);
        yAxis.autoRangingProperty().set(true);

        correctionsChart = new CorrectionsChart<Number, Number>(xAxis, yAxis);
        correctionsChart.setLegendVisible(false);
        correctionsChart.getStyleClass().add("corrections-chart");

        addSeries(ChartType.CORRECTIONS, SeriesType.HORIZONTAL_CORRECTORS_CORRECTION, false);
        addSeries(ChartType.CORRECTIONS, SeriesType.VERTICAL_CORRECTORS_CORRECTION, false);

        correctionsZoom = new ZoomableLineChart(correctionsChart, false, true, true);
        correctionsZoom.setMinWidth(0);
        correctionsZoom.setMaxWidth(Integer.MAX_VALUE);
        correctionsZoom.setMinHeight(0);
        correctionsZoom.setMaxHeight(Integer.MAX_VALUE);

        return correctionsZoom;
    }

    private Region createLatticeChart() {
        final ValueAxis<Number> xAxis = new SpecialNumberAxis(-5, 185, 5);
        final NumberAxis yAxis = new NumberAxis(-50, 50, 10);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setMinorTickVisible(false);
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(true);

        latticeChart = new LineChart<>(xAxis, yAxis);
        latticeChart.setLegendVisible(false);
        latticeChart.getStyleClass().add("lattice-chart");

        addSeries(ChartType.LATTICE, SeriesType.BPM, false);
        addSeries(ChartType.LATTICE, SeriesType.HORIZONTAL_CORRECTORS, false);
        addSeries(ChartType.LATTICE, SeriesType.VERTICAL_CORRECTORS, false);

        latticeZoom = new ZoomableLineChart(latticeChart, false, true, false);
        latticeZoom.setMinWidth(0);
        latticeZoom.setMaxWidth(Integer.MAX_VALUE);
        final int height = 80;
        latticeChart.setMaxHeight(height);
        latticeChart.setMinHeight(height);
        latticeChart.setPrefHeight(height);

        return latticeZoom;
    }

    private GridPane createOrbitChartLegend() {
        GridPane legend = new GridPane();
        legend.setVgap(5);
        legend.setPadding(new Insets(10, 0, 0, 0));

        CheckBox hOrbitCheckBox = new TooltipCheckBox("Horizontal Orbit");
        hOrbitCheckBox.setSelected(true);
        hOrbitCheckBox.setOnAction(e -> {
            if (hOrbitCheckBox.isSelected()) {
                addSeries(ChartType.ORBIT, SeriesType.HORIZONTAL_ORBIT, false);
            } else {
                removeSeries(ChartType.ORBIT, SeriesType.HORIZONTAL_ORBIT);
            }
        });
        hOrbitCheckBox.getStyleClass().add("horizontal-check-box");

        CheckBox vOrbitCheckBox = new TooltipCheckBox("Vertical Orbit");
        vOrbitCheckBox.setSelected(true);
        vOrbitCheckBox.setOnAction(e -> {
            if (vOrbitCheckBox.isSelected()) {
                addSeries(ChartType.ORBIT, SeriesType.VERTICAL_ORBIT, false);
            } else {
                removeSeries(ChartType.ORBIT, SeriesType.VERTICAL_ORBIT);
            }
        });
        vOrbitCheckBox.getStyleClass().add("vertical-check-box");

        CheckBox hGoldenOrbitCheckBox = new TooltipCheckBox("Golden Horizontal Orbit");
        hGoldenOrbitCheckBox.setSelected(true);
        hGoldenOrbitCheckBox.setOnAction(e -> {
            if (hGoldenOrbitCheckBox.isSelected()) {
                addSeries(ChartType.ORBIT, SeriesType.GOLDEN_HORIZONTAL_ORBIT, false);
            } else {
                removeSeries(ChartType.ORBIT, SeriesType.GOLDEN_HORIZONTAL_ORBIT);
            }
        });
        hGoldenOrbitCheckBox.getStyleClass().add("golden-horizontal-check-box");

        CheckBox vGoldenOrbitCheckBox = new TooltipCheckBox("Golden Vertical Orbit");
        vGoldenOrbitCheckBox.setSelected(true);
        vGoldenOrbitCheckBox.setOnAction(e -> {
            if (vGoldenOrbitCheckBox.isSelected()) {
                addSeries(ChartType.ORBIT, SeriesType.GOLDEN_VERTICAL_ORBIT, false);
            } else {
                removeSeries(ChartType.ORBIT, SeriesType.GOLDEN_VERTICAL_ORBIT);
            }
        });
        vGoldenOrbitCheckBox.getStyleClass().add("golden-vertical-check-box");

        legend.add(hOrbitCheckBox, 0, 0);
        legend.add(vOrbitCheckBox, 0, 1);
        legend.add(hGoldenOrbitCheckBox, 0, 2);
        legend.add(vGoldenOrbitCheckBox, 0, 3);
        legend.setMinHeight(0);
        legend.setMinWidth(0);
        legend.setMaxWidth(Integer.MAX_VALUE);

        return legend;
    }

    private GridPane createCorrectionsChartLegend() {
        GridPane legend = new GridPane();
        legend.setPadding(new Insets(10, 0, 0, 0));
        legend.setVgap(10);

        GridPane checkBoxes = new GridPane();
        checkBoxes.setVgap(5);

        CheckBox hCheckBox = new TooltipCheckBox("Horizontal Correctors");
        hCheckBox.setSelected(true);
        hCheckBox.setOnAction(e -> {
            if (hCheckBox.isSelected()) {
                addSeries(ChartType.CORRECTIONS, SeriesType.HORIZONTAL_CORRECTORS_CORRECTION, false);
            } else {
                removeSeries(ChartType.CORRECTIONS, SeriesType.HORIZONTAL_CORRECTORS_CORRECTION);
            }
        });
        hCheckBox.getStyleClass().add("horizontal-check-box");

        CheckBox vCheckBox = new TooltipCheckBox("Vertical Correctors");
        vCheckBox.setSelected(true);
        vCheckBox.setOnAction(e -> {
            if (vCheckBox.isSelected()) {
                addSeries(ChartType.CORRECTIONS, SeriesType.VERTICAL_CORRECTORS_CORRECTION, false);
            } else {
                removeSeries(ChartType.CORRECTIONS, SeriesType.VERTICAL_CORRECTORS_CORRECTION);
            }
        });
        vCheckBox.getStyleClass().add("vertical-check-box");

        checkBoxes.add(hCheckBox, 0, 0);
        checkBoxes.add(vCheckBox, 0, 1);

        GridPane radioButtons = new GridPane();
        radioButtons.setVgap(5);

        ToggleGroup radioButtonsGroup = new ToggleGroup();
        RadioButton mradRadioButton = new RadioButton("mrad");
        mradRadioButton.setToggleGroup(radioButtonsGroup);
        mradRadioButton.setSelected(true);

        RadioButton maRadioButton = new RadioButton("mA");
        maRadioButton.setToggleGroup(radioButtonsGroup);

        radioButtons.add(mradRadioButton, 0, 0);
        radioButtons.add(maRadioButton, 0, 1);

        legend.add(checkBoxes, 0, 0);
        legend.add(radioButtons, 0, 2);

        legend.setMinHeight(0);
        legend.setMinWidth(0);
        legend.setMaxWidth(Integer.MAX_VALUE);
        return legend;
    }

    private GridPane createLatticeChartLegend() {
        GridPane legend = new GridPane();
        legend.setPadding(new Insets(10, 0, 0, 0));
        legend.setVgap(5);

        CheckBox bpmCheckBox = new TooltipCheckBox("BPMs");
        bpmCheckBox.setSelected(true);
        bpmCheckBox.setOnAction(e -> {
            if (bpmCheckBox.isSelected()) {
                addSeries(ChartType.LATTICE, SeriesType.BPM, false);
            } else {
                removeSeries(ChartType.LATTICE, SeriesType.BPM);
            }
        });
        bpmCheckBox.getStyleClass().add("bpm-check-box");

        CheckBox hCheckBox = new TooltipCheckBox("Horizontal Correctors");
        hCheckBox.setSelected(true);
        hCheckBox.setOnAction(e -> {
            if (hCheckBox.isSelected()) {
                addSeries(ChartType.LATTICE, SeriesType.HORIZONTAL_CORRECTORS, false);
            } else {
                removeSeries(ChartType.LATTICE, SeriesType.HORIZONTAL_CORRECTORS);
            }
        });
        hCheckBox.getStyleClass().add("horizontal-check-box");

        CheckBox vCheckBox = new TooltipCheckBox("Vertical Correctors");
        vCheckBox.setSelected(true);
        vCheckBox.setOnAction(e -> {
            if (vCheckBox.isSelected()) {
                addSeries(ChartType.LATTICE, SeriesType.VERTICAL_CORRECTORS, false);
            } else {
                removeSeries(ChartType.LATTICE, SeriesType.VERTICAL_CORRECTORS);
            }
        });
        vCheckBox.getStyleClass().add("vertical-check-box");

        legend.add(bpmCheckBox, 0, 0);
        legend.add(hCheckBox, 0, 1);
        legend.add(vCheckBox, 0, 2);

        legend.setMinHeight(0);
        legend.setMinWidth(0);
        legend.setMaxWidth(Integer.MAX_VALUE);
        return legend;
    }

    private GridPane createControls() {
        GridPane controls = new GridPane();
        controls.setHgap(10);
        controls.setVgap(10);
        controls.setPadding(new Insets(10, 10, 0, 64));

        controls.getColumnConstraints().setAll(new PercentColumnConstraints(40), new PercentColumnConstraints(16),
                new ColumnConstraints(0, 150, 150), new ColumnConstraints());

        Node correctionResults = createCorrectionResults();
        Node orbitCorrectionControl = createOrbitCorrectionControls();
        Region messageLog = createMessageLog();
        Node engineeringControls = createEngineeringScreensControls();

        setFullResizable(messageLog);
        controls.add(correctionResults, 0, 0);
        controls.add(messageLog, 3, 0);
        controls.add(orbitCorrectionControl, 0, 1);
        controls.add(createGoldenOrbitControls(), 1, 1);
        controls.add(createResponseMatrixControls(), 1, 2);
        controls.add(engineeringControls, 2, 1);

        GridPane.setColumnSpan(correctionResults, 3);
        GridPane.setRowSpan(messageLog, 3);
        GridPane.setRowSpan(orbitCorrectionControl, 2);
        GridPane.setRowSpan(engineeringControls, 2);
        return controls;
    }

    private BorderedTitledPane createCorrectionResults() {
        GridPane correctionResults = new GridPane();
        OrbitCorrectionResultsTable correctionResultsTable = new OrbitCorrectionResultsTable();

        // FIXME -> start tmp code
        List<OrbitCorrectionResultsEntry> entries = controller.getOrbitCorrectionResults();
        correctionResultsTable.updateTable(entries);
        // FIXME <- end tmp code

        correctionResults.add(correctionResultsTable, 0, 0);
        setGridConstraints(correctionResultsTable, true, true, Priority.ALWAYS, Priority.ALWAYS);
        return new BorderedTitledPane("Orbit Correction Results", correctionResults);
    }

    private BorderedTitledPane createMessageLog() {
        GridPane messageLog = new GridPane();

        StaticTextArea messageLogTextArea = new StaticTextArea();
        messageLogTextArea.setMaxHeight(Double.MAX_VALUE);
        messageLog.add(messageLogTextArea, 0, 0);

        setFullResizable(messageLogTextArea);
        messageLog.setMinSize(0, 0);
        messageLog.setMaxSize(Integer.MAX_VALUE, Integer.MAX_VALUE);

        return new BorderedTitledPane("Message Log", messageLog);
    }

    private BorderedTitledPane createOrbitCorrectionControls() {
        GridPane orbitCorrectionControl = new GridPane();
        orbitCorrectionControl.setHgap(10);
        orbitCorrectionControl.setVgap(10);

        orbitCorrectionControl.getColumnConstraints().setAll(PercentColumnConstraints.createEqualsConstraints(4));

        orbitCorrectionControl.getRowConstraints().setAll(PercentRowConstraints.createEqualsConstraints(2));

        final Button startMeasuringOrbitButton = new MultiLineButton("Start Measuring Orbit");
        startMeasuringOrbitButton.setOnAction(e -> {

        });

        final Button measureOrbitOnceButton = new MultiLineButton("Measure Orbit Once");
        measureOrbitOnceButton.setWrapText(true);
        measureOrbitOnceButton.setOnAction(e -> {

        });

        final Button startOrbitCorrectionButton = new MultiLineButton("Start Orbit Correction");
        startOrbitCorrectionButton.setWrapText(true);
        startOrbitCorrectionButton.setOnAction(e -> {

        });

        final Button exportCurrentOrbitButton = new MultiLineButton("Export Current Orbit");
        exportCurrentOrbitButton.setWrapText(true);
        exportCurrentOrbitButton.setOnAction(e -> {

        });

        final Button stopMeasuringOrbit = new MultiLineButton("Stop Measuring Orbit");
        stopMeasuringOrbit.setWrapText(true);
        stopMeasuringOrbit.setOnAction(e -> {

        });

        final Button correctOrbitOnceButton = new MultiLineButton("Correct Orbit Once");
        correctOrbitOnceButton.setWrapText(true);
        correctOrbitOnceButton.setOnAction(e -> {

        });

        final Button stopOrbitCorrectionButton = new MultiLineButton("Stop Orbit Correction");
        stopOrbitCorrectionButton.setWrapText(true);
        stopOrbitCorrectionButton.setOnAction(e -> {

        });

        final Button advancedButton = new MultiLineButton("Advanced...");
        advancedButton.setWrapText(true);
        advancedButton.setOnAction(e -> {

        });

        setFullResizable(startMeasuringOrbitButton, measureOrbitOnceButton, startOrbitCorrectionButton,
                exportCurrentOrbitButton, stopMeasuringOrbit, correctOrbitOnceButton, stopOrbitCorrectionButton,
                advancedButton);

        orbitCorrectionControl.add(startMeasuringOrbitButton, 0, 0);
        orbitCorrectionControl.add(measureOrbitOnceButton, 1, 0);
        orbitCorrectionControl.add(startOrbitCorrectionButton, 2, 0);
        orbitCorrectionControl.add(exportCurrentOrbitButton, 3, 0);
        orbitCorrectionControl.add(stopMeasuringOrbit, 0, 1);
        orbitCorrectionControl.add(correctOrbitOnceButton, 1, 1);
        orbitCorrectionControl.add(stopOrbitCorrectionButton, 2, 1);
        orbitCorrectionControl.add(advancedButton, 3, 1);

        return new BorderedTitledPane("Orbit Correction Control", orbitCorrectionControl);
    }

    private static void setFullResizable(Region... components) {
        for (Region component : components) {
            setGridConstraints(component, true, true, Priority.ALWAYS, Priority.ALWAYS);
            component.setMinSize(0, 0);
            component.setMaxSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
    }

    private BorderedTitledPane createGoldenOrbitControls() {
        GridPane goldenOrbitControl = new GridPane();
        goldenOrbitControl.setHgap(10);
        goldenOrbitControl.setVgap(10);

        goldenOrbitControl.getColumnConstraints().setAll(PercentColumnConstraints.createEqualsConstraints(3));

        final Button uploadButton = new Button("Upload");
        uploadButton.setOnAction(e -> {

        });

        final Button downloadButton = new Button("Download");
        downloadButton.setOnAction(e -> {

        });

        final Button useCurrentButton = new Button("Use current");
        useCurrentButton.setOnAction(e -> {

        });

        setFullResizable(uploadButton, downloadButton, useCurrentButton);

        goldenOrbitControl.add(uploadButton, 0, 0);
        goldenOrbitControl.add(downloadButton, 1, 0);
        goldenOrbitControl.add(useCurrentButton, 2, 0);

        return new BorderedTitledPane("Golden Orbit", goldenOrbitControl);
    }

    private BorderedTitledPane createResponseMatrixControls() {
        GridPane responseMatrixControl = new GridPane();
        responseMatrixControl.setHgap(10);
        responseMatrixControl.setVgap(10);
        responseMatrixControl.setPadding(new Insets(0, 10, 0, 10));

        responseMatrixControl.getColumnConstraints().setAll(PercentColumnConstraints.createEqualsConstraints(3));

        final Button downloadButton = new Button("Download");
        downloadButton.setOnAction(e -> {

        });

        final Button uploadButton = new Button("Upload");
        uploadButton.setOnAction(e -> {

        });

        final Button measureButton = new Button("Measure");
        measureButton.setOnAction(e -> {

        });

        setFullResizable(uploadButton, downloadButton, measureButton);

        responseMatrixControl.add(uploadButton, 0, 0);
        responseMatrixControl.add(downloadButton, 1, 0);
        responseMatrixControl.add(measureButton, 2, 0);

        return new BorderedTitledPane("Response Matrix", responseMatrixControl);
    }

    private BorderedTitledPane createEngineeringScreensControls() {
        GridPane engineeringScreensControl = new GridPane();
        engineeringScreensControl.setHgap(10);
        engineeringScreensControl.setVgap(10);

        engineeringScreensControl.getRowConstraints().setAll(PercentRowConstraints.createEqualsConstraints(2));

        final Button bpmControlButton = new MultiLineButton("BPM Control");
        bpmControlButton.setOnAction(e -> {

        });

        final Button correctorsControlButton = new MultiLineButton("Correctors Control");
        correctorsControlButton.setOnAction(e -> {

        });

        setFullResizable(bpmControlButton, correctorsControlButton);
        engineeringScreensControl.add(bpmControlButton, 0, 0);
        engineeringScreensControl.add(correctorsControlButton, 0, 1);

        return new BorderedTitledPane("Engineering Screens", engineeringScreensControl);
    }

    // TODO make method nicer?
    private void addSeries(ChartType chartType, SeriesType seriesType, boolean empty) {
        Series<Number, Number> series = new Series<>("Empty Series", FXCollections.emptyObservableList());
        if (!empty) {
            series = new Series<>(seriesType.getSeriesName(), getData(chartType, seriesType));
        }
        if (chartType == ChartType.ORBIT) {
            if (orbitChart.getData().size() > seriesType.getSeriesIndex()) {
                orbitChart.getData().set(seriesType.getSeriesIndex(), series);
            } else {
                orbitChart.getData().add(series);
            }
        } else if (chartType == ChartType.CORRECTIONS) {
            if (correctionsChart.getData().size() > seriesType.getSeriesIndex()) {
                correctionsChart.getData().set(seriesType.getSeriesIndex(), series);
            } else {
                correctionsChart.getData().add(series);
            }
        } else if (chartType == ChartType.LATTICE) {
            if (latticeChart.getData().size() > seriesType.getSeriesIndex()) {
                latticeChart.getData().set(seriesType.getSeriesIndex(), series);
            } else {
                latticeChart.getData().add(series);
            }
        }
    }

    private void removeSeries(ChartType chartType, SeriesType seriesType) {
        ObservableList<Series<Number, Number>> data = FXCollections.emptyObservableList();
        if (chartType == ChartType.ORBIT) {
            data = orbitChart.getData();
        } else if (chartType == ChartType.CORRECTIONS) {
            data = correctionsChart.getData();
        } else if (chartType == ChartType.LATTICE) {
            data = latticeChart.getData();
        }
        if (data != null && !data.isEmpty()) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getName().equals(seriesType.getSeriesName())) {
                    addSeries(chartType, seriesType, true);
                    break;
                }
            }
        }
    }

    private ObservableList<Data<Number, Number>> getData(ChartType chartType, SeriesType seriesType) {
        ObservableList<Data<Number, Number>> data = FXCollections.observableArrayList();
        controller.getBpms().forEach(bpm -> {
            Data<Number, Number> dataPoint = new Data<>();
            dataPoint.setXValue(bpm.getElementData().getPosition());

            if (chartType == ChartType.ORBIT) {
                if (seriesType == SeriesType.VERTICAL_ORBIT) {
                    dataPoint.YValueProperty().bind(bpm.verticalOrbitProperty());
                } else if (seriesType == SeriesType.HORIZONTAL_ORBIT) {
                    dataPoint.YValueProperty().bind(bpm.horizontalOrbitProperty());
                } else if (seriesType == SeriesType.GOLDEN_VERTICAL_ORBIT) {
                    dataPoint.YValueProperty().bind(bpm.goldenVerticalOrbitProperty());
                } else if (seriesType == SeriesType.GOLDEN_HORIZONTAL_ORBIT) {
                    dataPoint.YValueProperty().bind(bpm.goldenHorizontalOrbitProperty());
                }
            } else if (chartType == ChartType.LATTICE) {
                if (seriesType == SeriesType.BPM) {
                    dataPoint.setYValue(0.0);
                }
            }
            if (dataPoint.getYValue() != null || dataPoint.YValueProperty().isBound()) {
                data.add(dataPoint);
            }
        });

        controller.getCorrectors().forEach(corrector -> {
            Data<Number, Number> dataPoint = new Data<>();
            dataPoint.setXValue(corrector.getElementData().getPosition());

            if (chartType == ChartType.CORRECTIONS) {
                if (seriesType == SeriesType.HORIZONTAL_CORRECTORS_CORRECTION
                        && corrector.getElementData().getType() == LatticeElementType.HORIZONTAL_CORRECTOR) {
                    dataPoint.YValueProperty().bind(corrector.horizontalCorrectionProperty());
                } else if (seriesType == SeriesType.VERTICAL_CORRECTORS_CORRECTION
                        && corrector.getElementData().getType() == LatticeElementType.VERTICAL_CORRECTOR) {
                    dataPoint.YValueProperty().bind(corrector.verticalCorrectionProperty());
                }
            } else if (chartType == ChartType.LATTICE) {
                if (seriesType == SeriesType.HORIZONTAL_CORRECTORS
                        && corrector.getElementData().getType() == LatticeElementType.HORIZONTAL_CORRECTOR) {
                    dataPoint.setYValue(0.0);
                } else if (seriesType == SeriesType.VERTICAL_CORRECTORS
                        && corrector.getElementData().getType() == LatticeElementType.VERTICAL_CORRECTOR) {
                    dataPoint.setYValue(0.0);
                }
            }
            if (dataPoint.getYValue() != null || dataPoint.YValueProperty().isBound()) {
                data.add(dataPoint);
            }
        });

        return data;
    }
}