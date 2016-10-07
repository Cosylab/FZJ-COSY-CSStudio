package com.cosylab.fzj.cosy.oc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.osgi.service.datalocation.Location;

/**
 * <code>Preferences</code> represents the preferences for the Orbit Correction (OC) plugin.
 * The file names are loaded from the preferences.ini file. Files should exists in the workspace folder.
 * The PV names are loaded from the properties file which should existis in the workspace folder.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public final class Preferences {

    private static final String BPMS_FILE = "bpms_file";
    private static final String CORRECTORS_FILE = "correctors_file";
    private static final String PVS_FILE = "pvs_file";

    /** Horizontal orbit PV key. */
    public static final String HORIZONTAL_ORBIT_PV = "horizontal_orbit_pv";
    /** Vertical orbit PV key. */
    public static final String VERTICAL_ORBIT_PV = "vertical_orbit_pv";
    /** Golden horizontal orbit PV key. */
    public static final String GOLDEN_HORIZONTAL_ORBIT_PV = "golden_horizontal_orbit_pv";
    /** Golden vertical orbit PV key. */
    public static final String GOLDEN_VERTICAL_ORBIT_PV = "golden_vertical_orbit_pv";
    /** Horizontal correctors PV key. */
    public static final String HORIZONTAL_CORRECTOR_PV = "horizontal_corrector_pv";
    /** Vertical correctors PV key. */
    public static final String VERTICAL_CORRECTOR_PV = "vertical_corrector_pv";
    /** Horizontal orbit statistic PV key. */
    public static final String HORIZONTAL_ORBIT_STATISTIC_PV = "horizontal_orbit_statistic_pv";
    /** Vertical orbit statistic PV key. */
    public static final String VERTICAL_ORBIT_STATISTIC_PV = "vertical_orbit_statistic_pv";
    /** Golden horizontal orbit statistic PV key. */
    public static final String GOLDEN_HORIZONTAL_ORBIT_STATISTIC_PV = "golden_horizontal_orbit_statistic_pv";
    /** Golden vertical orbit statistic PV key. */
    public static final String GOLDEN_VERTICAL_ORBIT_STATISTIC_PV = "golden_vertical_orbit_statistic_pv";
    /** Start measuring orbit PV key. */
    public static final String START_MEASURING_ORBIT_PV = "start_measuring_orbit_pv";
    /** Stop measuring orbit PV key. */
    public static final String STOP_MEASURING_ORBIT_PV = "stop_measuring_orbit_pv";
    /** Measure orbit once PV key. */
    public static final String MEASURE_ORBIT_ONCE_PV = "measure_orbit_once_pv";
    /** Correct orbit once PV key. */
    public static final String CORRECT_ORBIT_ONCE_PV = "correct_orbit_once_pv";
    /** Start orbit correction PV key. */
    public static final String START_ORBIT_CORRECTION_PV = "start_orbit_correction_pv";
    /** Stop orbit correction PV key. */
    public static final String STOP_ORBIT_CORRECTION_PV = "stop_orbit_correction_pv";

    private URL bpmsFile;
    private URL correctorsFile;
    private URL pvsFile;

    private Properties properties;

    private static Preferences instance;

    private Preferences() {
    }

    /**
     * @return the singleton instance of the class.
     */
    public synchronized static Preferences getInstance() {
        if (instance == null) {
            instance = new Preferences();
        }
        return instance;
    }

    /**
     * @return the bpms file url.
     */
    public URL getBpmsFile() {
        if (bpmsFile == null) {
            try {
                bpmsFile = getWorkspaceFile(getBpmsFilename());
            } catch (Exception e) {
                OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                        "Could not load the bpms file.", e);
                return null;
            }
        }
        return bpmsFile;
    }

    /**
     * @return the bpms filename.
     */
    public String getBpmsFilename() {
        try {
            return getString(BPMS_FILE, "bpms.twiss", false);
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the bpms file name.", e);
            return null;
        }
    }

    /**
     * @return the correctors file url.
     */
    public URL getCorrectorsFile() {
        if (correctorsFile == null) {
            try {
                correctorsFile = getWorkspaceFile(getCorrectorsFilename());
            } catch (Exception e) {
                OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                        "Could not load the correctors file.", e);
                return null;
            }
        }
        return correctorsFile;
    }

    /**
     * @return the correctors filename.
     */
    public String getCorrectorsFilename() {
        try {
            return getString(CORRECTORS_FILE, "correctors.twiss", false);
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the correctors file name.", e);
            return null;
        }
    }

    /**
     * @return the pvs file url.
     */
    public URL getPVsFile() {
        if (pvsFile == null) {
            try {
                pvsFile = getWorkspaceFile(getPVsFilename());
            } catch (Exception e) {
                OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                        "Could not load the pvs file.", e);
                return null;
            }
        }
        return pvsFile;
    }

    /**
     * @return the pvs filename.
     */
    public String getPVsFilename() {
        try {
            return getString(PVS_FILE, "pvs.properties", false);
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the pvs properties file name.", e);
            return null;
        }
    }

    /**
     * @return the horizontal orbit PV name.
     */
    public String getHorizontalOrbitPVName() {
        try {
            return getString(HORIZONTAL_ORBIT_PV, null, true);
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the horizontal orbit PV name.", e);
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
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the vertical orbit PV name.", e);
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
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the golden horizontal orbit PV name.", e);
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
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the golden vertical orbit PV name.", e);
            return null;
        }
    }

    /**
     * @return the horizontal corrector PV name.
     */
    public String getHorizontalCorrectorPVName() {
        try {
            return getString(HORIZONTAL_CORRECTOR_PV, null, true);
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the horizontal corrector PV name.", e);
            return null;
        }
    }

    /**
     * @return the vertical corrector PV name.
     */
    public String getVerticalCorrectorPVName() {
        try {
            return getString(VERTICAL_CORRECTOR_PV, null, true);
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the vertical corrector PV name.", e);
            return null;
        }
    }

    /**
     * @return the horizontal orbit statistic PV name.
     */
    public String getHorizontalOrbitStatisticPVName() {
        try {
            return getString(HORIZONTAL_ORBIT_STATISTIC_PV, null ,true);
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the horizontal orbit statistic PV name.", e);
            return null;
        }
    }

    /**
     * @return the vertical orbit statistic PV name.
     */
    public String getVerticalOrbitStatisticPVName() {
        try {
            return getString(VERTICAL_ORBIT_STATISTIC_PV, null ,true);
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the vertical orbit statistic PV name.", e);
            return null;
        }
    }

    /**
     * @return the golden horizontal orbit statistic PV name.
     */
    public String getGoldenHorizontalOrbitStatisticPVName() {
        try {
            return getString(GOLDEN_HORIZONTAL_ORBIT_STATISTIC_PV, null ,true);
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the golden horizontal orbit statistic PV name.", e);
            return null;
        }
    }

    /**
     * @return the golden vertical orbit statistic PV name.
     */
    public String getGoldenVerticalOrbitStatisticPVName() {
        try {
            return getString(GOLDEN_VERTICAL_ORBIT_STATISTIC_PV, null ,true);
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the golden vertical orbit statistic PV name.", e);
            return null;
        }
    }

    /**
     * @return the start measuring orbit PV name.
     */
    public String getStartMeasuringOrbitPVName() {
        try {
            return getString(START_MEASURING_ORBIT_PV, null ,true);
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the start measuring orbit PV name.", e);
            return null;
        }
    }

    /**
     * @return the stop measuring orbit PV name.
     */
    public String getStopMeasuringOrbitPVName() {
        try {
            return getString(STOP_MEASURING_ORBIT_PV, null ,true);
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the stop measuring orbit PV name.", e);
            return null;
        }
    }

    /**
     * @return the measure orbit once PV name.
     */
    public String getMeasureOrbitOncePVName() {
        try {
            return getString(MEASURE_ORBIT_ONCE_PV, null ,true);
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the measure orbit once PV name.", e);
            return null;
        }
    }

    /**
     * @return the correct orbit once PV name.
     */
    public String getCorrectOrbitOncePVName() {
        try {
            return getString(CORRECT_ORBIT_ONCE_PV, null ,true);
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the correct orbit once PV name.", e);
            return null;
        }
    }

    /**
     * @return the start orbit correction PV name.
     */
    public String getStartOrbitCorrectionPVName() {
        try {
            return getString(START_ORBIT_CORRECTION_PV, null ,true);
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the start orbit correction PV name.", e);
            return null;
        }
    }

    /**
     * @return the stop orbit correction PV name.
     */
    public String getStopOrbitCorrectionPVName() {
        try {
            return getString(STOP_ORBIT_CORRECTION_PV, null ,true);
        } catch (Exception e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the stop orbit correction PV name.", e);
            return null;
        }
    }

    /**
     * @return the map with the PV names which values are show in the charts.
     */
    public Map<String, String> getChartPVNames() {
        Map<String, String> pvs = new HashMap<>();
        pvs.put(HORIZONTAL_ORBIT_PV, getHorizontalOrbitPVName());
        pvs.put(VERTICAL_ORBIT_PV, getVerticalOrbitPVName());
        pvs.put(GOLDEN_HORIZONTAL_ORBIT_PV, getGoldenHorizontalOrbitPVName());
        pvs.put(GOLDEN_VERTICAL_ORBIT_PV, getGoldenVerticalOrbitPVName());
        pvs.put(HORIZONTAL_CORRECTOR_PV, getHorizontalCorrectorPVName());
        pvs.put(VERTICAL_CORRECTOR_PV, getVerticalCorrectorPVName());
        return pvs;
    }

    /**
     * @return the map with the PV names which values are show in the table.
     */
    public Map<String, String> getStatisticPVNames() {
        Map<String, String> pvs = new HashMap<>();
        pvs.put(HORIZONTAL_ORBIT_STATISTIC_PV, getHorizontalOrbitStatisticPVName());
        pvs.put(VERTICAL_ORBIT_STATISTIC_PV, getVerticalOrbitStatisticPVName());
        pvs.put(GOLDEN_HORIZONTAL_ORBIT_STATISTIC_PV, getGoldenHorizontalOrbitStatisticPVName());
        pvs.put(GOLDEN_VERTICAL_ORBIT_STATISTIC_PV, getGoldenVerticalOrbitStatisticPVName());
        return pvs;
    }

    /**
     * Read a preference from the Eclipse preferences service or from the pvs properties file. If a preference is not
     * defined, a default value is returned.
     *
     * @param name the name of the preference
     * @param defaultValue the default value to use if the preference is not defined
     * @param mainFile if true the property value will be read from the pvs properties file, if false the Eclipse
     *                 preference service is used
     *
     * @return the preference.
     */
    private String getString(final String name, final String defaultValue, boolean pvsFile) {
        if (pvsFile) {
            if (properties == null) {
                try {
                    properties = new Properties();
                    InputStream stream = getPVsFile().openStream();
                    properties.load(stream);
                    stream.close();
                } catch (IOException e) {
                    OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                            "Could not load the PVs properties file.", e);
                }
            }
            return Preferences.getInstance().properties.getProperty(name, defaultValue);
        } else {
            final IPreferencesService service = Platform.getPreferencesService();
            if (service == null) return defaultValue;

            return service.getString(OrbitCorrectionService.PLUGIN_ID, name, defaultValue, null).trim();
        }
    }

    /**
     * Retrieves a file url located in the workspace.
     *
     * @param fileName the file name to open, relative to workspace root
     *
     * @return the file url.
     * @throws PreferencesException if the file could not be loaded
     */
    private static URL getWorkspaceFile(String fileName) throws PreferencesException {
        Location loc = Platform.getInstanceLocation();
        File file = new File(new File(loc.getURL().getFile()), fileName);
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new PreferencesException("Could not load file " + fileName + ".", e);
        }
    }
}