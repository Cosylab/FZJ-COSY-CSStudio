package com.cosylab.fzj.cosy.oc.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.diirt.datasource.PV;

import com.cosylab.fzj.cosy.oc.DataLoader;
import com.cosylab.fzj.cosy.oc.LatticeElement;
import com.cosylab.fzj.cosy.oc.LatticeElementType;

public class OrbitCorrectionController {

//    private class PV {
//        final String pvName;
//        final PVReader<VType> reader;
//        final PVWriter<Object> writer;
//        VType value;
//
//        PV(String pvName, PVReader<VType> reader, PVWriter<Object> writer) {
//            this.pvName = pvName;
//            this.reader = reader;
//            this.writer = writer;
//            this.reader.addPVReaderListener(e -> {
//                synchronized (OrbitCorrectionController.this) {
//                    if (suspend.get() > 0) {
//                        return;
//                    }
//                }
//                if (e.isExceptionChanged()) {
////                    SaveRestoreService.LOGGER.log(Level.WARNING, "DIIRT Connection Error.",
////                        e.getPvReader().lastException());
//                }
//                value = e.getPvReader().isConnected() ? e.getPvReader().getValue() : null;
//
//            });
//            this.value = reader.getValue();
//
//        }
//
//        void dispose() {
//            if (!reader.isClosed()) {
//                reader.close();
//            }
//            if (!writer.isClosed()) {
//                writer.close();
//            }
//        }
//    }

    private final AtomicInteger suspend = new AtomicInteger(0);

    private Map<String, LatticeElement> latticeElements = new HashMap<>();
    private Map<String, PV> pvs = new HashMap<>();
    private List<LatticeElementChartValues> latticeElementChartValues;

    public OrbitCorrectionController() {
        latticeElementChartValues = new ArrayList<>();
        DataLoader dataLoader = new DataLoader();
        List<LatticeElement> elements = dataLoader.loadLatticeElements();
        elements.forEach(el -> {
            String name = el.getName();

//            el.getPVs().forEach((k, v) -> {
//                PVReader<VType> reader = PVManager.read(channel(v, VType.class, VType.class))
//                        .maxRate(Duration.ofMillis(10000));
//                PVWriter<Object> writer = PVManager.write(channel(v)).timeout(Duration.ofMillis(1000)).async();
//                PV pv = new PV(v, reader, writer);
//
//                System.out.println(pv.value);
//                pvs.put(k, pv);
//
//            });

            Random rand = new Random();

            // create random data TODO: only for testing
            LatticeElementChartValues lecv = new LatticeElementChartValues();
            lecv.getElementType().set(el.getType());
            lecv.getPositionValue().set(el.getPosition());
            if (el.getType() == LatticeElementType.BPM) {
                lecv.getGoldenHorizontalOrbitValue().set((double) rand.nextInt((200 + 200) + 1) - 200);
                lecv.getGoldenVerticalOrbitValue().set((double) rand.nextInt((200 + 200) + 1) - 200);
                lecv.getHorizontalOrbitValue().set((double) rand.nextInt((200 + 200) + 1) - 200);
                lecv.getVerticalOrbitValue().set((double) rand.nextInt((200 + 200) + 1) - 200);
            }
            if (el.getType() == LatticeElementType.HORIZONTAL_CORRECTOR) {
                lecv.getHorizontalCorrectionValue().set((double) rand.nextInt((200 + 200) + 1) - 200);
            } else if (el.getType() == LatticeElementType.VERTICAL_CORRECTOR) {
                lecv.getVerticalCorrectionValue().set((double) rand.nextInt((200 + 200) + 1) - 200);
            }
            latticeElementChartValues.add(lecv);

//            latticeElements.put(name, el);
        });
    }

    public List<LatticeElementChartValues> getLatticeElementChartValues() {
        return latticeElementChartValues;
    }
}