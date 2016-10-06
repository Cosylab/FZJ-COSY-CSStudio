package com.cosylab.fzj.cosy.oc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.osgi.service.datalocation.Location;

public class Preferences {

    private static final String PLUGIN_ID = "com.cosylab.fzj.cosy.oc";

    private static final String BPMS_FILE = "bpms_file";
    private static final String CORRECTORS_FILE = "correctors_file";
    private static final String PVS_FILE = "pvs_file";

    public static final String HORIZONTAL_ORBIT_PV = "horizontal_orbit_pv";
    public static final String VERTICAL_ORBIT_PV = "vertical_orbit_pv";
    public static final String GOLDEN_HORIZONTAL_ORBIT_PV = "golden_horizontal_orbit_pv";
    public static final String GOLDEN_VERTICAL_ORBIT_PV = "golden_vertical_orbit_pv";
    public static final String HORIZONTAL_CORRECTORS_PV = "horizontal_correctors_pv";
    public static final String VERTICAL_CORRECTORS_PV = "vertical_correctors_pv";
    public static final String HORIZONTAL_ORBIT_STATISTIC_PV = "horizontal_orbit_statistic_pv";
    public static final String VERTICAL_ORBIT_STATISTIC_PV = "vertical_orbit_statistic_pv";
    public static final String GOLDEN_HORIZONTAL_ORBIT_STATISTIC_PV = "golden_horizontal_orbit_statistic_pv";
    public static final String GOLDEN_VERTICAL_ORBIT_STATISTIC_PV = "golden_vertical_orbit_statistic_pv";
    public static final String START_MEASURING_ORBIT_PV = "start_measuring_orbit_pv";
    public static final String STOP_MEASURING_ORBIT_PV = "stop_measuring_orbit_pv";
    public static final String MEASURE_ORBIT_ONCE_PV = "measure_orbit_once_pv";
    public static final String CORRECT_ORBIT_ONCE_PV = "correct_orbit_once_pv";
    public static final String START_ORBIT_CORRECTION_PV = "start_orbit_correction_pv";
    public static final String STOP_ORBIT_CORRECTION_PV = "stop_orbit_correction_pv";

    private URL bpmsFile;
    private URL correctorsFile;
    private URL pvsFile;

    private Properties properties;

    private static Preferences instance;

    private Preferences() {
        super();
    }

    /**
     * @return the singleton instance of this class
     */
    public synchronized static Preferences getInstance() {
        if (instance == null) {
            instance = new Preferences();
        }
        return instance;
    }

    public URL getBpmsFile() {
        if (bpmsFile == null) {
            bpmsFile = getWorkspaceFile(getBpmsFilename());
        }
        return bpmsFile;
    }

    public String getBpmsFilename() {
        return getString(BPMS_FILE, "bpms.twiss", false);
    }

    public URL getCorrectorsFile() {
        if (correctorsFile == null) {
            correctorsFile = getWorkspaceFile(getCorrectorsFilename());
        }
        return correctorsFile;
    }

    public String getCorrectorsFilename() {
        return getString(CORRECTORS_FILE, "correctors.twiss", false);
    }

    public URL getPVsFile() {
        if (pvsFile == null) {
            pvsFile = getWorkspaceFile(getPVsFilename());
        }
        return pvsFile;
    }

    public String getPVsFilename() {
        return getString(PVS_FILE, "pvs.properties", false);
    }

    /**
     * @return the horizontal orbit PV name.
     */
    public String getHorizontalOrbitPVName() {
        try {
            return getString(HORIZONTAL_ORBIT_PV, null, true);
        } catch (Exception e) {
            e.printStackTrace();  // TODO log this
            return null;
        }
    }

    /**
     * @return the vertical orbit PV name.
     */
    public String getVerticalOrbitPVName() {
        try {
            return getString(VERTICAL_ORBIT_PV, null, true);
        } catch (Exception e) {
            e.printStackTrace();  // TODO log this
            return null;
        }
    }

    /**
     * @return the golden horizontal orbit PV name.
     */
    public String getGoldenHorizontalOrbitPVName() {
        try {
            return getString(GOLDEN_HORIZONTAL_ORBIT_PV, null, true);
        } catch (Exception e) {
            e.printStackTrace();  // TODO log this
            return null;
        }
    }

    /**
     * @return the golden vertical orbit PV name.
     */
    public String getGoldenVerticalOrbitPVName() {
        try {
            return getString(GOLDEN_VERTICAL_ORBIT_PV, null, true);
        } catch (Exception e) {
            e.printStackTrace();  // TODO log this
            return null;
        }
    }

    public String getHorizontalCorrectorsPVName() {
        try {
            return getString(HORIZONTAL_CORRECTORS_PV, null, true);
        } catch (Exception e) {
            e.printStackTrace();  // TODO log this
            return null;
        }
    }

    public String getVerticalCorrectorsPVName() {
        try {
            return getString(VERTICAL_CORRECTORS_PV, null, true);
        } catch (Exception e) {
            e.printStackTrace();  // TODO log this
            return null;
        }
    }

    public String getHorizontalOrbitStatisticPVName() {
        try {
            return getString(HORIZONTAL_ORBIT_STATISTIC_PV, null ,true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getVerticalOrbitStatisticPVName() {
        try {
            return getString(VERTICAL_ORBIT_STATISTIC_PV, null ,true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getGoldenHorizontalOrbitStatisticPVName() {
        try {
            return getString(GOLDEN_HORIZONTAL_ORBIT_STATISTIC_PV, null ,true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getGoldenVerticalOrbitStatisticPVName() {
        try {
            return getString(GOLDEN_VERTICAL_ORBIT_STATISTIC_PV, null ,true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, String> getChartPVNames() {
        Map<String, String> pvs = new HashMap<>();
        pvs.put(HORIZONTAL_ORBIT_PV, getHorizontalOrbitPVName());
        pvs.put(VERTICAL_ORBIT_PV, getVerticalOrbitPVName());
        pvs.put(GOLDEN_HORIZONTAL_ORBIT_PV, getGoldenHorizontalOrbitPVName());
        pvs.put(GOLDEN_VERTICAL_ORBIT_PV, getGoldenVerticalOrbitPVName());
        pvs.put(HORIZONTAL_CORRECTORS_PV, getHorizontalCorrectorsPVName());
        pvs.put(VERTICAL_CORRECTORS_PV, getVerticalCorrectorsPVName());
        return pvs;
    }

    public Map<String, String> getStatisticPVNames() {
        Map<String, String> pvs = new HashMap<>();
        pvs.put(HORIZONTAL_ORBIT_STATISTIC_PV, getHorizontalOrbitStatisticPVName());
        pvs.put(VERTICAL_ORBIT_STATISTIC_PV, getVerticalOrbitStatisticPVName());
        pvs.put(GOLDEN_HORIZONTAL_ORBIT_STATISTIC_PV, getGoldenHorizontalOrbitStatisticPVName());
        pvs.put(GOLDEN_VERTICAL_ORBIT_STATISTIC_PV, getGoldenVerticalOrbitStatisticPVName());
        return pvs;
    }

    private String getString(final String name, final String defaultValue, boolean pvsFile) {
        if (pvsFile) {
            if (properties == null) {
                try {
                    properties = new Properties();
                    InputStream stream = getPVsFile().openStream();
                    properties.load(stream);
                    stream.close();
                } catch (IOException e) {
                    //
                }
            }
            return Preferences.getInstance().properties.getProperty(name, defaultValue);
        } else {
            final IPreferencesService service = Platform.getPreferencesService();
            if (service == null) return defaultValue;

            return service.getString(PLUGIN_ID, name, defaultValue, null).trim();
        }
    }

    /**
     * Retrieves a file url located in the workspace.
     *
     * @param fileName the file name to open, relative to workspace root
     * @return the file url
     * @throws PreferencesException if the file could not be loaded
     */
    public static URL getWorkspaceFile(String fileName) throws PreferencesException {
        Location loc = Platform.getInstanceLocation();
        File file = new File(new File(loc.getURL().getFile()), fileName);
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new PreferencesException("Could not load file " + fileName + ".", e);
        }
    }
}