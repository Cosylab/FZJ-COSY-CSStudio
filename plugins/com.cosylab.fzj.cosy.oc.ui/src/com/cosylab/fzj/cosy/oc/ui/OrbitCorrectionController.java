package com.cosylab.fzj.cosy.oc.ui;

import static org.diirt.datasource.ExpressionLanguage.channel;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import org.diirt.datasource.PVManager;
import org.diirt.datasource.PVReader;
import org.diirt.datasource.PVWriter;
import org.diirt.util.array.ListDouble;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VType;

import com.cosylab.fzj.cosy.oc.LatticeElementDataLoader;
import com.cosylab.fzj.cosy.oc.LatticeElementType;
import com.cosylab.fzj.cosy.oc.Preferences;
import com.cosylab.fzj.cosy.oc.ui.model.BPM;
import com.cosylab.fzj.cosy.oc.ui.model.Corrector;
import com.cosylab.fzj.cosy.oc.ui.util.GUIUpdateThrottle;

import javafx.application.Platform;

public class OrbitCorrectionController {

    /** The rate at which the UI is updated */
    public static final long UPDATE_RATE = 500;
    private static Executor UI_EXECUTOR = Platform::runLater;

    private class PV {
        final String pvName;
        final PVReader<VType> reader;
        final PVWriter<Object> writer;
        VType value;

        PV(String pvName, PVReader<VType> reader, PVWriter<Object> writer) {
            this.pvName = pvName;
            this.reader = reader;
            this.writer = writer;
            this.reader.addPVReaderListener(e -> {
                synchronized (OrbitCorrectionController.this) {
                    if (suspend.get() > 0) {
                        return;
                    }
                }
                if (e.isExceptionChanged()) {
                    // TODO log
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

    private final GUIUpdateThrottle throttle = new GUIUpdateThrottle(20, UPDATE_RATE) {
        @Override
        protected void fire() {
            UI_EXECUTOR.execute(() -> {
                if (suspend.get() > 0) {
                    return;
                }
                update();
            });
        }
    };
    private final AtomicInteger suspend = new AtomicInteger(0);

    private Map<String, PV> chartPVs = new HashMap<>();
    private Map<OrbitCorrectionResultsEntry, PV> correctionResultPVs = new HashMap<>();

    private List<BPM> bpms = new ArrayList<>();
    private List<Corrector> correctors = new ArrayList<>();


    public OrbitCorrectionController() throws Exception {
        loadLatticeElements();
        connectPVs();
        start();
    }

    /**
     * Start the gui throttle.
     */
    protected void start() {
        throttle.start();
    }

    public void dispose() {
        synchronized (chartPVs) {
            // synchronise, because this method can be called from the UI thread by Eclipse, when the editor is closing
            chartPVs.values().forEach(e -> e.dispose());
            chartPVs.clear();
            correctionResultPVs.values().forEach(e -> e.dispose());
            correctionResultPVs.clear();
            bpms.clear();
            correctors.clear();
        }
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

    private void connectPVs() {
        connectChartPVs();
        connectOrbitCorrectionResultsPVs();
    }

    private void connectChartPVs() {
        Preferences.getInstance().getChartPVNames().forEach((k, v) -> {
            if (v != null) {
                PVReader<VType> reader = PVManager.read(channel(v, VType.class, VType.class))
                        .maxRate(Duration.ofMillis(100));
                PVWriter<Object> writer = PVManager.write(channel(v)).timeout(Duration.ofMillis(2000)).async();
                chartPVs.put(k, new PV(v, reader, writer));
            }
        });
    }

    private void connectOrbitCorrectionResultsPVs() {
        Preferences.getInstance().getStatisticPVNames().forEach((k,v) -> {
            if (v != null) {
                PVReader<VType> reader = PVManager.read(channel(v, VType.class, VType.class))
                        .maxRate(Duration.ofMillis(100));
                PVWriter<Object> writer = PVManager.write(channel(v)).timeout(Duration.ofMillis(2000)).async();

                String name = resolveEntryName(k);
                correctionResultPVs.put(new OrbitCorrectionResultsEntry(name), new PV(v, reader, writer));
            }
        });
    }

    private String resolveEntryName(String name) {
        switch (name) {
            case Preferences.HORIZONTAL_ORBIT_STATISTIC_PV:
                return "Horizontal Orbit";
            case Preferences.VERTICAL_ORBIT_STATISTIC_PV:
                return "Vertical Orbit";
            case Preferences.GOLDEN_HORIZONTAL_ORBIT_STATISTIC_PV:
                return "Golden Horizontal Orbit";
            case Preferences.GOLDEN_VERTICAL_ORBIT_STATISTIC_PV:
                return "Golden Vertical Orbit";
            default:
                return "";
        }
    }

    public List<OrbitCorrectionResultsEntry> getOrbitCorrectionResults() {
        if (correctionResultPVs != null) {
            return new ArrayList<OrbitCorrectionResultsEntry>(correctionResultPVs.keySet()); // TODO ORDER?
        }
        return new ArrayList<>(5);
    }

    public List<BPM> getBpms() {
        return bpms;
    }

    public List<Corrector> getCorrectors() {
        return correctors;
    }

    public void startMeasuringOrbit() {

    }

    public void stopMeasuringOrbit() {

    }

    public void measureOrbitOnce() {

    }

    public void correctOrbitOnce() {

    }

    public void startOrbitCorrection() {

    }

    public void stopOrbitCorrection() {

    }

    public void exportCurrentOrbit() {

    }

    private void update() {
        updateCharts();
        updateOrbitCorrectionResults();
    }

    private void updateCharts() {
        chartPVs.forEach((k, v) -> {
            List<Double> values = retrieveWaveformValues(v.value);
            switch(k) {
                case Preferences.HORIZONTAL_ORBIT_PV:
                    updateHorizontalOrbit(values);
                    break;
                case Preferences.VERTICAL_ORBIT_PV:
                    updateVerticalOrbit(values);
                    break;
                case Preferences.GOLDEN_HORIZONTAL_ORBIT_PV:
                    updateGoldenHorizontalOrbit(values);
                    break;
                case Preferences.GOLDEN_VERTICAL_ORBIT_PV:
                    updateGoldenVerticalOrbit(values);
                    break;
                case Preferences.HORIZONTAL_CORRECTOR_PV:
                    updateHorizontalCorrectors(values);
                    break;
                case Preferences.VERTICAL_CORRECTOR_PV:
                    updateVerticalCorrectors(values);
                    break;
            }
        });
    }

    private void updateOrbitCorrectionResults() {
        correctionResultPVs.forEach((k, v) -> {
            List<Double> values = retrieveWaveformValues(v.value);
            if (values.size() >= 5) {
                k.minProperty().set(values.get(0));
                k.maxProperty().set(values.get(1));
                k.avgProperty().set(values.get(2));
                k.rmsProperty().set(values.get(3));
                k.stdProperty().set(values.get(4));
            }
        });
    }

    private void updateHorizontalOrbit(List<Double> values) {
        for (int i = 0; (i < values.size()) && (i < bpms.size()); i++) {
            bpms.get(i).horizontalOrbitProperty().set(values.get(i));
        }
    }

    private void updateVerticalOrbit(List<Double> values) {
        for (int i = 0; (i < values.size()) && (i < bpms.size()); i++) {
            bpms.get(i).verticalOrbitProperty().set(values.get(i));
        }
    }

    private void updateGoldenHorizontalOrbit(List<Double> values) {
        for (int i = 0; (i < values.size()) && (i < bpms.size()); i++) {
            bpms.get(i).goldenHorizontalOrbitProperty().set(values.get(i));
        }
    }

    private void updateGoldenVerticalOrbit(List<Double> values) {
        for (int i = 0; (i < values.size()) && (i < bpms.size()); i++) {
            bpms.get(i).goldenVerticalOrbitProperty().set(values.get(i));
        }
    }

    private void updateHorizontalCorrectors(List<Double> values) {
        for (int i = 0; (i < values.size()) && (i < correctors.size()); i++) {
            correctors.get(i).horizontalCorrectionProperty().set(values.get(i));
        }
    }

    private void updateVerticalCorrectors(List<Double> values) {
        for (int i = 0; (i < values.size()) && (i < correctors.size()); i++) {
            correctors.get(i).verticalCorrectionProperty().set(values.get(i));
        }
    }

    private List<Double> retrieveWaveformValues(VType value) {
        if (value instanceof VNumberArray) {
            ListNumber data = ((VNumberArray) value).getData();
            int length = data.size();
            if (data instanceof ListDouble) {
                List<Double> values = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    values.add(data.getDouble(i));
                }
                return values;
            }
        }
        return new ArrayList<>();
    }
}