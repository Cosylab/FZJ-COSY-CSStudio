package com.cosylab.fzj.cosy.oc.ui;

import static org.diirt.datasource.ExpressionLanguage.channel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.logging.Level;

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
    public static final long UPDATE_RATE = 500;
    private static Executor UI_EXECUTOR = Platform::runLater;

    // Orbit correction results table row names
    private static final String HORIZONTAL_ORBIT_TABLE_ENTRY = "Horizontal Orbit";
    private static final String VERTICAL_ORBIT_TABLE_ENTRY = "Vertical Orbit";
    private static final String GOLDEN_HORIZONTAL_ORBIT_TABLE_ENTRY = "Golden Horizontal Orbit";
    private static final String GOLDEN_VERTICAL_ORBIT_TABLE_ENTRY = "Golden Vertical Orbit";

    private static final String EMPTY_STRING = "";

    private class PV {
        final String pvKey;
        final PVReader<VType> reader;
        final PVWriter<Object> writer;
        VType value;

        PV(String pvKey, PVReader<VType> reader, PVWriter<Object> writer) {
            this.pvKey = pvKey;
            this.reader = reader;
            this.writer = writer;
            this.reader.addPVReaderListener(e -> {
                if (e.isExceptionChanged()) {
                    OrbitCorrectionService.LOGGER.log(Level.WARNING, "DIIRT Connection Error.",
                            e.getPvReader().lastException());
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
            update();
        }
    };

    private List<PV> pvs = new ArrayList<>();
    private List<OrbitCorrectionResultsEntry> correctionResultsEntries = new ArrayList<>();
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
    public List<OrbitCorrectionResultsEntry> getOrbitCorrectionResults() {
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
        String successMessage = "Start orbit measuring command was successfully sent.";
        String failureMessage = "Error occured while sending start orbit measuring command.";
        executeCommand(Preferences.START_MEASURING_ORBIT_PV, successMessage, failureMessage);
    }

    /**
     * Calls command which stops measuring orbit. Horizontal and vertical orbit keep the value as they have
     * it at this time.
     */
    public void stopMeasuringOrbit() {
        String successMessage = "Stop orbit measuring command was successfully sent.";
        String failureMessage = "Error occured while sending stop orbit measuring command.";
        executeCommand(Preferences.STOP_MEASURING_ORBIT_PV, successMessage, failureMessage);
    }

    /**
     * Calls command which performs one orbit measurement for X and Y and update horizontal and vertical
     * orbit.
     */
    public void measureOrbitOnce() {
        String successMessage = "Measure orbit once command was successfully sent.";
        String failureMessage = "Error occured while sending measure orbit once command.";
        executeCommand(Preferences.MEASURE_ORBIT_ONCE_PV, successMessage, failureMessage);
    }

    /**
     * Calls command which starts measuring orbit periodically and perform orbit correction for every
     * measurement.
     */
    public void startCorrectingOrbit() {
        String successMessage = "Start correcting orbit command was successfully sent.";
        String failureMessage = "Error occured while sending start correcting orbit command.";
        executeCommand(Preferences.START_CORRECTING_ORBIT_PV, successMessage, failureMessage);
    }

    /**
     * Calls command which stops measuring orbit, all PVs keep their last value.
     */
    public void stopCorrectingOrbit() {
        String successMessage = "Stop correcting orbit command was successfully sent.";
        String failureMessage = "Error occured while sending stop correcting orbit command.";
        executeCommand(Preferences.STOP_CORRECTING_ORBIT_PV, successMessage, failureMessage);
    }

    /**
     * Calls command which uses the last horizontal and vertical orbit, calculate the corrections and apply
     * them once
     */
    public void correctOrbitOnce() {
        String successMessage = "Correct orbit once command was successfully sent.";
        String failureMessage = "Error occured while sending correct orbit once command.";
        executeCommand(Preferences.CORRECT_ORBIT_ONCE_PV, successMessage, failureMessage);
    }

    /**
     * Exports current horizontal and vertical orbit with weights into the given file.
     *
     * @param file file in which horizontal and vertical orbit with weights will be written
     */
    public void exportCurrentOrbit(File file) {
        writeOrbitInFile(file, Preferences.HORIZONTAL_ORBIT_PV, Preferences.VERTICAL_ORBIT_PV);
    }

    /**
     * Uploads new golden horizontal and vertical orbit with weights from the given file.
     *
     * @param file file in which golden horizontal and vertical orbit with weights are written
     */
    public void uploadGoldenOrbit(File file) {
        readGoldenOrbitFromFile(file);
    }

    /**
     * Downloads current golden horizontal and vertical orbit with weights into the given file.
     *
     * @param file file in which golden horizontal and vertical orbit with weights will be written
     */
    public void downloadGoldenOrbit(File file) {
        writeOrbitInFile(file, Preferences.GOLDEN_HORIZONTAL_ORBIT_PV, Preferences.VERTICAL_ORBIT_WEIGHTS_PV);
    }

    /**
     * Uses current horizontal and vertical orbit values as golden horizontal and vertical orbit.
     */
    public void useCurrent() {
        final PV horizontalOrbit = findPV(Preferences.HORIZONTAL_ORBIT_PV);
        final PV verticalOrbit = findPV(Preferences.VERTICAL_ORBIT_PV);
        final PV goldenHorizontalOrbit = findPV(Preferences.GOLDEN_HORIZONTAL_ORBIT_PV);
        final PV goldenVerticalOrbit = findPV(Preferences.GOLDEN_VERTICAL_ORBIT_PV);

        if (horizontalOrbit != null && goldenHorizontalOrbit != null) {
            String successMessage = "Golden horizontal orbit was successfully updated.";
            String failureMessage = "Error occured while updatind golden horizontal orbit.";
            writeData(goldenHorizontalOrbit, Optional.of(horizontalOrbit.value), successMessage, failureMessage);
        }
        if (verticalOrbit != null && goldenVerticalOrbit != null) {
            String successMessage = "Golden vertical orbit was successfully updated.";
            String failureMessage = "Error occured while updatind golden vertical orbit.";
            writeData(goldenVerticalOrbit, Optional.of(verticalOrbit.value), successMessage, failureMessage);
        }
        throttle.trigger();
    }

    public void downloadResponseMatrix(File file) {
        // TODO
    }

    public void uploadResponseMatrix(File file) {
        // TODO
    }

    public void measure() {
        // TODO
    }

    /**
     * Dispose all PVs.
     */
    public void dispose() {
        synchronized (pvs) {
            // synchronise, because this method can be called from the UI thread by Eclipse, when the editor is closing
            pvs.forEach(e -> e.dispose());
            pvs.clear();
            bpms.clear();
            correctors.clear();
        }
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
        correctionResultsEntries = new ArrayList<>(4);
        correctionResultsEntries.add(new OrbitCorrectionResultsEntry(HORIZONTAL_ORBIT_TABLE_ENTRY));
        correctionResultsEntries.add(new OrbitCorrectionResultsEntry(VERTICAL_ORBIT_TABLE_ENTRY));
        correctionResultsEntries.add(new OrbitCorrectionResultsEntry(GOLDEN_HORIZONTAL_ORBIT_TABLE_ENTRY));
        correctionResultsEntries.add(new OrbitCorrectionResultsEntry(GOLDEN_VERTICAL_ORBIT_TABLE_ENTRY));
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
                pvs.add(new PV(k, reader, writer));
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
        final Optional<PV> pv = pvs.stream().filter(p -> p.pvKey.equals(pvKey)).findFirst();
        if (pv.isPresent()) {
            writeData(pv.get(), Optional.empty(), successMessage, failureMessage);
        }
    }

    /**
     * Method which updates PV values.
     */
    private void update() {
        pvs.forEach(p -> {
            List<Double> values = getWaveformValues(p.value);
            switch (p.pvKey) {
            case Preferences.HORIZONTAL_ORBIT_PV:
                updateOrbit(values, SeriesType.HORIZONTAL_ORBIT);
                break;
            case Preferences.VERTICAL_ORBIT_PV:
                updateOrbit(values, SeriesType.VERTICAL_ORBIT);
                break;
            case Preferences.GOLDEN_HORIZONTAL_ORBIT_PV:
                updateOrbit(values, SeriesType.GOLDEN_HORIZONTAL_ORBIT);
                break;
            case Preferences.GOLDEN_VERTICAL_ORBIT_PV:
                updateOrbit(values, SeriesType.GOLDEN_VERTICAL_ORBIT);
                break;
            case Preferences.HORIZONTAL_CORRECTOR_MRAD_PV:
                if (mradProperty.get()) {
                    updateCorrectors(values, LatticeElementType.HORIZONTAL_CORRECTOR);
                }
                break;
            case Preferences.VERTICAL_CORRECTOR_MRAD_PV:
                if (mradProperty.get()) {
                    updateCorrectors(values, LatticeElementType.VERTICAL_CORRECTOR);
                }
                break;
            case Preferences.HORIZONTAL_CORRECTOR_MA_PV:
                if (!mradProperty.get()) {
                    updateCorrectors(values, LatticeElementType.HORIZONTAL_CORRECTOR);
                }
            case Preferences.VERTICAL_CORRECTOR_MA_PV:
                if (!mradProperty.get()) {
                    updateCorrectors(values, LatticeElementType.VERTICAL_CORRECTOR);
                }
                break;
            case Preferences.HORIZONTAL_ORBIT_STATISTIC_PV:
            case Preferences.VERTICAL_ORBIT_STATISTIC_PV:
            case Preferences.GOLDEN_HORIZONTAL_ORBIT_STATISTIC_PV:
            case Preferences.GOLDEN_VERTICAL_ORBIT_STATISTIC_PV:
                updateOrbitCorrectionResults(p.pvKey, values);
                break;
            case Preferences.OPERATION_STATUS_PV:
                if (p.value instanceof VEnum) {
                    statusProperty.set(((VEnum) p.value).getValue());
                }
            }
        });
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
        Optional<OrbitCorrectionResultsEntry> correctionResultsEntry = correctionResultsEntries.stream()
                .filter(e -> e.nameProperty().get().equals(getEntryName(pvKey))).findFirst();
        if (correctionResultsEntry.isPresent()) {
            if (values.size() >= 5) {
                correctionResultsEntry.get().minProperty().set(values.get(0));
                correctionResultsEntry.get().maxProperty().set(values.get(1));
                correctionResultsEntry.get().avgProperty().set(values.get(2));
                correctionResultsEntry.get().rmsProperty().set(values.get(3));
                correctionResultsEntry.get().stdProperty().set(values.get(4));
            }
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
            public void pvChanged(PVWriterEvent<PV>w) {
                if (w.isWriteSucceeded()) {
                    writeToMessageLog(successMessage);
                } else if (w.isWriteFailed()) {
                    writeToMessageLog(failureMessage);
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
    private void convertedAndWriteData(PV pv, String[] values, String orbitName) {
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
        String failureMessage = "Error occured while updatind " + orbitName + ".";
        writeData(pv, Optional.of(data), successMessage, failureMessage);
    }

    /**
     * Reads golden orbit values with weights from file.
     *
     * @param file file with golden orbit values.
     */
    private void readGoldenOrbitFromFile(File file) {
        final PV xOrbitPV = findPV(Preferences.GOLDEN_HORIZONTAL_ORBIT_PV);
        final PV xWeightsPV = findPV(Preferences.HORIZONTAL_ORBIT_WEIGHTS_PV);
        final PV yOrbitPV = findPV(Preferences.GOLDEN_VERTICAL_ORBIT_PV);
        final PV yWeightsPV = findPV(Preferences.VERTICAL_ORBIT_WEIGHTS_PV);

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
                    convertedAndWriteData(xOrbitPV, values, "Golden Horizontal Orbit");
                } else if (xWeightsPV != null && lineCounter == 2) {
                    convertedAndWriteData(xWeightsPV, values, "Horizontal Orbit Weights");
                } else if (yOrbitPV != null && lineCounter == 3) {
                    convertedAndWriteData(yOrbitPV, values, "Golden Vertical Orbit");
                } else if (xOrbitPV != null && lineCounter == 4) {
                    convertedAndWriteData(yWeightsPV, values, "Vertical OrbitWeights");
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE, "Reading orbit from the selected file fails.", e);
        }
    }

    /**
     * Writes orbit values with weights into the file.
     *
     * @param file file in which orbit values with weights will be written
     * @param xOrbitKey horizontal or golden horizontal orbit PV key
     * @param yOrbitKey vertical or golden vertical orbit PV key
     */
    private void writeOrbitInFile(File file, String xOrbitKey, String yOrbitKey ) {
        final PV xOrbitPV = findPV(xOrbitKey);
        final PV xWeightsPV = findPV(Preferences.HORIZONTAL_ORBIT_WEIGHTS_PV);
        final PV yOrbitPV = findPV(yOrbitKey);
        final PV yWeightsPV = findPV(Preferences.VERTICAL_ORBIT_WEIGHTS_PV);

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
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE, "Writing orbit to the selected file fails.", e);
        }
    }

    /**
     * @param name orbit correction results entry name
     * @return orbit correction results entry name name
     */
    private String getEntryName(String name) {
        switch (name) {
        case Preferences.HORIZONTAL_ORBIT_STATISTIC_PV:
            return HORIZONTAL_ORBIT_TABLE_ENTRY;
        case Preferences.VERTICAL_ORBIT_STATISTIC_PV:
            return VERTICAL_ORBIT_TABLE_ENTRY;
        case Preferences.GOLDEN_HORIZONTAL_ORBIT_STATISTIC_PV:
            return GOLDEN_HORIZONTAL_ORBIT_TABLE_ENTRY;
        case Preferences.GOLDEN_VERTICAL_ORBIT_STATISTIC_PV:
            return GOLDEN_VERTICAL_ORBIT_TABLE_ENTRY;
        default:
            return EMPTY_STRING;
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
        if (value instanceof VNumberArray) {
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
    private void writeToMessageLog(String message) {
        StringBuilder sb = new StringBuilder(messageLogProperty.get());
        if (sb.length() != 0) {
            sb.append('\n');
        }
        sb.append(message);
        messageLogProperty.set(sb.toString());
    }

    /**
     * Finds and returns PV with given PV key in PVs list.
     *
     * @param pvKey PV key
     *
     * @return PV which corresponds to the given PV key.
     */
    private PV findPV(String pvKey) {
        final Optional<PV> pv = pvs.stream().filter(p -> p.pvKey.equals(pvKey)).findFirst();
        if (pv.isPresent()) {
            return pv.get();
        }
        return null;
    }
}