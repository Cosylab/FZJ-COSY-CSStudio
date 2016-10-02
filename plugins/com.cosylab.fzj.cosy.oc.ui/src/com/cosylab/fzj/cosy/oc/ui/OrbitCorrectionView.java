package com.cosylab.fzj.cosy.oc.ui;

import static org.csstudio.ui.fx.util.FXUtilities.setGridConstraints;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.fx.ui.workbench3.FXViewPart;

import com.cosylab.fzj.cosy.oc.LatticeElementType;

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
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

public class OrbitCorrectionView extends FXViewPart {

    private LineChart<Number,Number> orbitChart;
    private CorrectionsChart<Number, Number> correctionsChart;
    private LineChart<Number,Number> latticeChart;
    private ZoomableLineChart orbitZoom;
    private ZoomableLineChart correctionsZoom;
    private ZoomableLineChart latticeZoom;
    
    private OrbitCorrectionController controller;

    public OrbitCorrectionView() {
        controller = new OrbitCorrectionController();
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

    private GridPane createContentPane() {
        GridPane contentPane = new GridPane();
        contentPane.getStyleClass().add("root");
        
        RowConstraints row1Constraint = new RowConstraints();
        RowConstraints row2Constraint = new RowConstraints();
        row2Constraint.setMaxHeight(310);
        row2Constraint.setMinHeight(230);
        contentPane.getRowConstraints().addAll(row1Constraint, row2Constraint);
        
        GridPane chartsPane = createCharts();
        setGridConstraints(chartsPane, true, true, Priority.ALWAYS, Priority.ALWAYS);
        contentPane.add(chartsPane, 0, 0);
        GridPane controlsPane = createControls();
        setGridConstraints(controlsPane, true, true, Priority.ALWAYS, Priority.ALWAYS);
        contentPane.add(controlsPane, 0, 1);

        return contentPane;
    }

    private GridPane createCharts() {
        GridPane charts = new GridPane();
        
        ColumnConstraints column0Constraint = new ColumnConstraints();
        column0Constraint.setMinWidth(50);
        column0Constraint.setMaxWidth(50);
        ColumnConstraints column1Constraint = new ColumnConstraints();
        column1Constraint.setPercentWidth(80);
        ColumnConstraints column2Constraint = new ColumnConstraints();
        column2Constraint.setMinWidth(150);
        column2Constraint.setMaxWidth(200);
        charts.getColumnConstraints().setAll(column0Constraint, column1Constraint, column2Constraint);

        Node orbitNode = createOrbitChart();
        Node orbitLegendNode = createOrbitChartLegend();
        setGridConstraints(orbitNode, true, true, Priority.ALWAYS, Priority.ALWAYS);
        setGridConstraints(orbitLegendNode, true, false, HPos.LEFT, VPos.TOP, Priority.NEVER, Priority.ALWAYS);
        
        Node correctionNode = createCorrectionsChart();
        Node correctionLegendNode = createCorrectionsChartLegend();
        setGridConstraints(correctionNode, true, true, Priority.ALWAYS, Priority.ALWAYS);
        setGridConstraints(correctionLegendNode, true, false, HPos.LEFT, VPos.TOP, Priority.NEVER, Priority.ALWAYS);
        
        Node latticeNode = createLatticeChart();
        GridPane latticeLegendNode = createLatticeChartLegend();
        setGridConstraints(latticeNode, true, false, Priority.ALWAYS, Priority.NEVER);
        setGridConstraints(latticeLegendNode, true, false, HPos.LEFT, VPos.TOP, Priority.NEVER, Priority.NEVER);

        charts.add(createVerticalAxis(orbitChart, "Position [mm]"), 0, 0);
        charts.add(orbitNode, 1, 0);
        charts.add(orbitLegendNode, 2, 0);
        charts.add(createVerticalAxis(correctionsChart, "Kick Angle [mrad]"), 0,1);
        charts.add(correctionNode, 1, 1);
        charts.add(correctionLegendNode, 2, 1);
        charts.add(latticeNode, 1, 2);
        charts.add(latticeLegendNode, 2, 2);
        
        configureChartSynchronization();
        
        return charts;
    }
    
    private void configureChartSynchronization() {
        ChangeListener<Number> lowerSynchronizer = (a,o,n) -> {
            ((NumberAxis)orbitChart.getXAxis()).setLowerBound(n.doubleValue());
            ((NumberAxis)correctionsChart.getXAxis()).setLowerBound(n.doubleValue());
            ((NumberAxis)latticeChart.getXAxis()).setLowerBound(n.doubleValue());
        };
        ChangeListener<Number> upperSynchronizer = (a,o,n) -> {
            ((NumberAxis)orbitChart.getXAxis()).setUpperBound(n.doubleValue());
            ((NumberAxis)correctionsChart.getXAxis()).setUpperBound(n.doubleValue());
            ((NumberAxis)latticeChart.getXAxis()).setUpperBound(n.doubleValue());
        };
        
        ((NumberAxis)orbitChart.getXAxis()).lowerBoundProperty().addListener(lowerSynchronizer);
        ((NumberAxis)orbitChart.getXAxis()).upperBoundProperty().addListener(upperSynchronizer);
        ((NumberAxis)correctionsChart.getXAxis()).lowerBoundProperty().addListener(lowerSynchronizer);
        ((NumberAxis)correctionsChart.getXAxis()).upperBoundProperty().addListener(upperSynchronizer);
        ((NumberAxis)latticeChart.getXAxis()).lowerBoundProperty().addListener(lowerSynchronizer);
        ((NumberAxis)latticeChart.getXAxis()).upperBoundProperty().addListener(upperSynchronizer);
        
        ChangeListener<Boolean> zoomListener = (a,o,n) -> {
            if (n) {
                correctionsZoom.defaultZoom();
                latticeZoom.defaultZoom();
                orbitZoom.defaultZoom();
            } 
            orbitZoom.defaultZoomProperty().set(n);
            correctionsZoom.defaultZoomProperty().set(n);
            latticeZoom.defaultZoomProperty().set(n);
        };
        
        orbitZoom.defaultZoomProperty().addListener(zoomListener);
        correctionsZoom.defaultZoomProperty().addListener(zoomListener);
        latticeZoom.defaultZoomProperty().addListener(zoomListener);
    }
    
    private static Node createVerticalAxis(LineChart<Number, Number> chart, String label) {
        NumberAxis axis = new NumberAxis();
        axis.autoRangingProperty().set(false);
        axis.setSide(Side.LEFT);
        axis.upperBoundProperty().bind(((NumberAxis)chart.getYAxis()).upperBoundProperty());
        axis.lowerBoundProperty().bind(((NumberAxis)chart.getYAxis()).lowerBoundProperty());
        axis.tickUnitProperty().bind(((NumberAxis)chart.getYAxis()).tickUnitProperty());
        axis.setLabel(label);
        axis.setTickMarkVisible(true);
        axis.setTickLabelsVisible(true);
        axis.setTickLabelGap(2);
        GridPane axisPane = new GridPane();
        setGridConstraints(axis, true, true, Priority.ALWAYS, Priority.ALWAYS);
        setGridConstraints(axisPane, true, true, Priority.ALWAYS, Priority.ALWAYS);
        axisPane.setPadding(new Insets(10,0,2,0));
        axisPane.add(axis, 0, 0);
        axisPane.translateXProperty().set(15);
        return axisPane;
    }
    
    private GridPane createControls() {
        GridPane controls = new GridPane();
        controls.setHgap(10);
        controls.setVgap(10);
        controls.setPadding(new Insets(10, 10, 0, 67));

        ColumnConstraints column1Constraint = new ColumnConstraints();
        column1Constraint.setPercentWidth(35);
        ColumnConstraints column2Constraint = new ColumnConstraints();
        column2Constraint.setPercentWidth(35);
        ColumnConstraints column3Constraint = new ColumnConstraints();
        column3Constraint.setPercentWidth(30);
        controls.getColumnConstraints().addAll(column1Constraint, column2Constraint, column3Constraint);

//        RowConstraints row1Constraint = new RowConstraints();
//        row1Constraint.setPercentHeight(50);
//        RowConstraints row2Constraint = new RowConstraints();
//        row2Constraint.setPercentHeight(25);
//        RowConstraints row3Constraint = new RowConstraints();
//        row3Constraint.setPercentHeight(25);
//        controls.getRowConstraints().addAll(row1Constraint, row2Constraint, row3Constraint);

        BorderedTitledPane correctionResults = createCorrectionResults();
        BorderedTitledPane orbitCorrectionControl = createOrbitCorrectionControls();
        BorderedTitledPane messageLog = createMessageLog();

        controls.add(correctionResults, 0, 0);
        controls.add(messageLog, 2, 0);
        controls.add(orbitCorrectionControl, 0, 1);
        controls.add(createGoldenOrbitControls(), 1, 1);
        controls.add(createResponseMatrixControls(), 1, 2);
        controls.add(createEngineeringScreensControls(), 2, 2);

        GridPane.setColumnSpan(correctionResults, 2);
        GridPane.setRowSpan(messageLog, 2);
        GridPane.setRowSpan(orbitCorrectionControl, 2);
        return controls;
    }

    private Node createOrbitChart() {
        final NumberAxis xAxis = new NumberAxis(-5, 190, 20);
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setAnimated(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.autoRangingProperty().set(true);

        orbitChart = new LineChart<>(xAxis, yAxis);
        orbitChart.setLegendVisible(false);
        orbitChart.getStyleClass().add("orbit-chart");

        addSeries(ChartType.ORBIT, SeriesType.HORIZONTAL_ORBIT, false);
        addSeries(ChartType.ORBIT, SeriesType.VERTICAL_ORBIT, false);
        addSeries(ChartType.ORBIT, SeriesType.GOLDEN_HORIZONTAL_ORBIT, false);
        addSeries(ChartType.ORBIT, SeriesType.GOLDEN_VERTICAL_ORBIT, false);

        orbitZoom = new ZoomableLineChart(orbitChart,false,true,true);
        orbitZoom.setMinWidth(0);
        orbitZoom.setMaxWidth(Integer.MAX_VALUE);
        return orbitZoom;
    }

    private Node createCorrectionsChart() {
        final NumberAxis xAxis = new NumberAxis(-5, 190, 20);
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setAnimated(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.autoRangingProperty().set(true);

        correctionsChart = new CorrectionsChart<Number, Number>(xAxis, yAxis);
        correctionsChart.setLegendVisible(false);
        correctionsChart.getStyleClass().add("corrections-chart");

        addSeries(ChartType.CORRECTIONS, SeriesType.HORIZONTAL_CORRECTORS_CORRECTION, false);
        addSeries(ChartType.CORRECTIONS, SeriesType.VERTICAL_CORRECTORS_CORRECTION, false);
        
        correctionsZoom = new ZoomableLineChart(correctionsChart,false,true,true);
        correctionsZoom.setMinWidth(0);
        correctionsZoom.setMaxWidth(Integer.MAX_VALUE);
        
        return correctionsZoom;
    }

    private Node createLatticeChart() {
        final NumberAxis xAxis = new NumberAxis(-5, 190, 5);
        final NumberAxis yAxis = new NumberAxis(-50, 50, 10);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(true);

        latticeChart = new LineChart<>(xAxis, yAxis);
        latticeChart.setLegendVisible(false);
        latticeChart.getStyleClass().add("lattice-chart");

        addSeries(ChartType.LATTICE, SeriesType.BPM, false);
        addSeries(ChartType.LATTICE, SeriesType.HORIZONTAL_CORRECTORS, false);
        addSeries(ChartType.LATTICE, SeriesType.VERTICAL_CORRECTORS, false);
        
        latticeZoom = new ZoomableLineChart(latticeChart, false,true,false);
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
        legend.setPadding(new Insets(10,0,0,0));

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

        return legend;
    }

    private GridPane createCorrectionsChartLegend() {
        GridPane legend = new GridPane();
        legend.setPadding(new Insets(10,0,0,0));
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

        return legend;
    }

    private GridPane createLatticeChartLegend() {
        GridPane legend = new GridPane();
        legend.setPadding(new Insets(10,0,0,0));
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

        return legend;
    }

    private BorderedTitledPane createCorrectionResults() {
        GridPane correctionResults = new GridPane();
        correctionResults.setHgap(10);
        correctionResults.setVgap(10);
        correctionResults.setPadding(new Insets(0, 10, 0, 10));

        OrbitCorrectionResultsTable correctionResultsTable = new OrbitCorrectionResultsTable();

        // FIXME -> start tmp code
        List<OrbitCorrectionResultsEntry> entries = new ArrayList<>();
        OrbitCorrectionResultsEntry resultEntry = new OrbitCorrectionResultsEntry("Horizontal Orbit");
        entries.add(resultEntry);
        resultEntry = new OrbitCorrectionResultsEntry("Vertical Orbit");
        entries.add(resultEntry);
        resultEntry = new OrbitCorrectionResultsEntry("Golden Horizontal");
        entries.add(resultEntry);
        resultEntry = new OrbitCorrectionResultsEntry("Golden Vertical");
        entries.add(resultEntry);
        correctionResultsTable.updateTable(entries);
        // FIXME <- end tmp code

        correctionResults.add(correctionResultsTable, 0, 0);
        setGridConstraints(correctionResultsTable, true, false, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);

        return new BorderedTitledPane("Orbit Correction Results", correctionResults);
    }

    private BorderedTitledPane createMessageLog() {
        GridPane messageLog = new GridPane();
        messageLog.setHgap(10);
        messageLog.setVgap(10);
        messageLog.setPadding(new Insets(0, 10, 0, 10));

        TextArea messageLogTextArea = new TextArea();
        messageLogTextArea.setEditable(false);
        messageLogTextArea.setMaxHeight(Double.MAX_VALUE);
        messageLog.add(messageLogTextArea, 0, 0);

        setGridConstraints(messageLogTextArea, true, true, Priority.ALWAYS, Priority.ALWAYS);

        return new BorderedTitledPane("Message Log", messageLog);
    }

    private BorderedTitledPane createOrbitCorrectionControls() {
        GridPane orbitCorrectionControl = new GridPane();
        orbitCorrectionControl.setHgap(10);
        orbitCorrectionControl.setVgap(10);
        orbitCorrectionControl.setPadding(new Insets(0, 10, 0, 10));

        ColumnConstraints column1Constraint = new ColumnConstraints();
        column1Constraint.setPercentWidth(25);
        ColumnConstraints column2Constraint = new ColumnConstraints();
        column2Constraint.setPercentWidth(25);
        ColumnConstraints column3Constraint = new ColumnConstraints();
        column3Constraint.setPercentWidth(25);
        ColumnConstraints column4Constraint = new ColumnConstraints();
        column4Constraint.setPercentWidth(25);
        orbitCorrectionControl.getColumnConstraints().addAll(column1Constraint,
                column2Constraint, column3Constraint, column4Constraint);

        RowConstraints row1Constraint = new RowConstraints();
        row1Constraint.setPercentHeight(50);
        RowConstraints row2Constraint = new RowConstraints();
        row2Constraint.setPercentHeight(50);
        orbitCorrectionControl.getRowConstraints().addAll(row1Constraint, row2Constraint);

        final Button startMeasuringOrbitButton = new Button("Start Measuring Orbit");
        startMeasuringOrbitButton.setOnAction(e -> {

        });

        final Button measureOrbitOnceButton = new Button("Measure Orbit Once");
        measureOrbitOnceButton.setWrapText(true);
        measureOrbitOnceButton.setOnAction(e -> {

        });

        final Button startOrbitCorrectionButton = new Button("Start Orbit Correction");
        startOrbitCorrectionButton.setWrapText(true);
        startOrbitCorrectionButton.setOnAction(e -> {

        });

        final Button exportCurrentOrbitButton = new Button("Export Current Orbit");
        exportCurrentOrbitButton.setWrapText(true);
        exportCurrentOrbitButton.setOnAction(e -> {

        });

        final Button stopMeasuringOrbit = new Button("Stop Measuring Orbit");
        stopMeasuringOrbit.setWrapText(true);
        stopMeasuringOrbit.setOnAction(e -> {

        });

        final Button correctOrbitOnceButton = new Button("Correct Orbit Once");
        correctOrbitOnceButton.setWrapText(true);
        correctOrbitOnceButton.setOnAction(e -> {

        });

        final Button stopOrbitCorrectionButton = new Button("Stop Orbit Correction");
        stopOrbitCorrectionButton.setWrapText(true);
        stopOrbitCorrectionButton.setOnAction(e -> {

        });

        final Button advancedButton = new Button("Advanced...");
        advancedButton.setWrapText(true);
        advancedButton.setOnAction(e -> {

        });

        orbitCorrectionControl.add(startMeasuringOrbitButton, 0, 0);
        orbitCorrectionControl.add(measureOrbitOnceButton, 1, 0);
        orbitCorrectionControl.add(startOrbitCorrectionButton, 2, 0);
        orbitCorrectionControl.add(exportCurrentOrbitButton, 3, 0);
        orbitCorrectionControl.add(stopMeasuringOrbit, 0, 1);
        orbitCorrectionControl.add(correctOrbitOnceButton, 1, 1);
        orbitCorrectionControl.add(stopOrbitCorrectionButton, 2, 1);
        orbitCorrectionControl.add(advancedButton, 3, 1);

//        setGridConstraints(startMeasuringOrbitButton, false, false, HPos.CENTER, VPos.TOP, null, null);
//        setGridConstraints(measureOrbitOnceButton, false, false, HPos.CENTER, VPos.TOP, null, null);
//        setGridConstraints(startOrbitCorrectionButton, false, false, HPos.CENTER, VPos.TOP, null, null);
//        setGridConstraints(exportCurrentOrbitButton, false, false, HPos.CENTER, VPos.TOP, null, null);
//        setGridConstraints(stopMeasuringOrbit, false, false, HPos.CENTER, VPos.TOP, null, null);
//        setGridConstraints(correctOrbitOnceButton, false, false, HPos.CENTER, VPos.TOP, null, null);
//        setGridConstraints(stopOrbitCorrectionButton, false, false, HPos.CENTER, VPos.TOP, null, null);
//        setGridConstraints(advancedButton, false, false, HPos.CENTER, VPos.TOP, null, null);
//
//        GridPane.setMargin(exportCurrentOrbitButton, new Insets(0, 0, 0, 20));
//        GridPane.setMargin(advancedButton, new Insets(0, 0, 0, 20));

        return new BorderedTitledPane("Orbit Correction Control", orbitCorrectionControl);
    }

    private BorderedTitledPane createGoldenOrbitControls() {
        GridPane goldenOrbitControl = new GridPane();
        goldenOrbitControl.setHgap(10);
        goldenOrbitControl.setVgap(10);
        goldenOrbitControl.setPadding(new Insets(0, 10, 0, 10));

        ColumnConstraints column1Constraint = new ColumnConstraints();
        column1Constraint.setPercentWidth(33);
        ColumnConstraints column2Constraint = new ColumnConstraints();
        column2Constraint.setPercentWidth(33);
        ColumnConstraints column3Constraint = new ColumnConstraints();
        column3Constraint.setPercentWidth(33);
        goldenOrbitControl.getColumnConstraints().addAll(column1Constraint,
                column2Constraint, column3Constraint);

        RowConstraints row1Constraint = new RowConstraints();
        row1Constraint.setPercentHeight(100);
        goldenOrbitControl.getRowConstraints().addAll(row1Constraint);

        final Button uploadButton = new Button("Upload");
        uploadButton.setOnAction(e -> {

        });

        final Button downloadButton = new Button("Download");
        downloadButton.setOnAction(e -> {

        });

        final Button useCurrentButton = new Button("Use current");
        useCurrentButton.setOnAction(e -> {

        });

        goldenOrbitControl.add(uploadButton, 0, 0);
        goldenOrbitControl.add(downloadButton, 1, 0);
        goldenOrbitControl.add(useCurrentButton, 2, 0);

//        setGridConstraints(uploadButton, false, false, HPos.CENTER, VPos.CENTER, null, null);
//        setGridConstraints(downloadButton, false, false, HPos.CENTER, VPos.CENTER, null, null);
//        setGridConstraints(useCurrentButton, false, false, HPos.CENTER, VPos.CENTER, null, null);

        return new BorderedTitledPane("Golden Orbit", goldenOrbitControl);
    }

    private BorderedTitledPane createResponseMatrixControls() {
        GridPane responseMatrixControl = new GridPane();
        responseMatrixControl.setHgap(10);
        responseMatrixControl.setVgap(10);
        responseMatrixControl.setPadding(new Insets(0, 10, 0, 10));

        ColumnConstraints column1Constraint = new ColumnConstraints();
        column1Constraint.setPercentWidth(33);
        ColumnConstraints column2Constraint = new ColumnConstraints();
        column2Constraint.setPercentWidth(33);
        ColumnConstraints column3Constraint = new ColumnConstraints();
        column3Constraint.setPercentWidth(33);
        responseMatrixControl.getColumnConstraints().addAll(column1Constraint,
                column2Constraint, column3Constraint);

        RowConstraints row1Constraint = new RowConstraints();
        row1Constraint.setPercentHeight(100);
        responseMatrixControl.getRowConstraints().addAll(row1Constraint);

        final Button downloadButton = new Button("Download");
        downloadButton.setOnAction(e -> {

        });

        final Button uploadButton = new Button("Upload");
        uploadButton.setOnAction(e -> {

        });

        final Button measureButton = new Button("Measure");
        measureButton.setOnAction(e -> {

        });

        responseMatrixControl.add(downloadButton, 0, 0);
        responseMatrixControl.add(uploadButton, 1, 0);
        responseMatrixControl.add(measureButton, 2, 0);

//        setGridConstraints(downloadButton, false, false, HPos.CENTER, VPos.CENTER, null, null);
//        setGridConstraints(uploadButton, false, false, HPos.CENTER, VPos.CENTER, null, null);
//        setGridConstraints(measureButton, false, false, HPos.CENTER, VPos.CENTER, null, null);

        return new BorderedTitledPane("Response Matrix", responseMatrixControl);
    }

    private GridPane createEngineeringScreensControls() {
        GridPane engineeringScreensControl = new GridPane();
        engineeringScreensControl.setHgap(10);
        engineeringScreensControl.setVgap(10);

        ColumnConstraints column1Constraint = new ColumnConstraints();
        column1Constraint.setPercentWidth(80);
        ColumnConstraints column2Constraint = new ColumnConstraints();
        column2Constraint.setPercentWidth(20);
        engineeringScreensControl.getColumnConstraints().addAll(column1Constraint,
                column2Constraint);

        RowConstraints row1Constraint = new RowConstraints();
        row1Constraint.setPercentHeight(100);
        engineeringScreensControl.getRowConstraints().addAll(row1Constraint);

        final Button bpmControlButton = new Button("BPM Control");
        bpmControlButton.setOnAction(e -> {

        });

        final Button correctorsControlButton = new Button("Correctors Control");
        correctorsControlButton.setOnAction(e -> {

        });

//        engineeringScreensControl.add(bpmControlButton, 0, 0);
//        engineeringScreensControl.add(correctorsControlButton, 1, 0);
//
//        setGridConstraints(bpmControlButton, false, false, HPos.RIGHT, VPos.CENTER, null, null);
//        setGridConstraints(correctorsControlButton, false, false, HPos.RIGHT, VPos.CENTER, null, null);

        return engineeringScreensControl;
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
            for(int i = 0; i < data.size(); i++) {
                if (data.get(i).getName().equals(seriesType.getSeriesName())) {
                    addSeries(chartType, seriesType, true);
                    break;
                }
            }
        }
    }

    private ObservableList<Data<Number, Number>> getData(ChartType chartType, SeriesType seriesType) {
        ObservableList<Data<Number, Number>> data = FXCollections.observableArrayList();
        controller.getLatticeElementChartValues().forEach(element -> {
            LatticeElementType elementType = element.getElementType().get();
            Data<Number, Number> dataPoint = new Data<>();
            dataPoint.XValueProperty().bind(element.getPositionValue());
            if (chartType == ChartType.ORBIT) {
                if (seriesType == SeriesType.VERTICAL_ORBIT) {
                    dataPoint.YValueProperty().bind(element.getVerticalOrbitValue());
                } else if (seriesType == SeriesType.HORIZONTAL_ORBIT) {
                    dataPoint.YValueProperty().bind(element.getHorizontalOrbitValue());
                } else if (seriesType == SeriesType.GOLDEN_VERTICAL_ORBIT) {
                    dataPoint.YValueProperty().bind(element.getGoldenVerticalOrbitValue());
                } else if (seriesType == SeriesType.GOLDEN_HORIZONTAL_ORBIT) {
                    dataPoint.YValueProperty().bind(element.getGoldenHorizontalOrbitValue());
                }
            } else if (chartType == ChartType.CORRECTIONS) {
                if (seriesType == SeriesType.HORIZONTAL_CORRECTORS_CORRECTION) {
                    dataPoint.YValueProperty().bind(element.getHorizontalCorrectionValue());
                } else if (seriesType == SeriesType.VERTICAL_CORRECTORS_CORRECTION) {
                    dataPoint.YValueProperty().bind(element.getVerticalCorrectionValue());
                }
            } else if (chartType == ChartType.LATTICE){
                if (seriesType == SeriesType.BPM && elementType == LatticeElementType.BPM) {
                    dataPoint.setYValue(0.0);
                } else if (seriesType == SeriesType.HORIZONTAL_CORRECTORS && elementType == LatticeElementType.HORIZONTAL_CORRECTOR) {
                    dataPoint.setYValue(0.0);
                } else if (seriesType == SeriesType.VERTICAL_CORRECTORS && elementType ==LatticeElementType.VERTICAL_CORRECTOR) {
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