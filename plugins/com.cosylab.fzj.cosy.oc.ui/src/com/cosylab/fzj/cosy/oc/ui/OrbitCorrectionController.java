/*
 * Copyright (c) 2017 Cosylab d.d. Contact Information: Cosylab d.d., Ljubljana, Slovenia http://www.cosylab.com This
 * program is free software: you can redistribute it and/or modify it under the terms of the Eclipse Public License.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. For more information about the license please refer
 * to the LICENSE file included in the distribution.
 */
package com.cosylab.fzj.cosy.oc.ui;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.diirt.datasource.ExpressionLanguage.channel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.csstudio.openfile.DisplayUtil;
import org.diirt.datasource.PVManager;
import org.diirt.datasource.PVReader;
import org.diirt.datasource.PVReaderEvent;
import org.diirt.datasource.PVWriter;
import org.diirt.datasource.PVWriterEvent;
import org.diirt.datasource.PVWriterListener;
import org.diirt.util.array.ArrayDouble;
import org.diirt.util.array.ArrayInt;
import org.diirt.util.array.IteratorNumber;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.VEnum;
import org.diirt.vtype.VNumber;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VStringArray;
import org.diirt.vtype.VType;

import com.cosylab.fzj.cosy.oc.LatticeElementData;
import com.cosylab.fzj.cosy.oc.LatticeElementDataLoader;
import com.cosylab.fzj.cosy.oc.LatticeElementType;
import com.cosylab.fzj.cosy.oc.OrbitCorrectionPlugin;
import com.cosylab.fzj.cosy.oc.Preferences;
import com.cosylab.fzj.cosy.oc.ui.model.BPM;
import com.cosylab.fzj.cosy.oc.ui.model.Corrector;
import com.cosylab.fzj.cosy.oc.ui.model.Dipole;
import com.cosylab.fzj.cosy.oc.ui.model.LatticeElement;
import com.cosylab.fzj.cosy.oc.ui.model.Quadrupole;
import com.cosylab.fzj.cosy.oc.ui.model.SeriesType;
import com.cosylab.fzj.cosy.oc.ui.model.Sextupole;
import com.cosylab.fzj.cosy.oc.ui.util.GUIUpdateThrottle;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * <code>OrbitCorrectionController</code> is the controller for the orbit correction viewer. It provides the logic for
 * showing orbit correction status and executing orbit correction commands..
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class OrbitCorrectionController {

    /** The rate at which the UI is updated */
    private static final long UPDATE_RATE = 500;
    /** Timeout how long we are willing to wait to receive the lattice data */
    private static final long UPDATE_TIMEOUT = 10000L;
    private static final Executor UI_EXECUTOR = Platform::runLater;
    // Orbit correction results table row names
    private static final String TABLE_ENTRY_HORIZONTAL_ORBIT = "Horizontal Orbit";
    private static final String TABLE_ENTRY_VERTICAL_ORBIT = "Vertical Orbit";
    private static final String TABLE_ENTRY_GOLDEN_HORIZONTAL_ORBIT = "Golden Horizontal Orbit";
    private static final String TABLE_ENTRY_GOLDEN_VERTICAL_ORBIT = "Golden Vertical Orbit";
    // status messages
    private static final String MSG_RESET_CORRECTION_CMD_SUCCESS = "Reset orbit correction setpoints was successfully sent.";
    private static final String MSG_RESET_CORRECTION_CMD_FAILURE = "Error occured while sending the reset orbit correction command.";
    private static final String MSG_START_ORBIT_MEASURING_CMD_SUCCESS = "Start orbit measuring command was successfully sent.";
    private static final String MSG_START_ORBIT_MEASURING_CMD_FAILURE = "Error occured while sending start orbit measuring command.";
    private static final String MSG_STOP_ORBIT_MEASURING_CMD_SUCCESS = "Stop orbit measuring command was successfully sent.";
    private static final String MSG_STOP_ORBIT_MEASURING_CMD_FAILURE = "Error occured while sending stop orbit measuring command.";
    private static final String MSG_MEASURE_ORBIT_ONCE_CMD_SUCCESS = "Measure orbit once command was successfully sent.";
    private static final String MSG_MEASURE_ORBIT_ONCE_CMD_FAILURE = "Error occured while sending measure orbit once command.";
    private static final String MSG_START_CORRECTING_ORBIT_CMD_SUCCESS = "Start correcting orbit command was successfully sent.";
    private static final String MSG_START_CORRECTING_ORBIT_CMD_FAILURE = "Error occured while sending start correcting orbit command.";
    private static final String MSG_STOP_CORRECTING_ORBIT_CMD_SUCCESS = "Stop correcting orbit command was successfully sent.";
    private static final String MSG_STOP_CORRECTING_ORBIT_CMD_FAILURE = "Error occured while sending stop correcting orbit command.";
    private static final String MSG_CORRECT_ORBIT_ONCE_CMD_SUCCESS = "Correct orbit once command was successfully sent.";
    private static final String MSG_CORRECT_ORBIT_ONCE_CMD_FAILURE = "Error occured while sending correct orbit once command.";
    private static final String MSG_EXPORT_CURRENT_ORBIT_SUCCESS = "Current orbit was successfully exported.";
    private static final String MSG_EXPORT_CURRENT_ORBIT_FAILURE = "Error occured while exporting current orbit.";
    private static final String MSG_UPLOAD_GOLDEN_ORBIT_SUCCESS = "%s golden orbit was successfully uploaded.";
    private static final String MSG_UPLOAD_GOLDEN_ORBIT_FAILURE = "Error occured while uploading %s golden orbit.";
    private static final String MSG_UPDATE_ONOFF_SUCCESS = "Successfully enabled/disabled %s.";
    private static final String MSG_UPDATE_ONOFF_FAILURE = "Error enabling/disabling %s.";
    private static final String MSG_CUTOFF_SUCCESS = "%s cutoff factor %f successfully written.";
    private static final String MSG_CUTOFF_FAILURE = "Failed to write %s cutoff factor.";
    private static final String MSG_CORRECTION_FACTOR_SUCCESS = "%s correction factor %d successfully written.";
    private static final String MSG_CORRECTION_FACTOR_FAILURE = "Failed to write %s correction factor.";
    private static final String MSG_USE_CURRENT_HORIZONTAL_SUCCESS = "Golden horizontal orbit was successfully updated to current.";
    private static final String MSG_USE_CURRENT_HORIZONTAL_FAILURE = "Error occured while updating golden horizontal orbit to current.";
    private static final String MSG_USE_CURRENT_VERTICAL_SUCCESS = "Golden vertical orbit was successfully updated to current.";
    private static final String MSG_USE_CURRENT_VERTICAL_FAILURE = "Error occured while updatind golden vertical orbit to current.";
    private static final String MSG_USE_CURRENT_AS_REFERENCE_SUCCESS = "Reference orbit successfully updated to the current orbit values.";
    private static final String MSG_USE_CURRENT_AS_REFERENCE_FAILURE = "Reference orbit update failed.";
    @Deprecated
    private static final String MSG_DOWNLOAD_GOLDEN_ORBIT_SUCCESS = "Golden orbit was successfully downloaded.";
    @Deprecated
    private static final String MSG_DOWNLOAD_GOLDEN_ORBIT_FAILURE = "Error occured while downloading golden orbit.";
    @Deprecated
    private static final String MSG_UPLOAD_ORM_SUCCESS = "Orbit response matrix was successfully uploaded.";
    @Deprecated
    private static final String MSG_UPLOAD_ORM_FAILURE = "Error occured while uploading orbit response matrix.";
    @Deprecated
    private static final String MSG_DOWNLOAD_ORM_SUCCESS = "Orbit response matrix was successfully downloaded.";
    @Deprecated
    private static final String MSG_DOWNLOAD_ORM_FAILURE = "Error occured while downloading orbit response matrix.";
    private static final String EMPTY_STRING = "";
    private static final char NEW_LINE = '\n';

    private class SlowPV extends PV {

        AtomicBoolean startedTriggering = new AtomicBoolean(false);

        SlowPV(PVReader<VType> reader, PVWriter<Object> writer) {
            super(reader,writer);
        }

        @Override
        void init() {
            synchronized (this) {
                super.init();
                hasNewValue.set(value != null);
            }
        }

        @Override
        void handleValue(PVReaderEvent<VType> e) {
            if (e.isExceptionChanged()) {
                writeToLog("DIIRT Connection Error.",Level.SEVERE,ofNullable(e.getPvReader().lastException()));
            }
            connectionStateUpdate(e);
            synchronized (this) {
                value = e.getPvReader().isConnected() ? e.getPvReader().getValue() : null;
                hasNewValue.compareAndSet(false,value != null);
            }
        }

        @Override
        void postHandleAction() {
            if (startedTriggering.get()) {
                updateLattice();
            } else {
                synchronized (OrbitCorrectionController.this) {
                    OrbitCorrectionController.this.notifyAll();
                }
            }
        }

        void startTriggering() {
            startedTriggering.set(true);
            if (hasNewValue.get()) {
                updateLattice();
            }
        }
    }

    private class PV {

        final PVReader<VType> reader;
        final PVWriter<Object> writer;
        //all actions on the value are atomic, therefore no need for synchronisation
        volatile VType value;
        AtomicBoolean hasNewValue = new AtomicBoolean(false);
        AtomicBoolean connected = new AtomicBoolean(false);
        private boolean firstTime = true;

        PV(PVReader<VType> reader, PVWriter<Object> writer) {
            this.reader = reader;
            this.writer = writer;
            init();
        }

        void init() {
            this.reader.addPVReaderListener(e -> {
                handleValue(e);
                postHandleAction();
            });
            this.value = reader.getValue();
            hasNewValue.set(true);
        }

        void postHandleAction() {
            throttle.trigger();
        }

        void handleValue(PVReaderEvent<VType> e) {
            if (e.isExceptionChanged()) {
                writeToLog("DIIRT Connection Error.",Level.SEVERE,ofNullable(e.getPvReader().lastException()));
            }
            connectionStateUpdate(e);
            value = e.getPvReader().isConnected() ? e.getPvReader().getValue() : null;
            hasNewValue.set(true);
        }

        void connectionStateUpdate(PVReaderEvent<VType> e) {
            connected.set(e.getPvReader().isConnected());
            if (e.isConnectionChanged()) {
                updateConnected();
            }
            if (!e.getPvReader().isConnected()) {
                writeToLog(String.format("%s connection error.",e.getPvReader().getName()),Level.SEVERE,empty());
            } else if (e.isConnectionChanged() && !firstTime) {
                writeToLog(String.format("%s connection recovered.",e.getPvReader().getName()),Level.INFO,empty());
            }
            firstTime = false;
        }

        void dispose() {
            if (!reader.isClosed()) {
                reader.close();
            }
            synchronized (this) {
                if (writer != null && !writer.isClosed()) {
                    writer.close();
                }
            }
        }
    }

    private DoubleProperty horizontalCutOffProperty;
    private DoubleProperty verticalCutOffProperty;
    private IntegerProperty verticalCorrectionFactorProperty;
    private IntegerProperty horizontalCorrectionFactorProperty;
    private final BooleanProperty allConnectedProperty = new SimpleBooleanProperty(this,"allConnected",false);
    private final BooleanProperty mradProperty = new SimpleBooleanProperty(this,"mrad",false);
    private final StringProperty statusProperty = new SimpleStringProperty(this,"status",EMPTY_STRING);
    private final Map<String,PV> pvs = new HashMap<>();
    private final Map<String,PV> slowPVs = new HashMap<>();
    private final Map<String,OrbitCorrectionResultsEntry> correctionResultsEntries = new LinkedHashMap<>(4);
    private final List<BPM> horizontalBPMs = new ArrayList<>();
    private final List<BPM> verticalBPMs = new ArrayList<>();
    private final List<Corrector> horizontalCorrectors = new ArrayList<>();
    private final List<Corrector> verticalCorrectors = new ArrayList<>();
    private final List<Quadrupole> quadrupoles = new ArrayList<>();
    private final List<Dipole> dipoles = new ArrayList<>();
    private final List<Sextupole> sextupoles = new ArrayList<>();
    private final List<Consumer<LatticeElementType>> latticeUpdateCallbacks = new CopyOnWriteArrayList<>();
    private final List<Consumer<SeriesType>> goldenOrbitCallbacks = new CopyOnWriteArrayList<>();
    private static final BiConsumer<Runnable,Throwable> AFTER_EXECUTE = (r, t) -> {
        if (r instanceof Future<?>) {
            if (((Future<?>)r).isDone()) {
                try {
                    ((Future<?>)r).get(1,TimeUnit.MILLISECONDS);
                } catch (ExecutionException e) {
                    t = e.getCause();
                } catch (CancellationException | InterruptedException | TimeoutException e) {
                    //ignore
                }
            }
        }
        if (t != null) {
            OrbitCorrectionPlugin.LOGGER.log(Level.SEVERE,"Execution Error",t);
        }
    };
    private final ExecutorService nonUIexecutor = new ThreadPoolExecutor(1,1,0L,TimeUnit.SECONDS,
            new LinkedBlockingQueue<>()) {

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            AFTER_EXECUTE.accept(r,t);
        }
    };
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1) {

        protected void afterExecute(Runnable r, Throwable t) {
            AFTER_EXECUTE.accept(r,t);
        };
    };
    private final GUIUpdateThrottle throttle = new GUIUpdateThrottle(20,UPDATE_RATE,
            v -> UI_EXECUTOR.execute(() -> update()));

    /**
     * Constructs a new controller for the orbit correction view.
     */
    public OrbitCorrectionController() {
        createCorrectionResultsEntries();
        nonUIexecutor.execute(() -> loadLatticeElements());
        nonUIexecutor.execute(() -> connectPVs(Preferences.getInstance().getPVNames(),pvs,true));
        nonUIexecutor.execute(() -> throttle.start());
        mradProperty.addListener(e -> throttle.trigger());
    }

    /**
     * Add a callback listener, which is notified when the lattice is updated. The parameter specifies which type of
     * elements were updated.
     *
     * @param consumer the listener
     */
    public void addLaticeUpdateCallback(Consumer<LatticeElementType> consumer) {
        latticeUpdateCallbacks.add(consumer);
    }

    /**
     * Remove a callback listener.
     *
     * @param consumer the listener to remove
     */
    public void removeLaticeUpdateCallback(Consumer<LatticeElementType> consumer) {
        latticeUpdateCallbacks.remove(consumer);
    }

    /**
     * Add a callback listener, which is notified when the lattice is updated. The parameter specifies which type of
     * elements were updated.
     *
     * @param consumer the listener
     */
    public void addGoldenOrbitUpdateCallback(Consumer<SeriesType> consumer) {
        goldenOrbitCallbacks.add(consumer);
    }

    /**
     * Remove a callback listener.
     *
     * @param consumer the listener to remove
     */
    public void removeGoldernOrbitUpdateCallback(Consumer<SeriesType> consumer) {
        goldenOrbitCallbacks.remove(consumer);
    }

    /**
     * Returns the list of all horizontal beam position monitors.
     *
     * @return list of horizontal BPMs
     */
    public List<BPM> getHorizontalBPMs() {
        synchronized (horizontalBPMs) {
            return new ArrayList<>(horizontalBPMs);
        }
    }

    /**
     * Returns the list of all vertical beam position monitors.
     *
     * @return list of vertical BPMs
     */
    public List<BPM> getVerticalBPMs() {
        synchronized (verticalBPMs) {
            return new ArrayList<>(verticalBPMs);
        }
    }

    /**
     * Returns the list of all horizontal correctors.
     *
     * @return list of all horizontal correctors
     */
    public List<Corrector> getHorizontalCorrectors() {
        synchronized (horizontalCorrectors) {
            return new ArrayList<>(horizontalCorrectors);
        }
    }

    /**
     * Returns the list of all vertical correctors.
     *
     * @return list of all vertical correctors
     */
    public List<Corrector> getVerticalCorrectors() {
        synchronized (verticalCorrectors) {
            return new ArrayList<>(verticalCorrectors);
        }
    }

    /**
     * Returns the list of all quadrupole magnets.
     *
     * @return list of quadrupoles
     */
    public List<LatticeElement> getQuadrupoles() {
        synchronized (quadrupoles) {
            return new ArrayList<>(quadrupoles);
        }
    }

    /**
     * Returns the list of all dipoles magnets.
     *
     * @return list of dipoles
     */
    public List<LatticeElement> getDipoles() {
        synchronized (dipoles) {
            return new ArrayList<>(dipoles);
        }
    }

    /**
     * Returns the list of all sextupoles magnets.
     *
     * @return list of sextupoles
     */
    public List<LatticeElement> getSextupoles() {
        synchronized (sextupoles) {
            return new ArrayList<>(sextupoles);
        }
    }

    /**
     * Returns the list of orbit correction results table entries. The keys are the PV keys and the values are the
     * actual table entries
     *
     * @return list of orbit correction results table entries
     */
    public Map<String,OrbitCorrectionResultsEntry> getOrbitCorrectionResults() {
        return correctionResultsEntries;
    }

    /**
     * Returns the property providing the selected unit for the correctors kick values.
     *
     * @return property providing the selected unit (mrad or mA).
     */
    public BooleanProperty mradProperty() {
        return mradProperty;
    }

    /**
     * Returns the property that contains the current IOC operation status.
     *
     * @return property providing the current operation status
     */
    public StringProperty statusProperty() {
        return statusProperty;
    }

    /**
     * Return the property which has a value true if all PVs are connected or false if at least one is disconnected.
     *
     * @return the all connected property
     */
    public ReadOnlyBooleanProperty allConnectedProperty() {
        return allConnectedProperty;
    }

    private synchronized void updateConnected() {
        boolean allConnected = pvs.values().stream().allMatch(pv -> pv.connected.get());
        if (allConnected) {
            allConnected = slowPVs.values().stream().allMatch(pv -> pv.connected.get());
        }
        final boolean update = allConnected;
        UI_EXECUTOR.execute(() -> allConnectedProperty.set(update));
    }

    /**
     * Returns the property that stores the current SVD cut off value for horizontal correction.
     *
     * @return horizontal correction SVD cut off value
     */
    public DoubleProperty horizontalCutOffProperty() {
        if (horizontalCutOffProperty == null) {
            horizontalCutOffProperty = new SimpleDoubleProperty(this,"horizontalCutoff",0);
            horizontalCutOffProperty.addListener((a, o, n) -> {
                getNumberPV.apply(Preferences.PV_HORIZONTAL_CUTOFF).ifPresent(pv -> {
                    if (Double.compare(n.doubleValue(),((VNumber)pv.value).getValue().doubleValue()) != 0) {
                        writeData(pv,n.doubleValue(),String.format(MSG_CUTOFF_SUCCESS,"Horizontal",n.doubleValue()),
                                String.format(MSG_CUTOFF_FAILURE,"horizontal"),1);
                    }
                });
            });
        }
        return horizontalCutOffProperty;
    }

    /**
     * Returns the property that stores the current SVD cut off value for vertical correction.
     *
     * @return vertical correction SVD cut off value
     */
    public DoubleProperty verticalCutOffProperty() {
        if (verticalCutOffProperty == null) {
            verticalCutOffProperty = new SimpleDoubleProperty(this,"verticalCutoff",0);
            verticalCutOffProperty.addListener((a, o, n) -> {
                getNumberPV.apply(Preferences.PV_VERTICAL_CUTOFF).ifPresent(pv -> {
                    if (Double.compare(n.doubleValue(),((VNumber)pv.value).getValue().doubleValue()) != 0) {
                        writeData(pv,n.doubleValue(),String.format(MSG_CUTOFF_SUCCESS,"Vertical",n.doubleValue()),
                                String.format(MSG_CUTOFF_FAILURE,"vertical"),2);
                    }
                });
            });
        }
        return verticalCutOffProperty;
    }

    /**
     * Returns the property that stores the current factor used when applying horizontal correction. The correcting
     * values are calculated and then multiplied by this factor to only apply 10, 20, 30 etc. percent. This property
     * returns the factor in percentage between 0 and 100.
     *
     * @return the correction factor property
     */
    public IntegerProperty horizontalCorrectionFactorProperty() {
        if (horizontalCorrectionFactorProperty == null) {
            horizontalCorrectionFactorProperty = new SimpleIntegerProperty(this,"horizontalCorrectionFactor",0);
            horizontalCorrectionFactorProperty.addListener((a, o, n) -> {
                getNumberPV.apply(Preferences.PV_HORIZONTAL_CORRECTION_FRACTION).ifPresent(pv -> {
                    if (n.intValue() != (int)(100 * ((VNumber)pv.value).getValue().doubleValue())) {
                        writeData(pv,n.doubleValue() / 100.,
                                String.format(MSG_CORRECTION_FACTOR_SUCCESS,"Horizontal",n.intValue()),
                                String.format(MSG_CORRECTION_FACTOR_FAILURE,"horizontal"),3);
                    }
                });
            });
        }
        return horizontalCorrectionFactorProperty;
    }

    /**
     * Returns the property that stores the current factor used when applying vertical correction. The correcting values
     * are calculated and then multiplied by this factor to only apply 10, 20, 30 etc. percent. This property returns
     * the factor in percentage between 0 and 100.
     *
     * @return the correction factor property
     */
    public IntegerProperty verticalCorrectionFactorProperty() {
        if (verticalCorrectionFactorProperty == null) {
            verticalCorrectionFactorProperty = new SimpleIntegerProperty(this,"verticalCorrectionFactor",0);
            verticalCorrectionFactorProperty.addListener((a, o, n) -> {
                getNumberPV.apply(Preferences.PV_VERTICAL_CORRECTION_FRACTION).ifPresent(pv -> {
                    if (n.intValue() != (int)(100 * ((VNumber)pv.value).getValue().doubleValue())) {
                        writeData(pv,n.doubleValue() / 100.,
                                String.format(MSG_CORRECTION_FACTOR_SUCCESS,"Vertical",n.intValue()),
                                String.format(MSG_CORRECTION_FACTOR_FAILURE,"vertical"),3);
                    }
                });
            });
        }
        return verticalCorrectionFactorProperty;
    }

    /**
     * Calls the command which resets the correction values to the current steerer setpoints.
     */
    public void resetOrbitCorrection() {
        executeCommand(Preferences.PV_RESET_CORRECTION,MSG_RESET_CORRECTION_CMD_SUCCESS,
                MSG_RESET_CORRECTION_CMD_FAILURE);
    }

    /**
     * Calls command which starts measuring orbit and updating horizontal and vertical orbit periodically.
     */
    public void startMeasuringOrbit() {
        executeCommand(Preferences.PV_START_MEASURING_ORBIT,MSG_START_ORBIT_MEASURING_CMD_SUCCESS,
                MSG_START_ORBIT_MEASURING_CMD_FAILURE);
    }

    /**
     * Calls command which stops measuring orbit. Horizontal and vertical orbit keep the value as they have it at this
     * time.
     */
    public void stopMeasuringOrbit() {
        executeCommand(Preferences.PV_STOP_MEASURING_ORBIT,MSG_STOP_ORBIT_MEASURING_CMD_SUCCESS,
                MSG_STOP_ORBIT_MEASURING_CMD_FAILURE);
    }

    /**
     * Calls command which performs one orbit measurement for X and Y and update horizontal and vertical orbit.
     */
    public void measureOrbitOnce() {
        executeCommand(Preferences.PV_MEASURE_ORBIT_ONCE,MSG_MEASURE_ORBIT_ONCE_CMD_SUCCESS,
                MSG_MEASURE_ORBIT_ONCE_CMD_FAILURE);
    }

    /**
     * Calls command which starts measuring orbit periodically and perform orbit correction for every measurement.
     */
    public void startCorrectingOrbit() {
        executeCommand(Preferences.PV_START_CORRECTING_ORBIT,MSG_START_CORRECTING_ORBIT_CMD_SUCCESS,
                MSG_START_CORRECTING_ORBIT_CMD_FAILURE);
    }

    /**
     * Calls command which stops measuring orbit, all PVs keep their last value.
     */
    public void stopCorrectingOrbit() {
        executeCommand(Preferences.PV_STOP_CORRECTING_ORBIT,MSG_STOP_CORRECTING_ORBIT_CMD_SUCCESS,
                MSG_STOP_CORRECTING_ORBIT_CMD_FAILURE);
    }

    /**
     * Calls command which uses the last horizontal and vertical orbit, calculate the corrections and apply them once
     */
    public void correctOrbitOnce() {
        executeCommand(Preferences.PV_CORRECT_ORBIT_ONCE,MSG_CORRECT_ORBIT_ONCE_CMD_SUCCESS,
                MSG_CORRECT_ORBIT_ONCE_CMD_FAILURE);
    }

    /**
     * Exports current horizontal and vertical orbit with weights into the given file.
     *
     * @param file file in which horizontal and vertical orbit with weights will be written
     * @param comment to store into the file if given
     */
    public void exportCurrentOrbit(File file, Optional<String> comment) {
        Preferences.getInstance().saveInitialDirectory(file.getParentFile());
        writeOrbitToFile(file,comment.orElse(null),Preferences.PV_HORIZONTAL_ORBIT,Preferences.PV_VERTICAL_ORBIT,
                MSG_EXPORT_CURRENT_ORBIT_SUCCESS,MSG_EXPORT_CURRENT_ORBIT_FAILURE);
    }

    /**
     * Update the golden orbit as it is defined by the golden position wish property on each BPM.
     *
     * @param horizontal true if horizontal golden orbit should be updated
     * @param vertical true if vertical golden orbit should be updated
     */
    public void updateGoldenOrbit(boolean horizontal, boolean vertical) {
        ToDoubleFunction<BPM> mapper = bpm -> bpm.goldenPositionWishProperty().get();
        if (horizontal) {
            final ArrayDouble hValues = new ArrayDouble(getHorizontalBPMs().stream().mapToDouble(mapper).toArray());
            ofNullable(pvs.get(Preferences.PV_GOLDEN_HORIZONTAL_ORBIT))
                    .ifPresent(pv -> writeData(pv,hValues,String.format(MSG_UPLOAD_GOLDEN_ORBIT_SUCCESS,"Horizontal"),
                            String.format(MSG_UPLOAD_GOLDEN_ORBIT_FAILURE,"horizontal"),4));
        }
        if (vertical) {
            final ArrayDouble vValues = new ArrayDouble(getVerticalBPMs().stream().mapToDouble(mapper).toArray());
            ofNullable(pvs.get(Preferences.PV_GOLDEN_VERTICAL_ORBIT))
                    .ifPresent(pv -> writeData(pv,vValues,String.format(MSG_UPLOAD_GOLDEN_ORBIT_SUCCESS,"Vertical"),
                            String.format(MSG_UPLOAD_GOLDEN_ORBIT_FAILURE,"vertical"),5));
        }
    }

    /**
     * Uploads new golden horizontal and vertical orbit with weights from the given file.
     *
     * @param file file in which golden horizontal and vertical orbit with weights are written
     * @deprecated replaced by direct PV control
     */
    @Deprecated
    public void uploadGoldenOrbit(File file) {
        if (!throttle.isRunning()) return;
        nonUIexecutor.execute(() -> {
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(file.getPath()))) {
                int lineCounter = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    lineCounter++;
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }
                    String[] values = line.split(" ");
                    if (lineCounter == 1) {
                        ofNullable(pvs.get(Preferences.PV_GOLDEN_HORIZONTAL_ORBIT))
                                .ifPresent(pv -> convertAndWriteData(pv,values,"Golden Horizontal Orbit",0));
                    } else if (lineCounter == 2) {
                        ofNullable(pvs.get(Preferences.PV_GOLDEN_VERTICAL_ORBIT))
                                .ifPresent(pv -> convertAndWriteData(pv,values,"Golden Vertical Orbit",0));
                    } else {
                        break;
                    }
                }
                writeToLog(MSG_UPLOAD_GOLDEN_ORBIT_SUCCESS,Level.INFO,empty());
            } catch (Exception e) {
                writeToLog(MSG_UPLOAD_GOLDEN_ORBIT_FAILURE,Level.SEVERE,of(e));
            }
        });
    }

    /**
     * Downloads current golden horizontal and vertical orbit with weights into the given file.
     *
     * @param file destination file in which golden horizontal and vertical orbit with weights will be written
     * @deprecated replaced by direct PV control
     */
    @Deprecated
    public void downloadGoldenOrbit(File file) {
        writeOrbitToFile(file,null,Preferences.PV_GOLDEN_HORIZONTAL_ORBIT,Preferences.PV_GOLDEN_VERTICAL_ORBIT,
                MSG_DOWNLOAD_GOLDEN_ORBIT_SUCCESS,MSG_DOWNLOAD_GOLDEN_ORBIT_FAILURE);
    }

    /**
     * Uses current horizontal and vertical orbit values as golden orbits.
     */
    public void useCurrent() {
        if (!throttle.isRunning()) return;
        final PV horizontalOrbit = pvs.get(Preferences.PV_HORIZONTAL_ORBIT);
        final PV verticalOrbit = pvs.get(Preferences.PV_VERTICAL_ORBIT);
        final PV goldenHorizontalOrbit = pvs.get(Preferences.PV_GOLDEN_HORIZONTAL_ORBIT);
        final PV goldenVerticalOrbit = pvs.get(Preferences.PV_GOLDEN_VERTICAL_ORBIT);
        if (horizontalOrbit != null && goldenHorizontalOrbit != null) {
            writeData(goldenHorizontalOrbit,horizontalOrbit.value,MSG_USE_CURRENT_HORIZONTAL_SUCCESS,
                    MSG_USE_CURRENT_HORIZONTAL_FAILURE,0);
        }
        if (verticalOrbit != null && goldenVerticalOrbit != null) {
            writeData(goldenVerticalOrbit,verticalOrbit.value,MSG_USE_CURRENT_VERTICAL_SUCCESS,
                    MSG_USE_CURRENT_VERTICAL_FAILURE,0);
        }
        throttle.trigger();
    }

    /**
     * Uses current horizontal or vertical orbit values as a reference orbit.
     *
     * @param horizontal true if the horizontal reference orbit should be applied or false if vertical
     */
    public void useCurrentAsReference(boolean horizontal) {
        if (!throttle.isRunning()) return;
        final PV destination, source;
        if (horizontal) {
            destination = pvs.get(Preferences.PV_HORIZONTAL_REFERENCE_ORBIT);
            source = pvs.get(Preferences.PV_HORIZONTAL_ORBIT);
        } else {
            destination = pvs.get(Preferences.PV_VERTICAL_REFERENCE_ORBIT);
            source = pvs.get(Preferences.PV_VERTICAL_ORBIT);
        }
        if (destination != null && source != null) {
            writeData(destination,source.value,MSG_USE_CURRENT_AS_REFERENCE_SUCCESS,
                    MSG_USE_CURRENT_AS_REFERENCE_FAILURE,0);
        }
        throttle.trigger();
    }

    /**
     * Download orbit response matrix and save it to a file.
     *
     * @param file destination file in which to store the matrix
     * @deprecated functionality removed for the time being.
     */
    @Deprecated
    public void downloadOrbitResponseMatrix(File file) {
        if (!throttle.isRunning()) return;
        final PV ormPV = pvs.get(Preferences.PV_ORM);
        final PV horizontalOrbitPV = pvs.get(Preferences.PV_HORIZONTAL_ORBIT);
        final PV horizontalCorrectorPV = pvs.get(Preferences.PV_HORIZONTAL_CORRECTOR_MA);
        int n = horizontalOrbitPV != null ? ((VNumberArray)horizontalOrbitPV.value).getData().size() : -1;
        int m = horizontalCorrectorPV != null ? ((VNumberArray)horizontalCorrectorPV.value).getData().size() : -1;
        if (n == -1 || m == -1 || ormPV == null) {
            return;
        }
        final VNumberArray value = (VNumberArray)ormPV.value;
        nonUIexecutor.execute(() -> {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getPath()),StandardCharsets.UTF_8)) {
                if (value != null) {
                    StringBuilder sb = new StringBuilder(n * m * 15);
                    ListNumber data = value.getData();
                    for (int i = 0; i < data.size(); i++) {
                        if (i != 0 && i % n == 0) {
                            sb.append(NEW_LINE);
                        }
                        sb.append(data.getDouble(i)).append(' ');
                    }
                    writer.write(sb.toString());
                }
                writeToLog(MSG_DOWNLOAD_ORM_SUCCESS,Level.INFO,empty());
            } catch (Exception e) {
                writeToLog(MSG_DOWNLOAD_ORM_FAILURE,Level.SEVERE,of(e));
            }
        });
    }

    /**
     * Uploads response matrix provided by the given file.
     *
     * @param file source file to load the response matrix from
     * @deprecated functionality removed for the time being.
     */
    @Deprecated
    public void uploadOrbitResponseMatrix(File file) {
        if (!throttle.isRunning()) return;
        final PV ormPV = pvs.get(Preferences.PV_ORM);
        final PV horizontalOrbitPV = pvs.get(Preferences.PV_HORIZONTAL_ORBIT);
        final PV horizontalCorrectorPV = pvs.get(Preferences.PV_HORIZONTAL_CORRECTOR_MA);
        final int n = horizontalOrbitPV != null ? ((VNumberArray)horizontalOrbitPV.value).getData().size() : -1;
        final int m = horizontalCorrectorPV != null ? ((VNumberArray)horizontalCorrectorPV.value).getData().size() : -1;
        if (n == -1 || m == -1 || ormPV == null) {
            return;
        }
        nonUIexecutor.execute(() -> {
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(file.getPath()),StandardCharsets.UTF_8)) {
                StringBuilder sb = new StringBuilder(n * m * 15);
                String line;
                Pattern matchSpaces = Pattern.compile("[\\s\\t]+");
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) {
                        continue;
                    }
                    line = matchSpaces.matcher(line).replaceAll(" ");
                    sb.append(line).append(' ');
                }
                String[] waveform = sb.toString().split(" ");
                convertAndWriteData(ormPV,waveform,"Orbit response matrix",0);
                writeToLog(MSG_UPLOAD_ORM_SUCCESS,Level.INFO,empty());
            } catch (Exception e) {
                writeToLog(MSG_UPLOAD_ORM_FAILURE,Level.SEVERE,of(e));
            }
        });
    }

    /**
     * Measure orbit response matrix.
     */
    public void measureOrbitResponseMatrix() {
        Preferences.getInstance().getMeasureORMCommand().ifPresent(c -> nonUIexecutor.execute(() -> {
            try {
                Runtime.getRuntime().exec(c);
            } catch (IOException e) {
                writeToLog("Error starting command " + c,Level.SEVERE,of(e));
            }
        }));
    }

    /**
     * Opens the engineering OPI screen for BPMs or correctors if the appropriate property is defined in the
     * preferences.
     *
     * @param forBPM true if the BPM engineering screens should open or false if corrector screen should open
     */
    public void openEngineeringScreen(boolean forBPM) {
        Optional<String> opi;
        if (forBPM) {
            opi = Preferences.getInstance().getBPMOPIFile();
        } else {
            opi = Preferences.getInstance().getCorrectorOPIFile();
        }
        opi.ifPresent(path -> {
            try {
                DisplayUtil.getInstance().openDisplay(path,null);
            } catch (Exception e) {
                OrbitCorrectionPlugin.LOGGER.log(Level.WARNING,
                        String.format("Could not open the %s engineering screen.",forBPM ? "BPMs" : "correctors"),e);
            }
        });
    }

    /**
     * Dispose or all resources.
     */
    public void dispose() {
        nonUIexecutor.execute(() -> {
            throttle.dispose();
            try {
                //this should be enough to finish whatever is going on
                throttle.join(3000);
            } catch (InterruptedException e) {
                OrbitCorrectionPlugin.LOGGER.log(Level.WARNING,"Failed to shutdown gracefully. Timeout ocurred.",e);
            }
            synchronized (OrbitCorrectionController.this) {
                pvs.values().parallelStream().forEach(v -> v.dispose());
                pvs.clear();
                slowPVs.values().parallelStream().forEach(v -> v.dispose());
                slowPVs.clear();
            }
            clearList(horizontalBPMs);
            clearList(verticalBPMs);
            clearList(quadrupoles);
            clearList(dipoles);
            clearList(sextupoles);
            clearList(horizontalCorrectors);
            clearList(verticalCorrectors);
            nonUIexecutor.shutdownNow();
            scheduler.shutdownNow();
        });
    }

    private static void clearList(List<?> list) {
        synchronized (list) {
            list.clear();
        }
    }

    private static <T extends LatticeElement> void addToList(List<T> list, T item) {
        synchronized (list) {
            list.add(item);
        }
    }

    public void writeLatticeStatesToPV() {
        ToIntFunction<LatticeElement> toInt = e -> e.enabledWishProperty().get() ? 1 : 0;
        final int[] horizontalCorrectors = getHorizontalCorrectors().stream().mapToInt(toInt).toArray();
        final int[] verticalCorrectors = getVerticalCorrectors().stream().mapToInt(toInt).toArray();
        final int[] horizontalBPMs = getHorizontalBPMs().stream().mapToInt(toInt).toArray();
        final int[] verticalBPMs = getVerticalBPMs().stream().mapToInt(toInt).toArray();
        ofNullable(pvs.get(Preferences.PV_HORIZONTAL_BPM_ENABLED)).ifPresent(pv -> {
            writeData(pv,new ArrayInt(horizontalBPMs),String.format(MSG_UPDATE_ONOFF_SUCCESS,"horizontal BPMs"),
                    String.format(MSG_UPDATE_ONOFF_FAILURE,"horizontal BPMs"),5);
        });
        ofNullable(pvs.get(Preferences.PV_VERTICAL_BPM_ENABLED)).ifPresent(pv -> {
            writeData(pv,new ArrayInt(verticalBPMs),String.format(MSG_UPDATE_ONOFF_SUCCESS,"vertical BPMs"),
                    String.format(MSG_UPDATE_ONOFF_FAILURE,"vertical BPMs"),6);
        });
        ofNullable(pvs.get(Preferences.PV_HORIZONTAL_CORRECTOR_ENABLED)).ifPresent(pv -> {
            writeData(pv,new ArrayInt(horizontalCorrectors),
                    String.format(MSG_UPDATE_ONOFF_SUCCESS,"horizontal correctors"),
                    String.format(MSG_UPDATE_ONOFF_FAILURE,"horizontal correctors"),7);
        });
        ofNullable(pvs.get(Preferences.PV_VERTICAL_CORRECTOR_ENABLED)).ifPresent(pv -> {
            writeData(pv,new ArrayInt(verticalCorrectors),String.format(MSG_UPDATE_ONOFF_SUCCESS,"vertical correctors"),
                    String.format(MSG_UPDATE_ONOFF_FAILURE,"vertical correctors"),8);
        });
    }

    /**
     * Loads lattice elements from the file or PVs, depending on the preferences.
     */
    @SuppressWarnings("deprecation")
    private void loadLatticeElements() {
        if (Preferences.getInstance().isLoadLatticeFromFiles()) {
            LatticeElementDataLoader.loadLatticeElements().forEach(e -> {
                if (e.getType() == LatticeElementType.HORIZONTAL_BPM) {
                    addToList(horizontalBPMs,new BPM(e));
                } else if (e.getType() == LatticeElementType.VERTICAL_BPM) {
                    addToList(verticalBPMs,new BPM(e));
                } else if (e.getType() == LatticeElementType.HORIZONTAL_CORRECTOR) {
                    addToList(horizontalCorrectors,new Corrector(e));
                } else if (e.getType() == LatticeElementType.VERTICAL_CORRECTOR) {
                    addToList(verticalCorrectors,new Corrector(e));
                } else if (e.getType() == LatticeElementType.QUADRUPOLE) {
                    addToList(quadrupoles,new Quadrupole(e));
                } else if (e.getType() == LatticeElementType.DIPOLE) {
                    addToList(dipoles,new Dipole(e));
                } else if (e.getType() == LatticeElementType.SEXTUPOLE) {
                    addToList(sextupoles,new Sextupole(e));
                }
            });
        } else {
            synchronized (this) {
                connectPVs(Preferences.getInstance().getLatticePVNames(),slowPVs,false);
            }
            try {
                long start = System.currentTimeMillis();
                writeToLog("Trying to read the lattice.",Level.INFO,empty());
                while (true) {
                    //check if there is a single value which has not yet received an update
                    //if there is one, wait a while, then check again
                    Optional<PV> noValue = slowPVs.values().parallelStream().filter(pv -> !pv.hasNewValue.get())
                            .findAny();
                    if (noValue.isPresent()) {
                        synchronized (OrbitCorrectionController.this) {
                            OrbitCorrectionController.this.wait(50);
                        }
                        if (System.currentTimeMillis() - start > UPDATE_TIMEOUT) {
                            writeToLog("Lattice information could not be read from the IOC.",Level.SEVERE,empty());
                            break;
                        }
                    } else {
                        writeToLog("Lattice constructed.",Level.INFO,empty());
                        break;
                    }
                }
            } catch (InterruptedException e) {
                //ignore - executor aborted because application is being closed; we don't care for that
                return;
            }
            updateLattice();
            slowPVs.values().parallelStream().forEach(pv -> ((SlowPV)pv).startTriggering());
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends LatticeElement> void handleLatticeUpdate(VStringArray names, VNumberArray positions,
            VNumberArray enable, List<T> destination, LatticeElementType type,
            Function<LatticeElementData,LatticeElement> factory) {
        UI_EXECUTOR.execute(() -> {
            boolean callback = false;
            setUpElements : {
                if (names != null && positions != null) {
                    List<String> namesData = names.getData();
                    ListNumber positionsData = positions.getData();
                    if (namesData.size() == positionsData.size()) {
                        synchronized (destination) {
                            if (namesData.size() == destination.size()) {
                                for (int i = namesData.size() - 1; i > -1; i--) {
                                    destination.get(i).nameProperty().set(namesData.get(i));
                                    destination.get(i).locationProperty().set(positionsData.getDouble(i));
                                }
                                break setUpElements;
                            }
                        }
                        final IteratorNumber it = positionsData.iterator();
                        List<T> newData = (namesData.stream()
                                .map(name -> new LatticeElementData(name,it.nextDouble(),type))
                                .map(e -> (T)factory.apply(e)).collect(Collectors.toList()));
                        //these two actions are not atomic, but it's not a problem, because this is the only location
                        //where the list contents can be changed
                        synchronized (destination) {
                            destination.clear();
                            destination.addAll(newData);
                        }
                        callback = true;
                    }
                } else if (names != null) {
                    //names was updated, but locations weren't
                    List<String> namesData = names.getData();
                    synchronized (destination) {
                        if (namesData.size() == destination.size()) {
                            for (int i = namesData.size() - 1; i > -1; i--) {
                                destination.get(i).nameProperty().set(namesData.get(i));
                            }
                        }
                    }
                } else if (positions != null) {
                    //positions were updated but locations weren't
                    ListNumber positionsData = positions.getData();
                    synchronized (destination) {
                        if (positionsData.size() == destination.size()) {
                            for (int i = positionsData.size() - 1; i > -1; i--) {
                                destination.get(i).locationProperty().set(positionsData.getDouble(i));
                            }
                        }
                    }
                }
            }
            boolean called = handleEnableDisable(enable,destination,type);
            if (callback && !called) {
                latticeUpdateCallbacks.forEach(c -> c.accept(type));
            }
        });
    }

    private <T extends LatticeElement> boolean handleEnableDisable(VNumberArray enable, List<T> destination,
            LatticeElementType type) {
        boolean callback = false;
        if (enable != null) {
            ListNumber data = enable.getData();
            synchronized (destination) {
                if (data.size() == destination.size()) {
                    for (int i = data.size() - 1; i > -1; i--) {
                        destination.get(i).enabledProperty().set(data.getByte(i) == 1);
                    }
                    callback = true;
                }
            }
            if (callback) {
                latticeUpdateCallbacks.forEach(c -> c.accept(type));
            }
        }
        return callback;
    }

    private void updateLattice() {
        if (slowPVs.values().stream().allMatch(pv -> ((SlowPV)pv).connected.get())) {
            VStringArray hbpmNames = getSlowPVString.apply(Preferences.PV_HORIZONTAL_BPM_NAMES);
            VNumberArray hbpmPositions = getSlowPVNumber.apply(Preferences.PV_HORIZONTAL_BPM_POSITIONS);
            VNumberArray hbpmEnable = getPVNumberArray.apply(Preferences.PV_HORIZONTAL_BPM_ENABLED);
            handleLatticeUpdate(hbpmNames,hbpmPositions,hbpmEnable,horizontalBPMs,LatticeElementType.HORIZONTAL_BPM,
                    BPM::new);
            VStringArray vbpmNames = getSlowPVString.apply(Preferences.PV_VERTICAL_BPM_NAMES);
            VNumberArray vbpmPositions = getSlowPVNumber.apply(Preferences.PV_VERTICAL_BPM_POSITIONS);
            VNumberArray vbpmEnable = getPVNumberArray.apply(Preferences.PV_VERTICAL_BPM_ENABLED);
            handleLatticeUpdate(vbpmNames,vbpmPositions,vbpmEnable,verticalBPMs,LatticeElementType.VERTICAL_BPM,
                    BPM::new);
            VStringArray hCorrNames = getSlowPVString.apply(Preferences.PV_HORIZONTAL_CORRECTOR_NAMES);
            VNumberArray hCorrPositions = getSlowPVNumber.apply(Preferences.PV_HORIZONTAL_CORRECTOR_POSITIONS);
            VNumberArray hCorrEnable = getPVNumberArray.apply(Preferences.PV_HORIZONTAL_CORRECTOR_ENABLED);
            handleLatticeUpdate(hCorrNames,hCorrPositions,hCorrEnable,horizontalCorrectors,
                    LatticeElementType.HORIZONTAL_CORRECTOR,Corrector::new);
            VStringArray vCorrNames = getSlowPVString.apply(Preferences.PV_VERTICAL_CORRECTOR_NAMES);
            VNumberArray vCorrPositions = getSlowPVNumber.apply(Preferences.PV_VERTICAL_CORRECTOR_POSITIONS);
            VNumberArray vCorrEnable = getPVNumberArray.apply(Preferences.PV_VERTICAL_CORRECTOR_ENABLED);
            handleLatticeUpdate(vCorrNames,vCorrPositions,vCorrEnable,verticalCorrectors,
                    LatticeElementType.VERTICAL_CORRECTOR,Corrector::new);
            VStringArray quadrupoleNames = getSlowPVString.apply(Preferences.PV_QUADRUPOLE_NAMES);
            VNumberArray quadrupolePositions = getSlowPVNumber.apply(Preferences.PV_QUADRUPOLE_POSITIONS);
            handleLatticeUpdate(quadrupoleNames,quadrupolePositions,null,quadrupoles,LatticeElementType.QUADRUPOLE,
                    Quadrupole::new);
            VStringArray dipoleNames = getSlowPVString.apply(Preferences.PV_DIPOLE_NAMES);
            VNumberArray dipolePositions = getSlowPVNumber.apply(Preferences.PV_DIPOLE_POSITIONS);
            handleLatticeUpdate(dipoleNames,dipolePositions,null,dipoles,LatticeElementType.DIPOLE,Dipole::new);
            VStringArray sextupoleNames = getSlowPVString.apply(Preferences.PV_SEXTUPOLE_NAMES);
            VNumberArray sextupolePositions = getSlowPVNumber.apply(Preferences.PV_SEXTUPOLE_POSITIONS);
            handleLatticeUpdate(sextupoleNames,sextupolePositions,null,sextupoles,LatticeElementType.SEXTUPOLE,
                    Sextupole::new);
        }
    }

    private void createCorrectionResultsEntries() {
        correctionResultsEntries.put(Preferences.PV_HORIZONTAL_ORBIT_STATISTIC,
                new OrbitCorrectionResultsEntry(TABLE_ENTRY_HORIZONTAL_ORBIT));
        correctionResultsEntries.put(Preferences.PV_VERTICAL_ORBIT_STATISTIC,
                new OrbitCorrectionResultsEntry(TABLE_ENTRY_VERTICAL_ORBIT));
        correctionResultsEntries.put(Preferences.PV_GOLDEN_HORIZONTAL_ORBIT_STATISTIC,
                new OrbitCorrectionResultsEntry(TABLE_ENTRY_GOLDEN_HORIZONTAL_ORBIT));
        correctionResultsEntries.put(Preferences.PV_GOLDEN_VERTICAL_ORBIT_STATISTIC,
                new OrbitCorrectionResultsEntry(TABLE_ENTRY_GOLDEN_VERTICAL_ORBIT));
    }

    private void connectPVs(Map<String,String> pvsToconnect, Map<String,PV> destination, boolean trigger) {
        pvsToconnect.forEach((k, v) -> {
            if (v != null) {
                PVReader<VType> reader = PVManager.read(channel(v,VType.class,VType.class))
                        .maxRate(Duration.ofMillis(100));
                PV pv;
                if (trigger) {
                    PVWriter<Object> writer = PVManager.write(channel(v)).timeout(Duration.ofMillis(2000)).async();
                    pv = new PV(reader,writer);
                } else {
                    PVWriter<Object> writer = PVManager.write(channel(v)).timeout(Duration.ofMillis(2000)).async();
                    pv = new SlowPV(reader,writer);
                }
                destination.put(k,pv);
            }
        });
    }

    /**
     * Executes the command. Writes 1 into the PV with the given key. Also writes success message into the message log
     * if value (1) was successfully written or failure message if write failed.
     *
     * @param pvKey PV key of the PV to write to
     * @param successMessage message to log on successful write attempt
     * @param failureMessage message to log on failed write attempt
     */
    private void executeCommand(String pvKey, String successMessage, String failureMessage) {
        if (!throttle.isRunning()) return;
        ofNullable(pvs.get(pvKey)).ifPresent(pv -> writeData(pv,null,successMessage,failureMessage,0));
    }

    // Filter tests the PV if a new value has been received since the previous update. The filter is
    // used to eliminate unnecessary updates (if the value has not changed nothing should happen)
    private static final Predicate<PV> HAS_VALUE_FILTER = pv -> pv.hasNewValue.compareAndSet(true,false);
    private final Function<String,Optional<PV>> getPV = s -> ofNullable(pvs.get(s)).filter(HAS_VALUE_FILTER);
    private final Function<String,Optional<PV>> getNumberPV = s -> ofNullable(pvs.get(s))
            .filter(pv -> pv.value instanceof VNumber);
    private final Function<String,VNumberArray> getPVNumberArray = s -> ofNullable(pvs.get(s))
            .filter(pv -> pv.value instanceof VNumberArray).map(pv -> (VNumberArray)pv.value).orElse(null);
    private final Function<String,VNumberArray> getPVNumberArrayFilter = s -> getPV.apply(s)
            .filter(pv -> pv.value instanceof VNumberArray).map(pv -> (VNumberArray)pv.value).orElse(null);
    private final BiConsumer<String,Consumer<PV>> handlePV = (s, f) -> getPV.apply(s).ifPresent(f);
    private final Function<String,Optional<PV>> getSlowPV = s -> ofNullable(slowPVs.get(s));
    private final Function<String,VNumberArray> getSlowPVNumber = s -> getSlowPV.apply(s)
            .filter(pv -> pv.value instanceof VNumberArray).map(pv -> (VNumberArray)pv.value).orElse(null);
    private final Function<String,VStringArray> getSlowPVString = s -> getSlowPV.apply(s)
            .filter(pv -> pv.value instanceof VStringArray).map(pv -> (VStringArray)pv.value).orElse(null);

    private void update() {
        if (!throttle.isRunning()) return;
        //only update those structures that actually received a new pv value
        handlePV.accept(Preferences.PV_HORIZONTAL_ORBIT,pv -> updateOrbit(pv.value,SeriesType.HORIZONTAL_ORBIT));
        handlePV.accept(Preferences.PV_VERTICAL_ORBIT,pv -> updateOrbit(pv.value,SeriesType.VERTICAL_ORBIT));
        handlePV.accept(Preferences.PV_GOLDEN_HORIZONTAL_ORBIT,
                pv -> updateOrbit(pv.value,SeriesType.GOLDEN_HORIZONTAL_ORBIT));
        handlePV.accept(Preferences.PV_GOLDEN_VERTICAL_ORBIT,
                pv -> updateOrbit(pv.value,SeriesType.GOLDEN_VERTICAL_ORBIT));
        handlePV.accept(Preferences.PV_HORIZONTAL_REFERENCE_ORBIT,
                pv -> updateOrbit(pv.value,SeriesType.REFERENCE_HORIZONTAL_ORBIT));
        handlePV.accept(Preferences.PV_VERTICAL_REFERENCE_ORBIT,
                pv -> updateOrbit(pv.value,SeriesType.REFERENCE_VERTICAL_ORBIT));
        handlePV.accept(Preferences.PV_HORIZONTAL_DIFFERENCE_ORBIT,
                pv -> updateOrbit(pv.value,SeriesType.DIFFERENCE_HORIZONTAL_ORBIT));
        handlePV.accept(Preferences.PV_VERTICAL_DIFFERENCE_ORBIT,
                pv -> updateOrbit(pv.value,SeriesType.DIFFERENCE_VERTICAL_ORBIT));
        if (mradProperty.get()) {
            handlePV.accept(Preferences.PV_HORIZONTAL_CORRECTOR_MRAD,
                    pv -> updateCorrectors(pv.value,LatticeElementType.HORIZONTAL_CORRECTOR));
            handlePV.accept(Preferences.PV_VERTICAL_CORRECTOR_MRAD,
                    pv -> updateCorrectors(pv.value,LatticeElementType.VERTICAL_CORRECTOR));
        } else {
            handlePV.accept(Preferences.PV_HORIZONTAL_CORRECTOR_MA,
                    pv -> updateCorrectors(pv.value,LatticeElementType.HORIZONTAL_CORRECTOR));
            handlePV.accept(Preferences.PV_VERTICAL_CORRECTOR_MA,
                    pv -> updateCorrectors(pv.value,LatticeElementType.VERTICAL_CORRECTOR));
        }
        getPV.apply(Preferences.PV_OPERATION_STATUS).filter(pv -> pv.value instanceof VEnum)
                .ifPresent(pv -> statusProperty.set(((VEnum)pv.value).getValue()));
        handlePV.accept(Preferences.PV_HORIZONTAL_ORBIT_STATISTIC,
                pv -> updateOrbitCorrectionResults(pv.value,Preferences.PV_HORIZONTAL_ORBIT_STATISTIC));
        handlePV.accept(Preferences.PV_VERTICAL_ORBIT_STATISTIC,
                pv -> updateOrbitCorrectionResults(pv.value,Preferences.PV_VERTICAL_ORBIT_STATISTIC));
        handlePV.accept(Preferences.PV_GOLDEN_HORIZONTAL_ORBIT_STATISTIC,
                pv -> updateOrbitCorrectionResults(pv.value,Preferences.PV_GOLDEN_HORIZONTAL_ORBIT_STATISTIC));
        handlePV.accept(Preferences.PV_GOLDEN_VERTICAL_ORBIT_STATISTIC,
                pv -> updateOrbitCorrectionResults(pv.value,Preferences.PV_GOLDEN_VERTICAL_ORBIT_STATISTIC));
        getPV.apply(Preferences.PV_HORIZONTAL_CUTOFF).filter(pv -> pv.value instanceof VNumber)
                .ifPresent(pv -> horizontalCutOffProperty().set(((VNumber)pv.value).getValue().doubleValue()));
        getPV.apply(Preferences.PV_VERTICAL_CUTOFF).filter(pv -> pv.value instanceof VNumber)
                .ifPresent(pv -> verticalCutOffProperty().set(((VNumber)pv.value).getValue().doubleValue()));
        getPV.apply(Preferences.PV_HORIZONTAL_CORRECTION_FRACTION).filter(pv -> pv.value instanceof VNumber)
                .ifPresent(pv -> horizontalCorrectionFactorProperty()
                        .set((int)(100 * ((VNumber)pv.value).getValue().doubleValue())));
        getPV.apply(Preferences.PV_VERTICAL_CORRECTION_FRACTION).filter(pv -> pv.value instanceof VNumber)
                .ifPresent(pv -> verticalCorrectionFactorProperty()
                        .set((int)(100 * ((VNumber)pv.value).getValue().doubleValue())));
        VNumberArray vCorrEnable = getPVNumberArrayFilter.apply(Preferences.PV_VERTICAL_CORRECTOR_ENABLED);
        handleEnableDisable(vCorrEnable,verticalCorrectors,LatticeElementType.VERTICAL_CORRECTOR);
        VNumberArray hCorrEnable = getPVNumberArrayFilter.apply(Preferences.PV_HORIZONTAL_CORRECTOR_ENABLED);
        handleEnableDisable(hCorrEnable,horizontalCorrectors,LatticeElementType.HORIZONTAL_CORRECTOR);
        VNumberArray vBPMEnable = getPVNumberArrayFilter.apply(Preferences.PV_VERTICAL_BPM_ENABLED);
        handleEnableDisable(vBPMEnable,verticalBPMs,LatticeElementType.VERTICAL_BPM);
        VNumberArray hBPMEnable = getPVNumberArrayFilter.apply(Preferences.PV_HORIZONTAL_BPM_ENABLED);
        handleEnableDisable(hBPMEnable,horizontalBPMs,LatticeElementType.HORIZONTAL_BPM);
    }

    /**
     * Updates the BPM lattice element, which eventually triggers an update to the orbit charts.
     *
     * @param value new values to set on the BPMs (VNumberArray expected)
     * @param type series type defines which BPMs and which property should be updated
     */
    private void updateOrbit(final VType value, SeriesType type) {
        if (!(value instanceof VNumberArray)) return;
        final ListNumber va = ((VNumberArray)value).getData();
        if (va.size() == 0) return;
        Function<BPM,DoubleProperty> property;
        Predicate<BPM> inhibited = bpm -> false;
        List<BPM> bpms;
        boolean callback = false;
        switch (type) {
            case HORIZONTAL_ORBIT:
                property = bpm -> bpm.positionProperty();
                bpms = horizontalBPMs;
                break;
            case VERTICAL_ORBIT:
                property = bpm -> bpm.positionProperty();
                bpms = verticalBPMs;
                break;
            case GOLDEN_HORIZONTAL_ORBIT:
                property = bpm -> bpm.goldenPositionProperty();
                inhibited = bpm -> bpm.inhibitedProperty().get();
                bpms = horizontalBPMs;
                callback = true;
                break;
            case GOLDEN_VERTICAL_ORBIT:
                property = bpm -> bpm.goldenPositionProperty();
                inhibited = bpm -> bpm.inhibitedProperty().get();
                bpms = verticalBPMs;
                callback = true;
                break;
            case REFERENCE_HORIZONTAL_ORBIT:
                property = bpm -> bpm.referencePositionProperty();
                bpms = horizontalBPMs;
                break;
            case REFERENCE_VERTICAL_ORBIT:
                property = bpm -> bpm.referencePositionProperty();
                bpms = verticalBPMs;
                break;
            case DIFFERENCE_HORIZONTAL_ORBIT:
                property = bpm -> bpm.differencePositionProperty();
                bpms = horizontalBPMs;
                break;
            case DIFFERENCE_VERTICAL_ORBIT:
                property = bpm -> bpm.differencePositionProperty();
                bpms = verticalBPMs;
                break;
            default:
                return;
        }
        synchronized (bpms) {
            final Predicate<BPM> filterOut = inhibited;
            long enabledCount = bpms.stream().filter(b -> b.enabledProperty().get()).count();
            final IteratorNumber it = va.iterator();
            //no parallelism, we are on the ui thread
            if (enabledCount == va.size()) {
                bpms.stream().filter(b -> b.enabledProperty().get()).forEach(b -> {
                    double val = it.nextDouble();
                    if (!filterOut.test(b)) {
                        property.apply(b).set(val);
                    }
                });
            } else if (bpms.size() == va.size()) {
                bpms.forEach(b -> {
                    double val = it.nextDouble();
                    if (!filterOut.test(b)) {
                        property.apply(b).set(val);
                    }
                });
            } else if (bpms.size() > va.size()) {
                writeToLog(
                        String.format("The number of %s values (%d) does not match the number of enabled bpms (%d/%d).",
                                type.getSeriesName(),va.size(),enabledCount,bpms.size()),
                        Level.WARNING,empty());
                int i = 0;
                while (it.hasNext()) {
                    double val = it.nextDouble();
                    BPM bpm = bpms.get(i++);
                    if (!filterOut.test(bpm)) {
                        property.apply(bpm).set(val);
                    }
                }
            } else {
                if (bpms.isEmpty()) {
                    writeToLog("Lattice information unknown.",Level.WARNING,empty());
                }
                writeToLog(
                        String.format("The number of %s values (%d) does not match the number of enabled bpms (%d/%d).",
                                type.getSeriesName(),va.size(),enabledCount,bpms.size()),
                        Level.SEVERE,empty());
            }
        }
        if (callback) {
            goldenOrbitCallbacks.forEach(c -> c.accept(type));
        }
    }

    /**
     * Updates the correctors lattice element which eventually triggers the chart update.
     *
     * @param value new correctors values (VNumberArray expected)
     * @param type element type identifies whether the data belongs to horizontal or vertical correctors
     */
    private void updateCorrectors(final VType value, final LatticeElementType type) {
        if (!(value instanceof VNumberArray)) return;
        final ListNumber va = ((VNumberArray)value).getData();
        if (va.size() == 0) return;
        List<Corrector> correctors;
        if (type == LatticeElementType.HORIZONTAL_CORRECTOR) {
            correctors = horizontalCorrectors;
        } else if (type == LatticeElementType.VERTICAL_CORRECTOR) {
            correctors = verticalCorrectors;
        } else {
            return;
        }
        synchronized (correctors) {
            long enabledCount = correctors.stream().filter(c -> c.enabledProperty().get()).count();
            final IteratorNumber it = va.iterator();
            //no parallelism, we are on the ui thread
            if (enabledCount == va.size()) {
                correctors.stream().filter(c -> c.enabledProperty().get())
                        .forEach(c -> c.correctionProperty().set(it.nextDouble() / 1000.));
            } else if (correctors.size() == va.size()) {
                correctors.forEach(c -> c.correctionProperty().set(it.nextDouble() / 1000.));
            } else if (correctors.size() > va.size()) {
                writeToLog(
                        String.format(
                                "The number of %s kick values (%d) does not match the number of enabled correctors (%d/%d).",
                                type.getElementTypeName(),va.size(),enabledCount,correctors.size()),
                        Level.WARNING,empty());
                int i = 0;
                while (it.hasNext()) {
                    correctors.get(i++).correctionProperty().set(it.nextDouble() / 1000.);
                }
            } else {
                writeToLog(
                        String.format(
                                "The number of %s kick values (%d) does not match the number of correctors (%d/%d).",
                                type.getElementTypeName(),va.size(),enabledCount,correctors.size()),
                        Level.SEVERE,empty());
            }
        }
    }

    /**
     * Updates orbit correction results table entries.
     *
     * @param value new values (VNumberArray of length 5 expected)
     * @param pvKey the key identifying which orbit the results are for
     */
    private void updateOrbitCorrectionResults(VType value, String pvKey) {
        if (!(value instanceof VNumberArray)) return;
        ListNumber va = ((VNumberArray)value).getData();
        if (va.size() < 5) {
            writeToLog(String.format(
                    "Statistical parameters values for %s have incorrect dimension. 5 elements expected, but %d received.",
                    pvKey,va.size()),Level.SEVERE,empty());
            return;
        }
        ofNullable(correctionResultsEntries.get(pvKey)).ifPresent(entry -> {
            entry.minProperty().set(format(va.getDouble(0)));
            entry.maxProperty().set(format(va.getDouble(1)));
            entry.avgProperty().set(format(va.getDouble(2)));
            entry.rmsProperty().set(format(va.getDouble(3)));
            entry.stdProperty().set(format(va.getDouble(4)));
        });
    }

    /**
     * Format the value to be displayed in the orbit corrections statistics. The value is formatted to 3 decimal places.
     *
     * @param value the value to format
     * @return formatted value
     */
    private static double format(double value) {
        //3 decimal points should be enough
        return ((long)(value * 1000)) / 1000.;
    }

    /**
     * Writes data to the given PV. If data are provided they are written as waveform, otherwise 1 is written to the PV.
     * After the write completes, a message is logged.
     *
     * @param pv pv
     * @param data data to be written, if present
     * @param successMessage message that is logged if write completed successfully
     * @param failureMessage message that is logged if write failed for any reason
     * @param id the id of the execution, which makes sure that executions are not piled up
     */
    private void writeData(PV pv, Object data, String successMessage, String failureMessage, int id) {
        Runnable r = () -> {
            PVWriterListener<?> pvListener = new PVWriterListener<PV>() {

                @Override
                public void pvChanged(PVWriterEvent<PV> w) {
                    if (w.isWriteSucceeded()) {
                        writeToLog(successMessage,Level.INFO,empty());
                    } else if (w.isWriteFailed()) {
                        if (w.isExceptionChanged()) {
                            writeToLog(failureMessage,Level.SEVERE,of(w.getPvWriter().lastWriteException()));
                        } else {
                            writeToLog(failureMessage,Level.SEVERE,empty());
                        }
                    }
                    w.getPvWriter().removePVWriterListener(this);
                }
            };
            synchronized (pv) {
                pv.writer.addPVWriterListener(pvListener);
                if (data instanceof VNumberArray) {
                    pv.writer.write(((VNumberArray)data).getData());
                } else if (data != null) {
                    pv.writer.write(data);
                } else {
                    pv.writer.write(1);
                }
            }
        };
        if (id == 0) {
            nonUIexecutor.execute(r);
        } else {
            //this is always on UI thread
            ofNullable(tasks.get(id)).ifPresent(f -> f.cancel(false));
            tasks.put(id,scheduler.schedule(r,300,TimeUnit.MILLISECONDS));
        }
    }

    private Map<Integer,Future<?>> tasks = new HashMap<>();

    /**
     * Converts data from the string array and writes it into PV.
     *
     * @param pv destination PV to write the values to
     * @param values new values
     * @param orbitName the orbit name (used for logging only)
     */
    private void convertAndWriteData(PV pv, String[] values, String orbitName, int id) {
        double[] array = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = Double.parseDouble(values[i].trim()); // throw exception if value is not double
        }
        String successMessage = orbitName + " was successfully updated.";
        String failureMessage = "Error occured while updating " + orbitName + ".";
        writeData(pv,new ArrayDouble(array),successMessage,failureMessage,0);
    }

    /**
     * Writes orbit values with weights into the file.
     *
     * @param file destination file in which values will be written
     * @param xOrbitKey horizontal or golden horizontal orbit PV key
     * @param yOrbitKey vertical or golden vertical orbit PV key
     * @param successMessage logged message if write succeeded
     * @param failureMessage logged message if write failed
     */
    private void writeOrbitToFile(File file, String comment, String xOrbitKey, String yOrbitKey, String successMessage,
            String failureMessage) {
        if (!throttle.isRunning()) return;
        final PV xOrbitPV = pvs.get(xOrbitKey);
        final PV yOrbitPV = pvs.get(yOrbitKey);
        nonUIexecutor.execute(() -> {
            String xOrbit = xOrbitPV != null ? getStringValue(xOrbitPV.value) : EMPTY_STRING;
            String yOrbit = yOrbitPV != null ? getStringValue(yOrbitPV.value) : EMPTY_STRING;
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getPath()),StandardCharsets.UTF_8)) {
                if (comment != null) {
                    writer.write("<" + comment + ">");
                    writer.write(NEW_LINE);
                    writer.write(NEW_LINE);
                }
                writer.write(xOrbit);
                writer.write(NEW_LINE);
                writer.write(yOrbit);
                writeToLog(successMessage,Level.INFO,empty());
            } catch (Exception e) {
                writeToLog(failureMessage,Level.SEVERE,of(e));
            }
        });
    }

    /**
     * Transforms the value of the given {@link VType} to a human readable string.
     *
     * @param value data to transform
     * @return string representation of the data
     */
    private static String getStringValue(VType value) {
        if (value != null && value instanceof VNumberArray) {
            ListNumber data = ((VNumberArray)value).getData();
            int size = data.size();
            StringBuilder sb = new StringBuilder(size * 15);
            for (int i = 0; i < size; i++) {
                sb.append(data.getDouble(i)).append(' ');
            }
            return sb.toString();
        }
        return EMPTY_STRING;
    }

    /**
     * Writes the given message to the message log.
     *
     * @param message message to write
     * @param error true indicates that the log is an error type message (logged as SEVERE), false indicates an info
     *        message (logged as INFO)
     * @param exception exception to log; if present, the log will always be an error type
     */
    private void writeToLog(String message, Level level, Optional<Exception> exception) {
        if (message == null) return;
        if (exception.isPresent()) {
            OrbitCorrectionPlugin.LOGGER.log(Level.SEVERE,message,exception.get());
        } else {
            OrbitCorrectionPlugin.LOGGER.log(level,message);
        }
    }
}
