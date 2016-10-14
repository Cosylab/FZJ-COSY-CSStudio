package com.cosylab.fzj.cosy.oc.ui;

import static org.diirt.datasource.ExpressionLanguage.channel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.diirt.datasource.PVManager;
import org.diirt.datasource.PVReader;
import org.diirt.datasource.PVWriter;
import org.diirt.datasource.PVWriterEvent;
import org.diirt.datasource.PVWriterListener;
import org.diirt.util.array.ArrayDouble;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.Alarm;
import org.diirt.vtype.AlarmSeverity;
import org.diirt.vtype.Time;
import org.diirt.vtype.VEnum;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VType;
import org.diirt.vtype.ValueFactory;

import com.cosylab.fzj.cosy.oc.LatticeElementDataLoader;
import com.cosylab.fzj.cosy.oc.LatticeElementType;
import com.cosylab.fzj.cosy.oc.OrbitCorrectionService;
import com.cosylab.fzj.cosy.oc.Preferences;
import com.cosylab.fzj.cosy.oc.ui.model.BPM;
import com.cosylab.fzj.cosy.oc.ui.model.Corrector;
import com.cosylab.fzj.cosy.oc.ui.model.SeriesType;
import com.cosylab.fzj.cosy.oc.ui.util.GUIUpdateThrottle;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * <code>OrbitCorrectionController</code> is the controller for the orbit correction viewer. It provides the logic for
 * showing orbit correction status and executing orbit correction commands..
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 *
 */
public class OrbitCorrectionController {

    /** The rate at which the UI is updated */
    private static final long UPDATE_RATE = 500;
    private static Executor UI_EXECUTOR = Platform::runLater;

    // Orbit correction results table row names
    private static final String HORIZONTAL_ORBIT_TABLE_ENTRY = "Horizontal Orbit";
    private static final String VERTICAL_ORBIT_TABLE_ENTRY = "Vertical Orbit";
    private static final String GOLDEN_HORIZONTAL_ORBIT_TABLE_ENTRY = "Golden Horizontal Orbit";
    private static final String GOLDEN_VERTICAL_ORBIT_TABLE_ENTRY = "Golden Vertical Orbit";

    // status messages
    private static final String START_ORBIT_MEASURING_CMD_SUCCESS_MSG = "Start orbit measuring command was successfully sent.";
    private static final String START_ORBIT_MEASURING_CMD_FAILURE_MSG = "Error occured while sending start orbit measuring command.";
    private static final String STOP_ORBIT_MEASURING_CMD_SUCCESS_MSG = "Stop orbit measuring command was successfully sent.";
    private static final String STOP_ORBIT_MEASURING_CMD_FAILURE_MSG = "Error occured while sending stop orbit measuring command.";
    private static final String MEASURE_ORBIT_ONCE_CMD_SUCCESS_MSG = "Measure orbit once command was successfully sent.";
    private static final String MEASURE_ORBIT_ONCE_CMD_FAILURE_MSG = "Error occured while sending measure orbit once command.";
    private static final String START_CORRECTING_ORBIT_CMD_SUCCESS_MSG = "Start correcting orbit command was successfully sent.";
    private static final String START_CORRECTING_ORBIT_CMD_FAILURE_MSG = "Error occured while sending start correcting orbit command.";
    private static final String STOP_CORRECTING_ORBIT_CMD_SUCCESS_MSG = "Stop correcting orbit command was successfully sent.";
    private static final String STOP_CORRECTING_ORBIT_CMD_FAILURE_MSG = "Error occured while sending stop correcting orbit command.";
    private static final String CORRECT_ORBIT_ONCE_CMD_SUCCESS_MSG = "Correct orbit once command was successfully sent.";
    private static final String CORRECT_ORBIT_ONCE_CMD_FAILURE_MSG = "Error occured while sending correct orbit once command.";
    private static final String MEASURE_ORM_CMD_SUCCESS_MSG = "Measure orbit respone matrix command was successfully sent.";
    private static final String MEASURE_ORM_CMD_FAILURE_MSG = "Error occured while sending Measure orbit respone matrix command.";
    private static final String EXPORT_CURRENT_ORBIT_SUCCESS_MSG = "Current orbit was successfully exported.";
    private static final String EXPORT_CURRENT_ORBIT_FAILURE_MSG = "Error occured while exporting current orbit.";
    private static final String UPLOAD_GOLDEN_ORBIT_SUCCESS_MSG = "Golden orbit was successfully uploaded.";
    private static final String UPLOAD_GOLDEN_ORBIT_FAILURE_MSG = "Error occured while uploading golden orbit.";
    private static final String DOWNLOAD_GOLDEN_ORBIT_SUCCESS_MSG = "Golden orbit was successfully downloaded.";
    private static final String DOWNLOAD_GOLDEN_ORBIT_FAILURE_MSG = "Error occured while downloading golden orbit.";
    private static final String USE_CURRENT_HORIZONTAL_SUCCESS_MSG = "Golden horizontal orbit was successfully updated to current.";
    private static final String USE_CURRENT_HORIZONTAL_FAILURE_MSG = "Error occured while updating golden horizontal orbit to current.";
    private static final String USE_CURRENT_VERTICAL_SUCCESS_MSG = "Golden vertical orbit was successfully updated to current.";
    private static final String USE_CURRENT_VERTICAL_FAILURE_MSG = "Error occured while updatind golden vertical orbit to current.";
    private static final String UPLOAD_ORM_SUCCESS_MSG = "Orbit response matrix was successfully uploaded.";
    private static final String UPLOAD_ORM_FAILURE_MSG = "Error occured while uploading orbit response matrix.";
    private static final String DOWNLOAD_ORM_SUCCESS_MSG = "Orbit response matrix was successfully downloaded.";
    private static final String DOWNLOAD_ORM_FAILURE_MSG = "Error occured while downloading orbit response matrix.";

    private static final String EMPTY_STRING = "";

    private class PV {
        final PVReader<VType> reader;
        final PVWriter<Object> writer;
        VType value;

        PV(PVReader<VType> reader, PVWriter<Object> writer) {
            this.reader = reader;
            this.writer = writer;
            this.reader.addPVReaderListener(e -> {
                if (e.isExceptionChanged()) {
                    writeToLog("DIIRT Connection Error.", true,
                            Optional.of(e.getPvReader().lastException()));
                }
                value = e.getPvReader().isConnected() ? e.getPvReader().getValue() : null;
                throttle.trigger();
            });
            this.value = reader.getValue();
        }

        void dispose() {
            if (!reader.isClosed()) {
                reader.close();
            }
            if (!writer.isClosed()) {
                writer.close();
            }
        }
    }

    private final BooleanProperty mradProperty = new SimpleBooleanProperty(true);
    private final StringProperty messageLogProperty = new SimpleStringProperty(EMPTY_STRING);
    private final StringProperty statusProperty = new SimpleStringProperty();

    private final GUIUpdateThrottle throttle = new GUIUpdateThrottle(20, UPDATE_RATE) {
        @Override
        protected void fire() {
            UI_EXECUTOR.execute(() -> update());
        }
    };

    private Map<String, PV> pvs = new HashMap<>();
    private Map<String, OrbitCorrectionResultsEntry> correctionResultsEntries = new LinkedHashMap<>(4);
    private List<BPM> bpms = new ArrayList<>();
    private List<Corrector> correctors = new ArrayList<>();

    /**
     * Constructs a new controller for the orbit correction view.
     */
    public OrbitCorrectionController() {
        loadLatticeElements();
        createCorrectionResultsEntries();
        connectPVs();
        start();

        mradProperty.addListener(e -> throttle.trigger());
    }

    /**
     * Start the GUI throttle.
     */
    protected void start() {
        throttle.start();
    }

    /**
     * @return list of all BPMs.
     */
    public List<BPM> getBpms() {
        return bpms;
    }

    /**
     * @return list of all correctors.
     */
    public List<Corrector> getCorrectors() {
        return correctors;
    }

    /**
     * @return list of orbit correction results table entries.
     */
    public Map<String, OrbitCorrectionResultsEntry> getOrbitCorrectionResults() {
        return correctionResultsEntries;
    }

    /**
     * @return the property providing the current message log.
     */
    public StringProperty messageLogProperty() {
        return messageLogProperty;
    }

    /**
     * @return the property providing the selected unit (mrad or mA).
     */
    public BooleanProperty mradProperty() {
        return mradProperty;
    }

    /**
     * @return the property providing the current operation status.
     */
    public StringProperty statusProperty() {
        return statusProperty;
    }

    /**
     * Calls command which starts measuring orbit and updating horizontal and vertical orbit periodically.
     */
    public void startMeasuringOrbit() {
        executeCommand(Preferences.START_MEASURING_ORBIT_PV, START_ORBIT_MEASURING_CMD_SUCCESS_MSG,
                START_ORBIT_MEASURING_CMD_FAILURE_MSG);
    }

    /**
     * Calls command which stops measuring orbit. Horizontal and vertical orbit keep the value as they have
     * it at this time.
     */
    public void stopMeasuringOrbit() {
        executeCommand(Preferences.STOP_MEASURING_ORBIT_PV, STOP_ORBIT_MEASURING_CMD_SUCCESS_MSG,
                STOP_ORBIT_MEASURING_CMD_FAILURE_MSG);
    }

    /**
     * Calls command which performs one orbit measurement for X and Y and update horizontal and vertical
     * orbit.
     */
    public void measureOrbitOnce() {
        executeCommand(Preferences.MEASURE_ORBIT_ONCE_PV, MEASURE_ORBIT_ONCE_CMD_SUCCESS_MSG,
                MEASURE_ORBIT_ONCE_CMD_FAILURE_MSG);
    }

    /**
     * Calls command which starts measuring orbit periodically and perform orbit correction for every
     * measurement.
     */
    public void startCorrectingOrbit() {
        executeCommand(Preferences.START_CORRECTING_ORBIT_PV, START_CORRECTING_ORBIT_CMD_SUCCESS_MSG,
                START_CORRECTING_ORBIT_CMD_FAILURE_MSG);
    }

    /**
     * Calls command which stops measuring orbit, all PVs keep their last value.
     */
    public void stopCorrectingOrbit() {
        executeCommand(Preferences.STOP_CORRECTING_ORBIT_PV, STOP_CORRECTING_ORBIT_CMD_SUCCESS_MSG,
                STOP_CORRECTING_ORBIT_CMD_FAILURE_MSG);
    }

    /**
     * Calls command which uses the last horizontal and vertical orbit, calculate the corrections and apply
     * them once
     */
    public void correctOrbitOnce() {
        executeCommand(Preferences.CORRECT_ORBIT_ONCE_PV, CORRECT_ORBIT_ONCE_CMD_SUCCESS_MSG,
                CORRECT_ORBIT_ONCE_CMD_FAILURE_MSG);
    }

    /**
     * Exports current horizontal and vertical orbit with weights into the given file.
     *
     * @param file file in which horizontal and vertical orbit with weights will be written
     */
    public void exportCurrentOrbit(File file) {
        writeOrbitInFile(file, Preferences.HORIZONTAL_ORBIT_PV, Preferences.VERTICAL_ORBIT_PV,
                EXPORT_CURRENT_ORBIT_SUCCESS_MSG, EXPORT_CURRENT_ORBIT_FAILURE_MSG);
    }

    /**
     * Uploads new golden horizontal and vertical orbit with weights from the given file.
     *
     * @param file file in which golden horizontal and vertical orbit with weights are written
     */
    public void uploadGoldenOrbit(File file) {
        final PV xOrbitPV = pvs.get(Preferences.GOLDEN_HORIZONTAL_ORBIT_PV);
        final PV xWeightsPV = pvs.get(Preferences.HORIZONTAL_ORBIT_WEIGHTS_PV);
        final PV yOrbitPV = pvs.get(Preferences.GOLDEN_VERTICAL_ORBIT_PV);
        final PV yWeightsPV = pvs.get(Preferences.VERTICAL_ORBIT_WEIGHTS_PV);

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
                if (xOrbitPV != null && lineCounter == 1) {
                    convertAndWriteData(xOrbitPV, values, "Golden Horizontal Orbit");
                } else if (xWeightsPV != null && lineCounter == 2) {
                    convertAndWriteData(xWeightsPV, values, "Horizontal Orbit Weights");
                } else if (yOrbitPV != null && lineCounter == 3) {
                    convertAndWriteData(yOrbitPV, values, "Golden Vertical Orbit");
                } else if (xOrbitPV != null && lineCounter == 4) {
                    convertAndWriteData(yWeightsPV, values, "Vertical OrbitWeights");
                } else {
                    break;
                }
            }
            writeToLog(UPLOAD_GOLDEN_ORBIT_SUCCESS_MSG, false, Optional.empty());
        } catch (Exception e) {
            writeToLog(UPLOAD_GOLDEN_ORBIT_FAILURE_MSG, true, Optional.of(e));
        }
    }

    /**
     * Downloads current golden horizontal and vertical orbit with weights into the given file.
     *
     * @param file file in which golden horizontal and vertical orbit with weights will be written
     */
    public void downloadGoldenOrbit(File file) {
        writeOrbitInFile(file, Preferences.GOLDEN_HORIZONTAL_ORBIT_PV, Preferences.VERTICAL_ORBIT_WEIGHTS_PV,
                DOWNLOAD_GOLDEN_ORBIT_SUCCESS_MSG, DOWNLOAD_GOLDEN_ORBIT_FAILURE_MSG);
    }

    /**
     * Uses current horizontal and vertical orbit values as golden horizontal and vertical orbit.
     */
    public void useCurrent() {
        final PV horizontalOrbit = pvs.get(Preferences.HORIZONTAL_ORBIT_PV);
        final PV verticalOrbit = pvs.get(Preferences.VERTICAL_ORBIT_PV);
        final PV goldenHorizontalOrbit = pvs.get(Preferences.GOLDEN_HORIZONTAL_ORBIT_PV);
        final PV goldenVerticalOrbit = pvs.get(Preferences.GOLDEN_VERTICAL_ORBIT_PV);

        if (horizontalOrbit != null && goldenHorizontalOrbit != null) {
            writeData(goldenHorizontalOrbit, Optional.of(horizontalOrbit.value), USE_CURRENT_HORIZONTAL_SUCCESS_MSG,
                    USE_CURRENT_HORIZONTAL_FAILURE_MSG);
        }
        if (verticalOrbit != null && goldenVerticalOrbit != null) {
            writeData(goldenVerticalOrbit, Optional.of(verticalOrbit.value), USE_CURRENT_VERTICAL_SUCCESS_MSG,
                    USE_CURRENT_VERTICAL_FAILURE_MSG);
        }
        throttle.trigger();
    }

    /**
     * Downloads response matrix.
     */
    public void downloadResponseMatrix(File file) {
        final PV ormPV = pvs.get(Preferences.ORM_PV);
        final PV horizontalOrbitPV = pvs.get(Preferences.HORIZONTAL_ORBIT_PV);
        final PV horizontalCorrectorPV = pvs.get(Preferences.HORIZONTAL_CORRECTOR_MA_PV);

        int n = horizontalOrbitPV != null ? ((VNumberArray) horizontalOrbitPV.value).getData().size() : -1;
        int m = horizontalCorrectorPV != null ? ((VNumberArray) horizontalCorrectorPV.value).getData().size() : -1;

        if (n == -1 || m == -1 || ormPV == null) {
            return;
        }

        final VNumberArray value = (VNumberArray) ormPV.value;
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getPath()))) {
            if (value != null) {
                StringBuilder sb = new StringBuilder(n * m);
                ListNumber data = value.getData();
                for (int i = 0; i < data.size(); i++) {
                    if (i != 0 && i % n == 0) {
                        sb.append('\n');
                    }
                    sb.append(data.getDouble(i)).append(' ');
                }
                writer.write(sb.toString());
            }
            writeToLog(DOWNLOAD_ORM_SUCCESS_MSG, false, Optional.empty());
        } catch (Exception e) {
            writeToLog(DOWNLOAD_ORM_FAILURE_MSG, true, Optional.of(e));
        }
    }

    /**
     * Uploads response matrix.
     */
    public void uploadResponseMatrix(File file) {
        final PV ormPV = pvs.get(Preferences.ORM_PV);
        final PV horizontalOrbitPV = pvs.get(Preferences.HORIZONTAL_ORBIT_PV);
        final PV horizontalCorrectorPV = pvs.get(Preferences.HORIZONTAL_CORRECTOR_MA_PV);

        int n = horizontalOrbitPV != null ? ((VNumberArray) horizontalOrbitPV.value).getData().size() : -1;
        int m = horizontalCorrectorPV != null ? ((VNumberArray) horizontalCorrectorPV.value).getData().size() : -1;

        if (n == -1 || m == -1 || ormPV == null) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(file.getPath()))) {
            StringBuilder sb = new StringBuilder (n * m);
            String line;
            Pattern matchSpaces = Pattern.compile("\\s+");
            Pattern matchTabs = Pattern.compile("\\t+");
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                line = matchSpaces.matcher(matchTabs.matcher(line).replaceAll(" ")).replaceAll(" ");
                sb.append(line).append(' ');
            }
            String[] waveform = sb.toString().split(" ");
            convertAndWriteData(ormPV, waveform, "Orbit response matrix");
            writeToLog(UPLOAD_ORM_SUCCESS_MSG, false, Optional.empty());
        } catch (Exception e) {
            writeToLog(UPLOAD_ORM_FAILURE_MSG, true, Optional.of(e));
        }
    }

    /**
     * Measure orbit response matrix.
     */
    public void measure() {
        executeCommand(Preferences.MEASURE_ORM_PV, MEASURE_ORM_CMD_SUCCESS_MSG, MEASURE_ORM_CMD_FAILURE_MSG);
    }

    /**
     * Dispose all PVs.
     */
    public void dispose() {
        pvs.forEach((k, v) -> v.dispose());
        pvs.clear();
        bpms.clear();
        correctors.clear();
    }

    /**
     * Loads lattice elements from the file.
     */
    private void loadLatticeElements() {
        LatticeElementDataLoader dataLoader = new LatticeElementDataLoader();
        dataLoader.loadLatticeElements().forEach(e -> {
            if (e.getType() == LatticeElementType.BPM) {
                bpms.add(new BPM(e));
            } else {
                correctors.add(new Corrector(e));
            }
        });
    }

    /**
     * Creates orbit correction results table entries.
     */
    private void createCorrectionResultsEntries() {
        correctionResultsEntries.put(Preferences.HORIZONTAL_ORBIT_STATISTIC_PV,
                new OrbitCorrectionResultsEntry(HORIZONTAL_ORBIT_TABLE_ENTRY));
        correctionResultsEntries.put(Preferences.VERTICAL_ORBIT_STATISTIC_PV,
                new OrbitCorrectionResultsEntry(VERTICAL_ORBIT_TABLE_ENTRY));
        correctionResultsEntries.put(Preferences.GOLDEN_HORIZONTAL_ORBIT_STATISTIC_PV,
                new OrbitCorrectionResultsEntry(GOLDEN_HORIZONTAL_ORBIT_TABLE_ENTRY));
        correctionResultsEntries.put(Preferences.GOLDEN_VERTICAL_ORBIT_STATISTIC_PV,
                new OrbitCorrectionResultsEntry(GOLDEN_VERTICAL_ORBIT_TABLE_ENTRY));
    }

    /**
     * Connects all PVs.
     */
    private void connectPVs() {
        Preferences.getInstance().getPVNames().forEach((k, v) -> {
            if (v != null) {
                PVReader<VType> reader = PVManager.read(channel(v, VType.class, VType.class))
                        .maxRate(Duration.ofMillis(100));
                PVWriter<Object> writer = PVManager.write(channel(v)).timeout(Duration.ofMillis(2000)).async();
                pvs.put(k, new PV(reader, writer));
            }
        });
    }

    /**
     * Executes the command. Writes 1 into the PV with the given key. Also writes success message into the message log
     * if value (1) was successfully written or failure message if write fails.
     *
     * @param pvKey PV key
     * @param successMessage success message
     * @param failureMessage
     */
    private void executeCommand(String pvKey, String successMessage, String failureMessage) {
        final PV pv = pvs.get(pvKey);
        if (pv != null) {
            writeData(pv, Optional.empty(), successMessage, failureMessage);
        }
    }

    /**
     * Method which updates PV values.
     */
    private void update() {
        final PV horizontalOrbitPV = pvs.get(Preferences.HORIZONTAL_ORBIT_PV);
        if (horizontalOrbitPV != null) {
            updateOrbit(getWaveformValues(horizontalOrbitPV.value), SeriesType.HORIZONTAL_ORBIT);
        }
        final PV verticalOrbitPV = pvs.get(Preferences.VERTICAL_ORBIT_PV);
        if (verticalOrbitPV != null) {
            updateOrbit(getWaveformValues(verticalOrbitPV.value), SeriesType.VERTICAL_ORBIT);
        }
        final PV goldenHorizontalOrbitPV = pvs.get(Preferences.GOLDEN_HORIZONTAL_ORBIT_PV);
        if (goldenHorizontalOrbitPV != null) {
            updateOrbit(getWaveformValues(goldenHorizontalOrbitPV.value), SeriesType.GOLDEN_HORIZONTAL_ORBIT);
        }
        final PV goldenVerticalOrbitPV = pvs.get(Preferences.GOLDEN_VERTICAL_ORBIT_PV);
        if (goldenVerticalOrbitPV != null) {
            updateOrbit(getWaveformValues(goldenVerticalOrbitPV.value), SeriesType.GOLDEN_VERTICAL_ORBIT);
        }
        final PV horizontalCorrectorMradPV = pvs.get(Preferences.HORIZONTAL_CORRECTOR_MRAD_PV);
        if (horizontalCorrectorMradPV != null && mradProperty.get()) {
            updateCorrectors(getWaveformValues(horizontalCorrectorMradPV.value), LatticeElementType.HORIZONTAL_CORRECTOR);
        }
        final PV verticalCorrectorMradPV = pvs.get(Preferences.VERTICAL_CORRECTOR_MRAD_PV);
        if (verticalCorrectorMradPV != null && mradProperty.get()) {
            updateCorrectors(getWaveformValues(horizontalCorrectorMradPV.value), LatticeElementType.VERTICAL_CORRECTOR);
        }
        final PV horizontalCorrectorMaPV = pvs.get(Preferences.HORIZONTAL_CORRECTOR_MA_PV);
        if (horizontalCorrectorMaPV != null && !mradProperty.get()) {
            updateCorrectors(getWaveformValues(horizontalCorrectorMaPV.value), LatticeElementType.HORIZONTAL_CORRECTOR);
        }
        final PV verticalCorrectorMaPV = pvs.get(Preferences.VERTICAL_CORRECTOR_MA_PV);
        if (verticalCorrectorMaPV != null && !mradProperty.get()) {
            updateCorrectors(getWaveformValues(verticalCorrectorMaPV.value), LatticeElementType.VERTICAL_CORRECTOR);
        }
        final PV operationStatusPV = pvs.get(Preferences.OPERATION_STATUS_PV);
        if (operationStatusPV != null && operationStatusPV.value instanceof VEnum) {
            statusProperty.set(((VEnum) operationStatusPV.value).getValue());
        }
        final PV horizontalOrbitStatisticPV = pvs.get(Preferences.HORIZONTAL_ORBIT_STATISTIC_PV);
        if (horizontalOrbitStatisticPV != null) {
            updateOrbitCorrectionResults(Preferences.HORIZONTAL_ORBIT_STATISTIC_PV, getWaveformValues(horizontalOrbitStatisticPV.value));
        }
        final PV verticalOrbitStatisticPV = pvs.get(Preferences.VERTICAL_ORBIT_STATISTIC_PV);
        if (verticalOrbitStatisticPV != null) {
            updateOrbitCorrectionResults(Preferences.VERTICAL_ORBIT_STATISTIC_PV,getWaveformValues(verticalOrbitStatisticPV.value));
        }
        final PV goldenHorizontalOrbitStatisticPV = pvs.get(Preferences.GOLDEN_HORIZONTAL_ORBIT_STATISTIC_PV);
        if (goldenHorizontalOrbitStatisticPV != null) {
            updateOrbitCorrectionResults(Preferences.GOLDEN_HORIZONTAL_ORBIT_STATISTIC_PV,
                    getWaveformValues(goldenHorizontalOrbitStatisticPV.value));
        }
        final PV goldenVerticalOrbitStatisticPV = pvs.get(Preferences.GOLDEN_VERTICAL_ORBIT_STATISTIC_PV);
        if (goldenVerticalOrbitStatisticPV != null) {
            updateOrbitCorrectionResults(Preferences.GOLDEN_VERTICAL_ORBIT_STATISTIC_PV,
                    getWaveformValues(goldenVerticalOrbitStatisticPV.value));
        }
    }

    /**
     * Updates orbit chart values.
     *
     * @param values new values
     * @param type series type
     */
    private void updateOrbit(List<Double> values, SeriesType type) {
        int valueCounter = 0;
        for (BPM bpm : bpms) {
            if (type == SeriesType.HORIZONTAL_ORBIT) {
                bpm.horizontalOrbitProperty().set(values.get(valueCounter));
            } else if (type == SeriesType.VERTICAL_ORBIT) {
                bpm.verticalOrbitProperty().set(values.get(valueCounter));
            } else if (type == SeriesType.GOLDEN_HORIZONTAL_ORBIT) {
                bpm.goldenHorizontalOrbitProperty().set(values.get(valueCounter));
            } else if (type == SeriesType.GOLDEN_VERTICAL_ORBIT) {
                bpm.goldenVerticalOrbitProperty().set(values.get(valueCounter));
            }
            valueCounter++;
        }
    }

    /**
     * Updates correctors chart values.
     *
     * @param values new correctors values
     * @param type element type
     */
    private void updateCorrectors(List<Double> values, LatticeElementType type) {
        int valueCounter = 0;
        for (Corrector corrector : correctors) {
            if (corrector.getElementData().getType() == type) {
                if (type == LatticeElementType.HORIZONTAL_CORRECTOR) {
                    corrector.horizontalCorrectionProperty().set(values.get(valueCounter));
                } else {
                    corrector.verticalCorrectionProperty().set(values.get(valueCounter));
                }
                valueCounter++;
            }
        }
    }

    /**
     * Updates orbit correction results table entries.
     *
     * @param pvKey PV key
     * @param values new values
     */
    private void updateOrbitCorrectionResults(String pvKey, List<Double> values) {
        final OrbitCorrectionResultsEntry correctionResultsEntry = correctionResultsEntries.get(pvKey);
        if (correctionResultsEntry != null && values.size() >= 5) {
            correctionResultsEntry.minProperty().set(values.get(0));
            correctionResultsEntry.maxProperty().set(values.get(1));
            correctionResultsEntry.avgProperty().set(values.get(2));
            correctionResultsEntry.rmsProperty().set(values.get(3));
            correctionResultsEntry.stdProperty().set(values.get(4));
        }
    }

    /**
     * Writes data to the given PV. If data is presented write data as waveform otherwise writes 1 to the PV.
     *
     * @param pv pv
     * @param data data to be written, if presented
     * @param successMessage success message
     * @param failureMessage failure message
     */
    private void writeData(PV pv, Optional<VType> data, String successMessage, String failureMessage) {
        PVWriterListener<?> pvListener = new PVWriterListener<PV>() {
            @Override
            public void pvChanged(PVWriterEvent<PV> w) {
                if (w.isWriteSucceeded()) {
                    writeToLog(successMessage, false, Optional.empty());
                } else if (w.isWriteFailed()) {
                    if (w.isExceptionChanged()) {
                        writeToLog(failureMessage, true,
                                Optional.of(w.getPvWriter().lastWriteException()));
                    } else {
                        writeToLog(failureMessage, true, Optional.empty());
                    }
                }
                pv.writer.removePVWriterListener(this);
            }
        };
        pv.writer.addPVWriterListener(pvListener);
        if (data.isPresent()) {
            pv.writer.write(((VNumberArray) data.get()).getData());
        } else {
            pv.writer.write(1);
        }
    }

    /**
     * Converts data from string array and writes it into PV.
     *
     * @param pv pv
     * @param values new values
     * @param orbitName orbit name
     */
    private void convertAndWriteData(PV pv, String[] values, String orbitName) {
        Alarm alarm = ValueFactory.newAlarm(AlarmSeverity.NONE, "USER DEFINED");
        Time time = ValueFactory.timeNow();
        ListNumber list = null;
        double[] array = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = Double.parseDouble(values[i].trim()); // throw exception if value is not double
        }
        list = new ArrayDouble(array);
        VNumberArray data = ValueFactory.newVNumberArray(list, alarm, time, (VNumberArray) pv.value);

        String successMessage = orbitName + " was successfully updated.";
        String failureMessage = "Error occured while updating " + orbitName + ".";
        writeData(pv, Optional.of(data), successMessage, failureMessage);
    }

    /**
     * Writes orbit values with weights into the file.
     *
     * @param file file in which orbit values with weights will be written
     * @param xOrbitKey horizontal or golden horizontal orbit PV key
     * @param yOrbitKey vertical or golden vertical orbit PV key
     * @param successMessage success message
     * @param failureMessage failure message
     */
    private void writeOrbitInFile(File file, String xOrbitKey, String yOrbitKey, String successMessage, String failureMessage) {
        final PV xOrbitPV = pvs.get(xOrbitKey);
        final PV xWeightsPV = pvs.get(Preferences.HORIZONTAL_ORBIT_WEIGHTS_PV);
        final PV yOrbitPV = pvs.get(yOrbitKey);
        final PV yWeightsPV = pvs.get(Preferences.VERTICAL_ORBIT_WEIGHTS_PV);

        String xOrbit = xOrbitPV != null ? getStringValue(xOrbitPV.value) : EMPTY_STRING;
        String xWeights = xWeightsPV != null ? getStringValue(xWeightsPV.value) : EMPTY_STRING;
        String yOrbit = yOrbitPV != null ? getStringValue(yOrbitPV.value) : EMPTY_STRING;
        String yWeights = yWeightsPV != null ? getStringValue(yWeightsPV.value) : EMPTY_STRING;

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getPath()))) {
            writer.write(xOrbit);
            writer.newLine();
            writer.write(xWeights);
            writer.newLine();
            writer.write(yOrbit);
            writer.newLine();
            writer.write(yWeights);
            writeToLog(successMessage, false, Optional.empty());
        } catch (Exception e) {
            writeToLog(failureMessage, true, Optional.of(e));
        }
    }

    /**
     * @param value the value
     * @return a list of double values for the given value.
     */
    private List<Double> getWaveformValues(VType value) {
        if (value instanceof VNumberArray) {
            ListNumber data = ((VNumberArray) value).getData();
            int size = data.size();
            List<Double> values = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                values.add(data.getDouble(i));
            }
            return values;
        }
        return new ArrayList<>();
    }

    /**
     * Transforms the value of the given {@link VType} to a human readable string.
     *
     * @param type the data to transform
     *
     * @return string representation of the data.
     */
    private String getStringValue(VType value) {
        if (value != null && value instanceof VNumberArray) {
            ListNumber data = ((VNumberArray) value).getData();
            int size = data.size();
            StringBuilder sb = new StringBuilder(size * 2 - 1);
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
     */
    private void writeToLog(String message, boolean error, Optional<Exception> e) {
        if (error) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE, message);
        } if (error && e.isPresent()) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE, message, e.get());
        } else {
            OrbitCorrectionService.LOGGER.log(Level.FINE, message);
        }
        messageLogProperty.set(message);
        messageLogProperty.set("\n");
    }
}