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

import static java.util.Optional.ofNullable;
import static org.csstudio.ui.fx.util.FXUtilities.setGridConstraints;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;

import org.csstudio.ui.fx.util.FXTextAreaInputDialog;
import org.eclipse.fx.ui.workbench3.FXViewPart;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.cosylab.fzj.cosy.oc.OrbitCorrectionPlugin;
import com.cosylab.fzj.cosy.oc.Preferences;
import com.cosylab.fzj.cosy.oc.ui.util.BorderedTitledPane;
import com.cosylab.fzj.cosy.oc.ui.util.MultiLineButton;

import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * <code>OrbitCorrectionControls</code> is an {@link FXViewPart} implementation displaying the controls for the orbit
 * correction application.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class OrbitCorrectionControls extends FXViewPart {

    private static enum OperationStatus {
        IDLE, MEASURING_ORBIT, CORRECTING_ORBIT, CORRECTING_ORM
    }

    private OrbitCorrectionController controller;
    private Scene scene;
    private FileChooser fileChooser;
    private AdvancedDialog advancedDialog;
    private AdvancedGoldenOrbitDialog advancedGoldernOrbitDialog;

    /*
     * (non-Javadoc)
     * @see org.eclipse.fx.ui.workbench3.FXViewPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        try {
            OrbitCorrectionView view = (OrbitCorrectionView)PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().findView(OrbitCorrectionView.ID);
            controller = view.getController();
        } catch (Exception e) {
            OrbitCorrectionPlugin.LOGGER.log(Level.SEVERE,
                    "Orbit Correction View could not be found. Using separate controller.",e);
            controller = new OrbitCorrectionController();
        }
        super.createPartControl(parent);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.fx.ui.workbench3.FXViewPart#createFxScene()
     */
    @Override
    protected Scene createFxScene() {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(0,new ExtensionFilter("All Files","*.*"));
        fileChooser.setInitialDirectory(Preferences.getInstance().getInitialDirectory());
        scene = new Scene(createContentPane());
        scene.getStylesheets().add(OrbitCorrectionView.class.getResource("style.css").toExternalForm());
        return scene;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.fx.ui.workbench3.FXViewPart#setFxFocus()
     */
    @Override
    protected void setFxFocus() {}

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
        contentPane.getRowConstraints().addAll(new RowConstraints(0,Region.USE_COMPUTED_SIZE,Double.MAX_VALUE));
        GridPane controlsPane = createControls();
        setFullResizable(controlsPane);
        contentPane.add(controlsPane,0,1);
        return contentPane;
    }

    /**
     * @return grid pane which contains orbit correction results table, orbit correction controls, golden orbit
     *         controls, response matrix controls, engineering screens controls and message log.
     */
    private GridPane createControls() {
        GridPane buttonControls = new GridPane();
        buttonControls.setHgap(10);
        buttonControls.setVgap(10);
        buttonControls.setPadding(new Insets(0,0,0,0));
        buttonControls.getColumnConstraints().setAll(new PercentColumnConstraints(55),new PercentColumnConstraints(25),
                new PercentColumnConstraints(20));
        buttonControls.getRowConstraints().setAll(PercentRowConstraints.createEqualsConstraints(2));
        Node orbitCorrectionControl = createOrbitCorrectionControls();
        buttonControls.add(orbitCorrectionControl,0,0);
        buttonControls.add(createGoldenOrbitControls(),1,0);
        buttonControls.add(createReferenceOrbitControls(),1,1);
        buttonControls.add(createResponseMatrixControls(),2,0);
        buttonControls.add(createEngineeringScreensControls(),2,1);
        GridPane.setRowSpan(orbitCorrectionControl,2);
        GridPane masterControls = new GridPane();
        masterControls.setHgap(10);
        masterControls.setVgap(10);
        masterControls.setPadding(new Insets(10,0,0,0));
        Region correctionResults = createCorrectionResults();
        masterControls.add(correctionResults,0,0);
        masterControls.add(buttonControls,0,1);
        OrbitCorrectionView.setFullResizable(correctionResults);
        return masterControls;
    }

    /**
     * @return titled pane with correction results table.
     */
    private BorderedTitledPane createCorrectionResults() {
        OrbitCorrectionResultsTable correctionResultsTable = new OrbitCorrectionResultsTable();
        correctionResultsTable.updateTable(new ArrayList<>(controller.getOrbitCorrectionResults().values()));
        return new BorderedTitledPane("Orbit Correction Results",correctionResultsTable);
    }

    /**
     * @return titled pane with orbit correction controls.
     */
    private BorderedTitledPane createOrbitCorrectionControls() {
        GridPane orbitCorrectionControl = new GridPane();
        orbitCorrectionControl.setHgap(10);
        orbitCorrectionControl.setVgap(10);
        orbitCorrectionControl.getColumnConstraints().setAll(PercentColumnConstraints.createEqualsConstraints(5));
        orbitCorrectionControl.getRowConstraints().setAll(PercentRowConstraints.createEqualsConstraints(2));
        final StringProperty status = controller.statusProperty();
        Button startMeasuringOrbitButton = new MultiLineButton("Start Measuring Orbit");
        startMeasuringOrbitButton.setTooltip(new Tooltip("Continuously measure the orbit"));
        startMeasuringOrbitButton.setWrapText(true);
        startMeasuringOrbitButton.getStyleClass().add("button");
        startMeasuringOrbitButton.setOnAction(e -> controller.startMeasuringOrbit());
        startMeasuringOrbitButton.disableProperty().bind(status.isNotEqualTo(OperationStatus.IDLE.toString()));
        Button measureOrbitOnceButton = new MultiLineButton("Measure Orbit Once");
        measureOrbitOnceButton.setTooltip(new Tooltip("Perform a single orbit measurement"));
        measureOrbitOnceButton.setWrapText(true);
        measureOrbitOnceButton.setOnAction(e -> controller.measureOrbitOnce());
        measureOrbitOnceButton.disableProperty().bind(status.isNotEqualTo(OperationStatus.IDLE.toString()));
        Button startOrbitCorrectionButton = new MultiLineButton("Start Orbit Correction");
        startOrbitCorrectionButton.setTooltip(new Tooltip("Start orbit correction in continuous mode"));
        startOrbitCorrectionButton.setWrapText(true);
        startOrbitCorrectionButton.setOnAction(e -> controller.startCorrectingOrbit());
        startOrbitCorrectionButton.disableProperty().bind(status.isNotEqualTo(OperationStatus.IDLE.toString()));
        Button exportCurrentOrbitButton = new MultiLineButton("Export Current Orbit");
        exportCurrentOrbitButton.setTooltip(new Tooltip("Export current orbit to a file"));
        exportCurrentOrbitButton.setWrapText(true);
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        exportCurrentOrbitButton.setOnAction(e -> {
            fileChooser.setInitialFileName("Orbit-" + format.format(new Date()));
            ofNullable(fileChooser.showSaveDialog(scene.getWindow())).ifPresent(file -> FXTextAreaInputDialog
                    .get(getSite().getShell(),"Exported Orbit Comment",
                            "Enter the comment to store into the orbit file:","",null)
                    .ifPresent(comment -> controller.exportCurrentOrbit(file,Optional.of(comment))));
        });

        Button stopMeasuringOrbitButton = new MultiLineButton("Stop Measuring Orbit");
        stopMeasuringOrbitButton.setTooltip(new Tooltip("Stop performing orbit measurement"));
        stopMeasuringOrbitButton.setWrapText(true);
        stopMeasuringOrbitButton.setOnAction(e -> controller.stopMeasuringOrbit());
        stopMeasuringOrbitButton.disableProperty()
                .bind(status.isNotEqualTo(OperationStatus.MEASURING_ORBIT.toString()));
        Button correctOrbitOnceButton = new MultiLineButton("Correct Orbit Once");
        correctOrbitOnceButton.setTooltip(new Tooltip("Perform a single orbit correction"));
        correctOrbitOnceButton.setWrapText(true);
        correctOrbitOnceButton.setOnAction(e -> controller.correctOrbitOnce());
        correctOrbitOnceButton.disableProperty().bind(status.isNotEqualTo(OperationStatus.IDLE.toString()));
        Button stopOrbitCorrectionButton = new MultiLineButton("Stop Orbit Correction");
        stopOrbitCorrectionButton.setTooltip(new Tooltip("Stop performing orbit correction"));
        stopOrbitCorrectionButton.setWrapText(true);
        stopOrbitCorrectionButton.setOnAction(e -> controller.stopCorrectingOrbit());
        stopOrbitCorrectionButton.disableProperty()
                .bind(status.isNotEqualTo(OperationStatus.CORRECTING_ORBIT.toString()));
        Button resetCorrectionButton = new MultiLineButton("Reset Correction");
        resetCorrectionButton.setTooltip(new Tooltip("Reset current correction to the current magnet setpoints"));
        resetCorrectionButton.setWrapText(true);
        resetCorrectionButton.getStyleClass().add("button");
        resetCorrectionButton.setOnAction(e -> controller.resetOrbitCorrection());
        resetCorrectionButton.disableProperty().bind(status.isNotEqualTo(OperationStatus.IDLE.toString()));
        Button advancedButton = new MultiLineButton("Advanced...");
        advancedButton.setWrapText(true);
        advancedButton.setOnAction(e -> getAdvancedDialog().open());
        setFullResizable(startMeasuringOrbitButton,measureOrbitOnceButton,startOrbitCorrectionButton,
                exportCurrentOrbitButton,stopMeasuringOrbitButton,correctOrbitOnceButton,stopOrbitCorrectionButton,
                advancedButton,resetCorrectionButton);
        orbitCorrectionControl.add(startMeasuringOrbitButton,0,0);
        orbitCorrectionControl.add(measureOrbitOnceButton,1,0);
        orbitCorrectionControl.add(startOrbitCorrectionButton,2,0);
        orbitCorrectionControl.add(resetCorrectionButton,3,0);
        orbitCorrectionControl.add(exportCurrentOrbitButton,4,0);
        orbitCorrectionControl.add(stopMeasuringOrbitButton,0,1);
        orbitCorrectionControl.add(correctOrbitOnceButton,1,1);
        orbitCorrectionControl.add(stopOrbitCorrectionButton,2,1);
        orbitCorrectionControl.add(advancedButton,4,1);
        return new BorderedTitledPane("Orbit Correction Control",orbitCorrectionControl);
    }

    private AdvancedDialog getAdvancedDialog() {
        if (advancedDialog == null) {
            advancedDialog = new AdvancedDialog(getViewSite().getShell(),controller);
        }
        return advancedDialog;
    }

    private AdvancedGoldenOrbitDialog getAdvancedGoldenOrbitDialog() {
        if (advancedGoldernOrbitDialog == null) {
            advancedGoldernOrbitDialog = new AdvancedGoldenOrbitDialog(getViewSite().getShell(),controller);
        }
        return advancedGoldernOrbitDialog;
    }

    /**
     * Sets given regions full resizable.
     */
    private static void setFullResizable(Region... components) {
        for (Region component : components) {
            setGridConstraints(component,true,true,Priority.ALWAYS,Priority.ALWAYS);
            component.setMinSize(0,0);
            component.setMaxSize(Integer.MAX_VALUE,Integer.MAX_VALUE);
        }
    }

    /**
     * @return titled pane with golden orbit controls.
     */
    private BorderedTitledPane createGoldenOrbitControls() {
        GridPane goldenOrbitControl = new GridPane();
        goldenOrbitControl.setHgap(10);
        goldenOrbitControl.setVgap(10);
        goldenOrbitControl.setPadding(new Insets(0,0,0,0));
        goldenOrbitControl.getColumnConstraints().setAll(PercentColumnConstraints.createEqualsConstraints(2));
        final StringProperty status = controller.statusProperty();
        Button useCurrentButton = new Button("Use current");
        useCurrentButton.setTooltip(new Tooltip("Use current orbit as the new golden orbit"));
        useCurrentButton.setOnAction(e -> controller.useCurrent());
        useCurrentButton.disableProperty().bind(status.isNotEqualTo(OperationStatus.IDLE.toString()));
        Button advancedButton = new Button("Advanced...");
        advancedButton.setTooltip(new Tooltip("Show advance golden orbit features"));
        advancedButton.setOnAction(e -> getAdvancedGoldenOrbitDialog().open());
        advancedButton.disableProperty().bind(status.isNotEqualTo(OperationStatus.IDLE.toString()));
        setFullResizable(useCurrentButton,advancedButton);
        goldenOrbitControl.add(useCurrentButton,0,0);
        goldenOrbitControl.add(advancedButton,1,0);
        return new BorderedTitledPane("Golden Orbit",goldenOrbitControl);
    }

    /**
     * @return titled pane with golden orbit controls.
     */
    private BorderedTitledPane createReferenceOrbitControls() {
        GridPane referenceOrbitControl = new GridPane();
        referenceOrbitControl.setHgap(10);
        referenceOrbitControl.setVgap(10);
        referenceOrbitControl.setPadding(new Insets(0,0,0,0));
        referenceOrbitControl.getColumnConstraints().setAll(PercentColumnConstraints.createEqualsConstraints(2));
        final StringProperty status = controller.statusProperty();
        Button horizontalButton = new Button("Horizontal");
        horizontalButton.setTooltip(new Tooltip("Set current horizontal orbit as a reference horizontal orbit"));
        horizontalButton.setOnAction(e -> controller.useCurrentAsReference(true));
        horizontalButton.disableProperty().bind(status.isNotEqualTo(OperationStatus.IDLE.toString()));
        Button verticalButton = new Button("Vertical");
        verticalButton.setTooltip(new Tooltip("Set current vertical orbit as a reference vertical orbit"));
        verticalButton.setOnAction(e -> controller.useCurrentAsReference(false));
        verticalButton.disableProperty().bind(status.isNotEqualTo(OperationStatus.IDLE.toString()));
        setFullResizable(horizontalButton,verticalButton);
        referenceOrbitControl.add(horizontalButton,0,0);
        referenceOrbitControl.add(verticalButton,1,0);
        return new BorderedTitledPane("Reference Orbit",referenceOrbitControl);
    }

    /**
     * @return titled pane with response matrix controls.
     */
    private BorderedTitledPane createResponseMatrixControls() {
        GridPane responseMatrixControl = new GridPane();
        responseMatrixControl.setHgap(10);
        responseMatrixControl.setVgap(10);
        responseMatrixControl.setPadding(new Insets(0,0,0,0));
        responseMatrixControl.getColumnConstraints().setAll(PercentColumnConstraints.createEqualsConstraints(1));
        Button measureButton = new Button("Measure");
        measureButton.setOnAction(e -> controller.measureOrbitResponseMatrix());
        measureButton.disableProperty().set(!Preferences.getInstance().getMeasureORMCommand().isPresent());
        setFullResizable(measureButton);
        responseMatrixControl.add(measureButton,0,0);
        return new BorderedTitledPane("Response Matrix",responseMatrixControl);
    }

    /**
     * @return titled pane with engineering screens controls.
     */
    private BorderedTitledPane createEngineeringScreensControls() {
        GridPane engineeringScreensControl = new GridPane();
        engineeringScreensControl.setHgap(10);
        engineeringScreensControl.setVgap(10);
        engineeringScreensControl.getColumnConstraints().setAll(PercentColumnConstraints.createEqualsConstraints(2));
        Button bpmControlButton = new MultiLineButton("BPM");
        bpmControlButton.setTooltip(new Tooltip("Open BPM Engineering OPI"));
        bpmControlButton.setDisable(!Preferences.getInstance().getBPMOPIFile().isPresent());
        bpmControlButton.setOnAction(e -> controller.openEngineeringScreen(true));
        Button correctorsControlButton = new MultiLineButton("Correctors");
        bpmControlButton.setTooltip(new Tooltip("Open Correctors Engineering OPI"));
        correctorsControlButton.setDisable(!Preferences.getInstance().getCorrectorOPIFile().isPresent());
        correctorsControlButton.setOnAction(e -> controller.openEngineeringScreen(false));
        setFullResizable(bpmControlButton,correctorsControlButton);
        engineeringScreensControl.add(bpmControlButton,0,0);
        engineeringScreensControl.add(correctorsControlButton,1,0);
        return new BorderedTitledPane("Engineering Screens",engineeringScreensControl);
    }
}
