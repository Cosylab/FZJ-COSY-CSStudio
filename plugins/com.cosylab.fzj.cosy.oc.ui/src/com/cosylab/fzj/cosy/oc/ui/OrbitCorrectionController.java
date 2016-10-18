package com.cosylab.fzj.cosy.oc.ui;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.diirt.datasource.ExpressionLanguage.channel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.diirt.datasource.PVManager;
import org.diirt.datasource.PVReader;
import org.diirt.datasource.PVWriter;
import org.diirt.datasource.PVWriterEvent;
import org.diirt.datasource.PVWriterListener;
import org.diirt.util.array.ArrayDouble;
import org.diirt.util.array.IteratorNumber;
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
import com.cosylab.fzj.cosy.oc.OrbitCorrectionPlugin;
import com.cosylab.fzj.cosy.oc.Preferences;
import com.cosylab.fzj.cosy.oc.ui.model.BPM;
import com.cosylab.fzj.cosy.oc.ui.model.Corrector;
import com.cosylab.fzj.cosy.oc.ui.model.SeriesType;
import com.cosylab.fzj.cosy.oc.ui.util.GUIUpdateThrottle;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
    private static final Executor UI_EXECUTOR = Platform::runLater;
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
        //all actions on the value are atomic, therefore no need for synchronisation
        volatile VType value;

        PV(PVReader<VType> reader, PVWriter<Object> writer) {
            this.reader = reader;
            this.writer = writer;
            this.reader.addPVReaderListener(e -> {
                if (e.isExceptionChanged()) {
                    writeToLog("DIIRT Connection Error.",true,ofNullable(e.getPvReader().lastException()));
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
            synchronized (this) {
                if (!writer.isClosed()) {
                    writer.close();
                }
            }
        }
    }

    private final BooleanProperty mradProperty = new SimpleBooleanProperty(true);
    private final StringProperty messageLogProperty = new SimpleStringProperty(EMPTY_STRING);
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final Map<String,PV> pvs = new HashMap<>();
    private final Map<String,OrbitCorrectionResultsEntry> correctionResultsEntries = new LinkedHashMap<>(4);
    private final List<BPM> bpms = new ArrayList<>();
    private final List<Corrector> correctors = new ArrayList<>();
    private final LinkedList<String> messages = new LinkedList<>();
    private static final int MAX_MESSAGES = 200;
    private final DateFormat MESSAGE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private final ExecutorService nonUIexecutor = new ThreadPoolExecutor(1,1,0L,TimeUnit.SECONDS,
            new LinkedBlockingQueue<>()) {

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
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
        }
    };
    private final GUIUpdateThrottle throttle = new GUIUpdateThrottle(20,UPDATE_RATE,
            v -> UI_EXECUTOR.execute(() -> update()));

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
        //make sire that the throttle is started after all data has already been read from the config files
        nonUIexecutor.execute(() -> throttle.start());
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
    public Map<String,OrbitCorrectionResultsEntry> getOrbitCorrectionResults() {
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
        executeCommand(Preferences.START_MEASURING_ORBIT_PV,START_ORBIT_MEASURING_CMD_SUCCESS_MSG,
                START_ORBIT_MEASURING_CMD_FAILURE_MSG);
    }

    /**
     * Calls command which stops measuring orbit. Horizontal and vertical orbit keep the value as they have it at this
     * time.
     */
    public void stopMeasuringOrbit() {
        executeCommand(Preferences.STOP_MEASURING_ORBIT_PV,STOP_ORBIT_MEASURING_CMD_SUCCESS_MSG,
                STOP_ORBIT_MEASURING_CMD_FAILURE_MSG);
    }

    /**
     * Calls command which performs one orbit measurement for X and Y and update horizontal and vertical orbit.
     */
    public void measureOrbitOnce() {
        executeCommand(Preferences.MEASURE_ORBIT_ONCE_PV,MEASURE_ORBIT_ONCE_CMD_SUCCESS_MSG,
                MEASURE_ORBIT_ONCE_CMD_FAILURE_MSG);
    }

    /**
     * Calls command which starts measuring orbit periodically and perform orbit correction for every measurement.
     */
    public void startCorrectingOrbit() {
        executeCommand(Preferences.START_CORRECTING_ORBIT_PV,START_CORRECTING_ORBIT_CMD_SUCCESS_MSG,
                START_CORRECTING_ORBIT_CMD_FAILURE_MSG);
    }

    /**
     * Calls command which stops measuring orbit, all PVs keep their last value.
     */
    public void stopCorrectingOrbit() {
        executeCommand(Preferences.STOP_CORRECTING_ORBIT_PV,STOP_CORRECTING_ORBIT_CMD_SUCCESS_MSG,
                STOP_CORRECTING_ORBIT_CMD_FAILURE_MSG);
    }

    /**
     * Calls command which uses the last horizontal and vertical orbit, calculate the corrections and apply them once
     */
    public void correctOrbitOnce() {
        executeCommand(Preferences.CORRECT_ORBIT_ONCE_PV,CORRECT_ORBIT_ONCE_CMD_SUCCESS_MSG,
                CORRECT_ORBIT_ONCE_CMD_FAILURE_MSG);
    }

    /**
     * Exports current horizontal and vertical orbit with weights into the given file.
     *
     * @param file file in which horizontal and vertical orbit with weights will be written
     */
    public void exportCurrentOrbit(File file) {
        writeOrbitInFile(file,Preferences.HORIZONTAL_ORBIT_PV,Preferences.VERTICAL_ORBIT_PV,
                EXPORT_CURRENT_ORBIT_SUCCESS_MSG,EXPORT_CURRENT_ORBIT_FAILURE_MSG);
    }

    /**
     * Uploads new golden horizontal and vertical orbit with weights from the given file.
     *
     * @param file file in which golden horizontal and vertical orbit with weights are written
     */
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
                        ofNullable(pvs.get(Preferences.GOLDEN_HORIZONTAL_ORBIT_PV))
                                .ifPresent(pv -> convertAndWriteData(pv,values,"Golden Horizontal Orbit"));
                    } else if (lineCounter == 2) {
                        ofNullable(pvs.get(Preferences.HORIZONTAL_ORBIT_WEIGHTS_PV))
                                .ifPresent(pv -> convertAndWriteData(pv,values,"Horizontal Orbit Weights"));
                    } else if (lineCounter == 3) {
                        ofNullable(pvs.get(Preferences.GOLDEN_VERTICAL_ORBIT_PV))
                                .ifPresent(pv -> convertAndWriteData(pv,values,"Golden Vertical Orbit"));
                    } else if (lineCounter == 4) {
                        ofNullable(pvs.get(Preferences.VERTICAL_ORBIT_WEIGHTS_PV))
                                .ifPresent(pv -> convertAndWriteData(pv,values,"Vertical Orbit Weights"));
                    } else {
                        break;
                    }
                }
                writeToLog(UPLOAD_GOLDEN_ORBIT_SUCCESS_MSG,false,empty());
            } catch (Exception e) {
                writeToLog(UPLOAD_GOLDEN_ORBIT_FAILURE_MSG,true,of(e));
            }
        });
    }

    /**
     * Downloads current golden horizontal and vertical orbit with weights into the given file.
     *
     * @param file file in which golden horizontal and vertical orbit with weights will be written
     */
    public void downloadGoldenOrbit(File file) {
        writeOrbitInFile(file,Preferences.GOLDEN_HORIZONTAL_ORBIT_PV,Preferences.VERTICAL_ORBIT_WEIGHTS_PV,
                DOWNLOAD_GOLDEN_ORBIT_SUCCESS_MSG,DOWNLOAD_GOLDEN_ORBIT_FAILURE_MSG);
    }

    /**
     * Uses current horizontal and vertical orbit values as golden horizontal and vertical orbit.
     */
    public void useCurrent() {
        if (!throttle.isRunning()) return;
        final PV horizontalOrbit = pvs.get(Preferences.HORIZONTAL_ORBIT_PV);
        final PV verticalOrbit = pvs.get(Preferences.VERTICAL_ORBIT_PV);
        final PV goldenHorizontalOrbit = pvs.get(Preferences.GOLDEN_HORIZONTAL_ORBIT_PV);
        final PV goldenVerticalOrbit = pvs.get(Preferences.GOLDEN_VERTICAL_ORBIT_PV);
        if (horizontalOrbit != null && goldenHorizontalOrbit != null) {
            writeData(goldenHorizontalOrbit,of(horizontalOrbit.value),USE_CURRENT_HORIZONTAL_SUCCESS_MSG,
                    USE_CURRENT_HORIZONTAL_FAILURE_MSG);
        }
        if (verticalOrbit != null && goldenVerticalOrbit != null) {
            writeData(goldenVerticalOrbit,of(verticalOrbit.value),USE_CURRENT_VERTICAL_SUCCESS_MSG,
                    USE_CURRENT_VERTICAL_FAILURE_MSG);
        }
        throttle.trigger();
    }

    /**
     * Downloads response matrix.
     */
    public void downloadResponseMatrix(File file) {
        if (!throttle.isRunning()) return;
        final PV ormPV = pvs.get(Preferences.ORM_PV);
        final PV horizontalOrbitPV = pvs.get(Preferences.HORIZONTAL_ORBIT_PV);
        final PV horizontalCorrectorPV = pvs.get(Preferences.HORIZONTAL_CORRECTOR_MA_PV);
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
                            sb.append('\n');
                        }
                        sb.append(data.getDouble(i)).append(' ');
                    }
                    writer.write(sb.toString());
                }
                writeToLog(DOWNLOAD_ORM_SUCCESS_MSG,false,empty());
            } catch (Exception e) {
                writeToLog(DOWNLOAD_ORM_FAILURE_MSG,true,of(e));
            }
        });
    }

    /**
     * Uploads response matrix.
     */
    public void uploadResponseMatrix(File file) {
        if (!throttle.isRunning()) return;
        final PV ormPV = pvs.get(Preferences.ORM_PV);
        final PV horizontalOrbitPV = pvs.get(Preferences.HORIZONTAL_ORBIT_PV);
        final PV horizontalCorrectorPV = pvs.get(Preferences.HORIZONTAL_CORRECTOR_MA_PV);
        final int n = horizontalOrbitPV != null ? ((VNumberArray)horizontalOrbitPV.value).getData().size() : -1;
        final int m = horizontalCorrectorPV != null ? ((VNumberArray)horizontalCorrectorPV.value).getData().size() : -1;
        if (n == -1 || m == -1 || ormPV == null) {
            return;
        }
        nonUIexecutor.execute(() -> {
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(file.getPath()))) {
                StringBuilder sb = new StringBuilder(n * m * 15);
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
                convertAndWriteData(ormPV,waveform,"Orbit response matrix");
                writeToLog(UPLOAD_ORM_SUCCESS_MSG,false,empty());
            } catch (Exception e) {
                writeToLog(UPLOAD_ORM_FAILURE_MSG,true,of(e));
            }
        });
    }

    /**
     * Measure orbit response matrix.
     */
    public void measure() {
        executeCommand(Preferences.MEASURE_ORM_PV,MEASURE_ORM_CMD_SUCCESS_MSG,MEASURE_ORM_CMD_FAILURE_MSG);
    }

    /**
     * Dispose all PVs.
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
            pvs.values().forEach(v -> v.dispose());
            pvs.clear();
            bpms.clear();
            correctors.clear();
            nonUIexecutor.shutdownNow();
        });
    }

    /**
     * Loads lattice elements from the file.
     */
    private void loadLatticeElements() {
        nonUIexecutor.execute(() -> LatticeElementDataLoader.loadLatticeElements().forEach(e -> {
            if (e.getType() == LatticeElementType.BPM) {
                bpms.add(new BPM(e));
            } else {
                correctors.add(new Corrector(e));
            }
        }));
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
        nonUIexecutor.execute(() -> Preferences.getInstance().getPVNames().forEach((k, v) -> {
            if (v != null) {
                PVReader<VType> reader = PVManager.read(channel(v,VType.class,VType.class))
                        .maxRate(Duration.ofMillis(100));
                PVWriter<Object> writer = PVManager.write(channel(v)).timeout(Duration.ofMillis(2000)).async();
                pvs.put(k,new PV(reader,writer));
            }
        }));
    }

    /**
     * Executes the command. Writes 1 into the PV with the given key. Also writes success message into the message log
     * if value (1) was successfully written or failure message if write failed.
     *
     * @param pvKey PV key
     * @param successMessage success message
     * @param failureMessage
     */
    private void executeCommand(String pvKey, String successMessage, String failureMessage) {
        if (!throttle.isRunning()) return;
        ofNullable(pvs.get(pvKey)).ifPresent(pv -> writeData(pv,empty(),successMessage,failureMessage));
    }

    /**
     * Method which updates PV values.
     */
    private void update() {
        if (!throttle.isRunning()) return;
        ofNullable(pvs.get(Preferences.HORIZONTAL_ORBIT_PV))
                .ifPresent(pv -> updateOrbit(pv.value,SeriesType.HORIZONTAL_ORBIT));
        ofNullable(pvs.get(Preferences.VERTICAL_ORBIT_PV))
                .ifPresent(pv -> updateOrbit(pv.value,SeriesType.VERTICAL_ORBIT));
        ofNullable(pvs.get(Preferences.GOLDEN_HORIZONTAL_ORBIT_PV))
                .ifPresent(pv -> updateOrbit(pv.value,SeriesType.GOLDEN_HORIZONTAL_ORBIT));
        ofNullable(pvs.get(Preferences.GOLDEN_VERTICAL_ORBIT_PV))
                .ifPresent(pv -> updateOrbit(pv.value,SeriesType.GOLDEN_VERTICAL_ORBIT));
        if (mradProperty.get()) {
            ofNullable(pvs.get(Preferences.HORIZONTAL_CORRECTOR_MRAD_PV))
                    .ifPresent(pv -> updateCorrectors(pv.value,LatticeElementType.HORIZONTAL_CORRECTOR));
            ofNullable(pvs.get(Preferences.VERTICAL_CORRECTOR_MRAD_PV))
                    .ifPresent(pv -> updateCorrectors(pv.value,LatticeElementType.VERTICAL_CORRECTOR));
        } else {
            ofNullable(pvs.get(Preferences.HORIZONTAL_CORRECTOR_MA_PV))
                    .ifPresent(pv -> updateCorrectors(pv.value,LatticeElementType.HORIZONTAL_CORRECTOR));
            ofNullable(pvs.get(Preferences.VERTICAL_CORRECTOR_MA_PV))
                    .ifPresent(pv -> updateCorrectors(pv.value,LatticeElementType.VERTICAL_CORRECTOR));
        }
        ofNullable(pvs.get(Preferences.OPERATION_STATUS_PV)).filter(pv -> pv.value instanceof VEnum)
                .ifPresent(pv -> statusProperty.set(((VEnum)pv.value).getValue()));
        ofNullable(pvs.get(Preferences.HORIZONTAL_ORBIT_STATISTIC_PV))
                .ifPresent(pv -> updateOrbitCorrectionResults(pv.value,Preferences.HORIZONTAL_ORBIT_STATISTIC_PV));
        ofNullable(pvs.get(Preferences.VERTICAL_ORBIT_STATISTIC_PV))
                .ifPresent(pv -> updateOrbitCorrectionResults(pv.value,Preferences.VERTICAL_ORBIT_STATISTIC_PV));
        ofNullable(pvs.get(Preferences.GOLDEN_HORIZONTAL_ORBIT_STATISTIC_PV)).ifPresent(
                pv -> updateOrbitCorrectionResults(pv.value,Preferences.GOLDEN_HORIZONTAL_ORBIT_STATISTIC_PV));
        ofNullable(pvs.get(Preferences.GOLDEN_VERTICAL_ORBIT_STATISTIC_PV))
                .ifPresent(pv -> updateOrbitCorrectionResults(pv.value,Preferences.GOLDEN_VERTICAL_ORBIT_STATISTIC_PV));
    }

    /**
     * Updates orbit chart values.
     *
     * @param values new values
     * @param type series type
     */
    private void updateOrbit(final VType value, SeriesType type) {
        if (!(value instanceof VNumberArray)) return;
        final ListNumber va = ((VNumberArray)value).getData();
        if (va.size() == 0) return;
        Function<BPM,DoubleProperty> property;
        switch (type) {
            case HORIZONTAL_ORBIT:
                property = bpm -> bpm.horizontalOrbitProperty();
                break;
            case VERTICAL_ORBIT:
                property = bpm -> bpm.verticalOrbitProperty();
                break;
            case GOLDEN_HORIZONTAL_ORBIT:
                property = bpm -> bpm.goldenHorizontalOrbitProperty();
                break;
            case GOLDEN_VERTICAL_ORBIT:
                property = bpm -> bpm.goldenVerticalOrbitProperty();
                break;
            default:
                return;
        }
        final IteratorNumber it = va.iterator();
        bpms.stream().map(bpm -> property.apply(bpm)).forEach(p -> p.set(it.nextDouble()));
    }

    /**
     * Updates correctors chart values.
     *
     * @param values new correctors values
     * @param type element type
     */
    private void updateCorrectors(final VType value, final LatticeElementType type) {
        if (!(value instanceof VNumberArray)) return;
        final ListNumber va = ((VNumberArray)value).getData();
        if (va.size() == 0) return;
        Function<Corrector,DoubleProperty> property;
        if (type == LatticeElementType.HORIZONTAL_CORRECTOR) {
            property = c -> c.horizontalCorrectionProperty();
        } else if (type == LatticeElementType.VERTICAL_CORRECTOR) {
            property = c -> c.verticalCorrectionProperty();
        } else {
            return;
        }
        final IteratorNumber it = va.iterator();
        correctors.stream().filter(c -> c.getElementData().getType() == type).map(c -> property.apply(c))
                .forEach(p -> p.set(it.nextDouble()));
    }

    /**
     * Updates orbit correction results table entries.
     *
     * @param pvKey the key identifying which orbit the results are for
     * @param values new values
     */
    private void updateOrbitCorrectionResults(VType value, String pvKey) {
        if (!(value instanceof VNumberArray)) return;
        ListNumber va = ((VNumberArray)value).getData();
        if (va.size() < 5) return;
        ofNullable(correctionResultsEntries.get(pvKey)).ifPresent(entry -> {
            entry.minProperty().set(va.getDouble(0));
            entry.maxProperty().set(va.getDouble(1));
            entry.avgProperty().set(va.getDouble(2));
            entry.rmsProperty().set(va.getDouble(3));
            entry.stdProperty().set(va.getDouble(4));
        });
    }

    /**
     * Writes data to the given PV. If data are provided they are written as waveform, otherwise 1 is written to the PV.
     *
     * @param pv pv
     * @param data data to be written, if presented
     * @param successMessage success message
     * @param failureMessage failure message
     */
    private void writeData(PV pv, Optional<VType> data, String successMessage, String failureMessage) {
        nonUIexecutor.execute(() -> {
            PVWriterListener<?> pvListener = new PVWriterListener<PV>() {

                @Override
                public void pvChanged(PVWriterEvent<PV> w) {
                    if (w.isWriteSucceeded()) {
                        writeToLog(successMessage,false,empty());
                    } else if (w.isWriteFailed()) {
                        if (w.isExceptionChanged()) {
                            writeToLog(failureMessage,true,of(w.getPvWriter().lastWriteException()));
                        } else {
                            writeToLog(failureMessage,true,empty());
                        }
                    }
                    w.getPvWriter().removePVWriterListener(this);
                }
            };
            synchronized (pv) {
                pv.writer.addPVWriterListener(pvListener);
                if (data.isPresent()) {
                    pv.writer.write(((VNumberArray)data.get()).getData());
                } else {
                    pv.writer.write(1);
                }
            }
        });
    }

    /**
     * Converts data from string array and writes it into PV.
     *
     * @param pv pv
     * @param values new values
     * @param orbitName the orbit name (used for logging only)
     */
    private void convertAndWriteData(PV pv, String[] values, String orbitName) {
        double[] array = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = Double.parseDouble(values[i].trim()); // throw exception if value is not double
        }
        Alarm alarm = ValueFactory.newAlarm(AlarmSeverity.NONE,"USER DEFINED");
        Time time = ValueFactory.timeNow();
        ListNumber list = new ArrayDouble(array);
        VNumberArray data = ValueFactory.newVNumberArray(list,alarm,time,(VNumberArray)pv.value);
        String successMessage = orbitName + " was successfully updated.";
        String failureMessage = "Error occured while updating " + orbitName + ".";
        writeData(pv,of(data),successMessage,failureMessage);
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
    private void writeOrbitInFile(File file, String xOrbitKey, String yOrbitKey, String successMessage,
            String failureMessage) {
        if (!throttle.isRunning()) return;
        final PV xOrbitPV = pvs.get(xOrbitKey);
        final PV xWeightsPV = pvs.get(Preferences.HORIZONTAL_ORBIT_WEIGHTS_PV);
        final PV yOrbitPV = pvs.get(yOrbitKey);
        final PV yWeightsPV = pvs.get(Preferences.VERTICAL_ORBIT_WEIGHTS_PV);
        nonUIexecutor.execute(() -> {
            String xOrbit = xOrbitPV != null ? getStringValue(xOrbitPV.value) : EMPTY_STRING;
            String xWeights = xWeightsPV != null ? getStringValue(xWeightsPV.value) : EMPTY_STRING;
            String yOrbit = yOrbitPV != null ? getStringValue(yOrbitPV.value) : EMPTY_STRING;
            String yWeights = yWeightsPV != null ? getStringValue(yWeightsPV.value) : EMPTY_STRING;
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getPath()),StandardCharsets.UTF_8)) {
                writer.write(xOrbit);
                writer.write('\n');
                writer.write(xWeights);
                writer.write('\n');
                writer.write(yOrbit);
                writer.write('\n');
                writer.write(yWeights);
                writeToLog(successMessage,false,empty());
            } catch (Exception e) {
                writeToLog(failureMessage,true,of(e));
            }
        });
    }

    /**
     * Transforms the value of the given {@link VType} to a human readable string.
     *
     * @param type the data to transform
     * @return string representation of the data.
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
     */
    private void writeToLog(String message, boolean error, Optional<Exception> e) {
        if (e.isPresent()) {
            OrbitCorrectionPlugin.LOGGER.log(Level.SEVERE,message,e.get());
        } else if (error) {
            OrbitCorrectionPlugin.LOGGER.log(Level.SEVERE,message);
        } else {
            OrbitCorrectionPlugin.LOGGER.log(Level.FINE,message);
        }
        final StringBuilder sb;
        synchronized (messages) {
            messages.add(String.format("%s: %s",MESSAGE_FORMAT.format(new Date()),message));
            while (messages.size() > MAX_MESSAGES) {
                messages.removeFirst();
            }
            sb = new StringBuilder(messages.size() * 100);
            messages.forEach(s -> sb.append(s).append('\n'));
        }
        UI_EXECUTOR.execute(() -> messageLogProperty.set(sb.toString()));
    }
}
