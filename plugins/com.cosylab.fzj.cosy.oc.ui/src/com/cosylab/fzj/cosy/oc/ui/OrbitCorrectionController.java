package com.cosylab.fzj.cosy.oc.ui;

import static org.diirt.datasource.ExpressionLanguage.channel;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.logging.Level;

import org.diirt.datasource.PVManager;
import org.diirt.datasource.PVReader;
import org.diirt.datasource.PVWriter;
import org.diirt.datasource.PVWriterListener;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.VEnum;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VType;

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

public class OrbitCorrectionController {

    /** The rate at which the UI is updated */
    public static final long UPDATE_RATE = 500;
    private static Executor UI_EXECUTOR = Platform::runLater;

    private static final String HORIZONTAL_ORBIT_TABLE_ENTRY = "Horizontal Orbit";
    private static final String VERTICAL_ORBIT_TABLE_ENTRY = "Vertical Orbit";
    private static final String GOLDEN_HORIZONTAL_ORBIT_TABLE_ENTRY = "Golden Horizontal Orbit";
    private static final String GOLDEN_VERTICAL_ORBIT_TABLE_ENTRY = "Golden Vertical Orbit";

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
    private final StringProperty messageLogProperty = new SimpleStringProperty("");
    private final StringProperty statusProperty = new SimpleStringProperty();

    private final GUIUpdateThrottle throttle = new GUIUpdateThrottle(20, UPDATE_RATE) {
        @Override
        protected void fire() {
            UI_EXECUTOR.execute(() -> {
                update();
            });
        }
    };
    private List<PV> pvs = new ArrayList<>();
    private List<OrbitCorrectionResultsEntry> correctionResultsEntries = new ArrayList<>();
    private List<BPM> bpms = new ArrayList<>();
    private List<Corrector> correctors = new ArrayList<>();

    public OrbitCorrectionController() throws Exception {

        loadLatticeElements();
        createCorrectionResultsEntries();
        connectPVs();
        start();

        mradProperty.addListener(c -> {
            UI_EXECUTOR.execute(() -> {
                update();
            });
        });
    }

    public void startMeasuringOrbit() {
        String successMessage = "Start orbit measuring command was successfully sent.";
        String failureMessage = "Error occured while sending start orbit measuring command.";
        executeCommand(Preferences.START_MEASURING_ORBIT_PV, successMessage, failureMessage);
    }

    public void stopMeasuringOrbit() {
        String successMessage = "Stop orbit measuring command was successfully sent.";
        String failureMessage = "Error occured while sending stop orbit measuring command.";
        executeCommand(Preferences.STOP_MEASURING_ORBIT_PV, successMessage, failureMessage);
    }

    public void measureOrbitOnce() {
        String successMessage = "Measure orbit once command was successfully sent.";
        String failureMessage = "Error occured while sending measure orbit once command.";
        executeCommand(Preferences.MEASURE_ORBIT_ONCE_PV, successMessage, failureMessage);
    }

    public void startCorrectingOrbit() {
        String successMessage = "Start correcting orbit command was successfully sent.";
        String failureMessage = "Error occured while sending start correcting orbit command.";
        executeCommand(Preferences.START_CORRECTING_ORBIT_PV, successMessage, failureMessage);
    }

    public void stopCorrectingOrbit() {
        String successMessage = "Stop correcting orbit command was successfully sent.";
        String failureMessage = "Error occured while sending stop correcting orbit command.";
        executeCommand(Preferences.STOP_CORRECTING_ORBIT_PV, successMessage, failureMessage);
    }

    public void correctOrbitOnce() {
        String successMessage = "Correct orbit once command was successfully sent.";
        String failureMessage = "Error occured while sending correct orbit once command.";
        executeCommand(Preferences.CORRECT_ORBIT_ONCE_PV, successMessage, failureMessage);
    }

    public void exportCurrentOrbit() {
        // TODO implementation
    }

    public List<OrbitCorrectionResultsEntry> getOrbitCorrectionResults() {
        return correctionResultsEntries;
    }

    public List<BPM> getBpms() {
        return bpms;
    }

    public List<Corrector> getCorrectors() {
        return correctors;
    }

    public StringProperty messageLogProperty() {
        return messageLogProperty;
    }

    public BooleanProperty mradProperty() {
        return mradProperty;
    }

    public StringProperty statusProperty() {
        return statusProperty;
    }

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
     * Start the gui throttle.
     */
    protected void start() {
        throttle.start();
    }

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

    private void createCorrectionResultsEntries() {
        correctionResultsEntries = new ArrayList<>(4);
        correctionResultsEntries.add(new OrbitCorrectionResultsEntry(HORIZONTAL_ORBIT_TABLE_ENTRY));
        correctionResultsEntries.add(new OrbitCorrectionResultsEntry(VERTICAL_ORBIT_TABLE_ENTRY));
        correctionResultsEntries.add(new OrbitCorrectionResultsEntry(GOLDEN_HORIZONTAL_ORBIT_TABLE_ENTRY));
        correctionResultsEntries.add(new OrbitCorrectionResultsEntry(GOLDEN_VERTICAL_ORBIT_TABLE_ENTRY));
    }

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

    private void update() {
        pvs.forEach(p -> {
            List<Double> values = retrieveWaveformValues(p.value);
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
                    VEnum enumE = (VEnum) p.value;
                    statusProperty.set(enumE.getValue());
                }
            }
        });
    }

    private void executeCommand(String pvKey, String successMessage, String failureMessage) {
        Optional<PV> pv = pvs.stream().filter(p -> p.pvKey.equals(pvKey)).findFirst();
        if (pv.isPresent()) {
            PVWriterListener<?> pvListener = w -> {
                StringBuilder sb = new StringBuilder(messageLogProperty.get()).append('\n');
                if (w.isWriteSucceeded()) {
                    sb.append(successMessage);
                } else if (w.isWriteFailed()) {
                    sb.append(failureMessage);
                }
                messageLogProperty.set(sb.toString());
            };
            pv.get().writer.addPVWriterListener(pvListener);
            pv.get().writer.write(1);
        }
    }

    private void updateOrbitCorrectionResults(String pvKey, List<Double> values) {
        Optional<OrbitCorrectionResultsEntry> correctionResultsEntry = correctionResultsEntries.stream()
                .filter(e -> e.nameProperty().get().equals(resolveEntryName(pvKey))).findFirst();
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

    private List<Double> retrieveWaveformValues(VType value) {
        if (value instanceof VNumberArray) {
            ListNumber data = ((VNumberArray) value).getData();
            int length = data.size();
            List<Double> values = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                values.add(data.getDouble(i));
            }
            return values;
        }
        return new ArrayList<>();
    }

    private String resolveEntryName(String name) {
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
            return "";
        }
    }
}