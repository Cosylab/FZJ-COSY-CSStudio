package com.cosylab.fzj.cosy.oc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.osgi.service.datalocation.Location;

/**
 * <code>Preferences</code> represents the preferences for the Orbit Correction (OC) plugin. The file names are loaded
 * from the preferences.ini file. Files should exists in the workspace folder. The PV names are loaded from the
 * properties file which should exist in the workspace folder.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public final class Preferences {

    private static final String BPMS_FILE = "bpms_file";
    private static final String CORRECTORS_FILE = "correctors_file";
    private static final String PVS_FILE = "pvs_file";
    private static final String MEASURE_ORM_COMMAND = "measureORM.command";
    private static final String LOAD_SETTINGS_FROM_FILES = "loadLatticeFromFiles";
    private static final String OC_DEVICE_MACRO = "ocDeviceMacro";
    private static final String BPM_OPI = "bpm_opi";
    private static final String CORRECTOR_OPI = "corrector_opi";
    /** Horizontal BPM names PV provides the names of all horizontal BPMS */
    public static final String PV_HORIZONTAL_BPM_NAMES = "horizontal_bpm_names";
    /** Horizontal BPM positions provides the locations of all horizontal BPMs along z axis */
    public static final String PV_HORIZONTAL_BPM_POSITIONS = "horizontal_bpm_positions";
    /** Horizontal BPM enabled provides 1 or 0 states whether individual horizontal BPM is enabled or not */
    public static final String PV_HORIZONTAL_BPM_ENABLED = "horizontal_bpm_enabled";
    /** Vertical BPM names PV provides the names of all vertical BPMS */
    public static final String PV_VERTICAL_BPM_NAMES = "vertical_bpm_names";
    /** Vertical BPM positions provides the locations of all vertical BPMs along z axis */
    public static final String PV_VERTICAL_BPM_POSITIONS = "vertical_bpm_positions";
    /** Vertical BPM enabled provides 1 or 0 states whether individual vertical BPM is enabled or not */
    public static final String PV_VERTICAL_BPM_ENABLED = "vertical_bpm_enabled";
    /** Horizontal corrector names PV provides the names of all horizontal correctors */
    public static final String PV_HORIZONTAL_CORRECTOR_NAMES = "horizontal_corrector_names";
    /** Horizontal corrector positions PV provides the positions of all horizontal correctors along the z axis */
    public static final String PV_HORIZONTAL_CORRECTOR_POSITIONS = "horizontal_corrector_positions";
    /** Horizontal corrector enable PV provides 1 or 0 states whether individual corrector is enabled or not */
    public static final String PV_HORIZONTAL_CORRECTOR_ENABLED = "horizontal_corrector_enabled";
    /** Vertical corrector names PV provides the names of all vertical correctors */
    public static final String PV_VERTICAL_CORRECTOR_NAMES = "vertical_corrector_names";
    /** Vertical corrector positions PV provides the positions of all vertical correctors along the z axis */
    public static final String PV_VERTICAL_CORRECTOR_POSITIONS = "vertical_corrector_positions";
    /** Vertical corrector enable PV provides 1 or 0 states whether individual corrector is enabled or not */
    public static final String PV_VERTICAL_CORRECTOR_ENABLED = "vertical_corrector_enabled";
    /** Horizontal orbit PV provides the horizontal orbit position */
    public static final String PV_HORIZONTAL_ORBIT = "horizontal_orbit";
    /** Vertical orbit PV provides the vertical orbit position */
    public static final String PV_VERTICAL_ORBIT = "vertical_orbit";
    /** Golden horizontal orbit provides the golden horizontal orbit position */
    public static final String PV_GOLDEN_HORIZONTAL_ORBIT = "golden_horizontal_orbit";
    /** Golden vertical orbit provides the golden vertical orbit position */
    public static final String PV_GOLDEN_VERTICAL_ORBIT = "golden_vertical_orbit";
    /** Horizontal correctors (mrad) provides the last horizontal correctors kick in milli radians */
    public static final String PV_HORIZONTAL_CORRECTOR_MRAD = "horizontal_corrector_mrad";
    /** Vertical correctors (mrad) provides the last vertical correctors kick in milli radians */
    public static final String PV_VERTICAL_CORRECTOR_MRAD = "vertical_corrector_mrad";
    /** Horizontal correctors (mA) provides the last horizontal correctors kick in milli ampers */
    public static final String PV_HORIZONTAL_CORRECTOR_MA = "horizontal_corrector_ma";
    /** Vertical correctors (mA) provides the last vertical correctors kick in milli ampers */
    public static final String PV_VERTICAL_CORRECTOR_MA = "vertical_corrector_ma";
    /** Horizontal orbit statistic provides the statistical parameters of the current horizontal orbit */
    public static final String PV_HORIZONTAL_ORBIT_STATISTIC = "horizontal_orbit_statistic";
    /** Vertical orbit statistic provides the statistical parameters of the current vertical orbit */
    public static final String PV_VERTICAL_ORBIT_STATISTIC = "vertical_orbit_statistic";
    /** Golden horizontal orbit statistic provides the statistical parameters of the current horizontal golden orbit. */
    public static final String PV_GOLDEN_HORIZONTAL_ORBIT_STATISTIC = "golden_horizontal_orbit_statistic";
    /** Golden vertical orbit statistic provides the statistical parameters of the current vertical golden orbit. */
    public static final String PV_GOLDEN_VERTICAL_ORBIT_STATISTIC = "golden_vertical_orbit_statistic";
    /** Horizontal orbit weights provides the weights used in calculation of the horizontal orbit corrections */
    public static final String PV_HORIZONTAL_ORBIT_WEIGHTS = "horizontal_orbit_weights";
    /** Vertical orbit weights PV provides the weights used in calculation of the vertical orbit corrections */
    public static final String PV_VERTICAL_ORBIT_WEIGHTS = "vertical_orbit_weights";
    /** Orbit response matrix provides the pv with the orbit response matrix in a single array */
    @Deprecated
    public static final String PV_ORM = "orm";
    /** Operation status provides the current status of the orbit corrections ioc. */
    public static final String PV_OPERATION_STATUS = "operation_status";
    /** Start measuring orbit stars measuring orbit continuously */
    public static final String PV_START_MEASURING_ORBIT = "start_measuring_orbit";
    /** Stop measuring orbit stops continuous orbit measurement */
    public static final String PV_STOP_MEASURING_ORBIT = "stop_measuring_orbit";
    /** Measure orbit once measures orbit once and stops */
    public static final String PV_MEASURE_ORBIT_ONCE = "measure_orbit_once";
    /** Start orbit correction starts continuous orbit correction */
    public static final String PV_START_CORRECTING_ORBIT = "start_correcting_orbit";
    /** Stop orbit correction stops continuous orbit correction */
    public static final String PV_STOP_CORRECTING_ORBIT = "stop_correcting_orbit";
    /** Correct orbit once performs orbit correction once and stop */
    public static final String PV_CORRECT_ORBIT_ONCE = "correct_orbit_once";
    /** SVD cutoff value for horizontal orbit correction */
    public static final String PV_HORIZONTAL_CUTOFF = "horizontal_orbit_cutoff";
    /** SVD cutoff value for vertical orbit correction */
    public static final String PV_VERTICAL_CUTOFF = "vertical_orbit_cutoff";
    /** Factor to multiply the values with when applying correction */
    public static final String PV_CORRECTION_FRACTION = "correction_factor";
    private Properties properties;
    private static Preferences instance;

    private Preferences() {}

    /**
     * Returns the singleton instance of this class.
     *
     * @return singleton instance of the class
     */
    public synchronized static Preferences getInstance() {
        if (instance == null) {
            instance = new Preferences();
        }
        return instance;
    }

    /**
     * Returns the shell command used to measure the orbit response matrix.
     *
     * @return the shell command
     */
    public Optional<String> getMeasureORMCommand() {
        return getSetting(MEASURE_ORM_COMMAND,"Could not load the measure command.");
    }

    /**
     * Returns the name of the engineering screen for bpms.
     *
     * @return the bpms opi file name if it exits
     */
    public Optional<String> getBPMOPIFile() {
        return getSetting(BPM_OPI,"Could not load the BPM engineering opi file name.");
    }

    /**
     * Returns the name of the engineering screen for correctors.
     *
     * @return the correctors opi file name if it exits
     */
    public Optional<String> getCorrectorOPIFile() {
        return getSetting(CORRECTOR_OPI,"Could not load the corrector engineering opi file name.");
    }

    private Optional<String> getSetting(String setting, String message) {
        try {
            String s = getString(setting,"",false);
            return s == null || s.trim().isEmpty() ? Optional.empty() : Optional.of(s);
        } catch (Exception e) {
            OrbitCorrectionPlugin.LOGGER.log(Level.SEVERE,message,e);
            return Optional.empty();
        }
    }

    /**
     * Returns true if the lattice settings are read from the file or false if they are read from the PVs.
     *
     * @return true if lattice should be read from the file or false if from the PVs
     */
    public boolean isLoadLatticeFromFiles() {
        try {
            String s = getString(LOAD_SETTINGS_FROM_FILES,"false",false);
            return Boolean.valueOf(s);
        } catch (Exception e) {
            OrbitCorrectionPlugin.LOGGER.log(Level.SEVERE,"Could not load setings plugin settings.",e);
            return false;
        }
    }

    private String getFilename(String key, String defaultValue) {
        try {
            return getString(key,defaultValue,false);
        } catch (Exception e) {
            OrbitCorrectionPlugin.LOGGER.log(Level.SEVERE,"Could not load the pvs properties file name.",e);
            return null;
        }
    }

    /**
     * The url to the file which contains the list of all BPMs and their positions in the lattic.
     *
     * @return bpms file url
     * @deprecated read info from the PVs instead
     */
    @Deprecated
    public URL getBpmsFile() {
        try {
            return getWorkspaceFile(getFilename(BPMS_FILE,"bpms.twiss"));
        } catch (Exception e) {
            OrbitCorrectionPlugin.LOGGER.log(Level.SEVERE,"Could not load the bpms file.",e);
            return null;
        }
    }

    /**
     * Returns the URL to the file that contains the entire lattice (correctors names and positions).
     *
     * @return correctors file url
     * @deprecated read info from the PVs instead
     */
    @Deprecated
    public URL getCorrectorsFile() {
        try {
            return getWorkspaceFile(getFilename(CORRECTORS_FILE,"correctors.twiss"));
        } catch (Exception e) {
            OrbitCorrectionPlugin.LOGGER.log(Level.SEVERE,"Could not load the correctors file.",e);
            return null;
        }
    }

    /**
     * Returns the URL to the file that lists all pvs.
     *
     * @return pvs file url
     */
    private URL getPVsFile() {
        try {
            URL pvsFile = getWorkspaceFile(getFilename(PVS_FILE,"pvs.properties"));
            File file = new File(pvsFile.getFile());
            if (!file.exists()) {
                pvsFile = Platform.getBundle(OrbitCorrectionPlugin.PLUGIN_ID).getEntry("pvs.properties");
            }
            return pvsFile;
        } catch (Exception e) {
            OrbitCorrectionPlugin.LOGGER.log(Level.SEVERE,"Could not load the pvs file.",e);
            return null;
        }
    }

    /**
     * Returns the name of PV stored under the given key.
     *
     * @param key pv key for which the actual PV name is requested
     * @return the name of the pv for the given key
     */
    private String getPVName(String pvKey) {
        try {
            return expandDeviceMacro(getString(pvKey,null,true));
        } catch (Exception e) {
            OrbitCorrectionPlugin.LOGGER.log(Level.SEVERE,String.format("Could not load the PV name for %s.",pvKey),e);
            return null;
        }
    }

    private static Pattern NO_MACRO_PATTERN = Pattern.compile("$(DEVICE):",Pattern.LITERAL);
    private static Pattern MACRO_PATTERN = Pattern.compile("$(DEVICE)",Pattern.LITERAL);

    private String expandDeviceMacro(String pvName) {
        if (pvName == null) {
            return null;
        }
        String macro = "";
        try {
            macro = getString(OC_DEVICE_MACRO,"",false);
        } catch (Exception e) {
            OrbitCorrectionPlugin.LOGGER.log(Level.WARNING,"Could not load the device macro.",e);
        }
        if (macro.isEmpty()) {
            return NO_MACRO_PATTERN.matcher(pvName).replaceAll("");
        } else {
            return MACRO_PATTERN.matcher(pvName).replaceAll(Matcher.quoteReplacement(macro));
        }
    }

    /**
     * Returns a map of all pvs that are related to the lattice.
     *
     * @return map of lattice related PVs
     */
    public Map<String,String> getLatticePVNames() {
        Map<String,String> pvs = new HashMap<>();
        pvs.put(PV_HORIZONTAL_BPM_NAMES,getPVName(PV_HORIZONTAL_BPM_NAMES));
        pvs.put(PV_HORIZONTAL_BPM_POSITIONS,getPVName(PV_HORIZONTAL_BPM_POSITIONS));
        pvs.put(PV_VERTICAL_BPM_NAMES,getPVName(PV_VERTICAL_BPM_NAMES));
        pvs.put(PV_VERTICAL_BPM_POSITIONS,getPVName(PV_VERTICAL_BPM_POSITIONS));
        pvs.put(PV_HORIZONTAL_CORRECTOR_NAMES,getPVName(PV_HORIZONTAL_CORRECTOR_NAMES));
        pvs.put(PV_HORIZONTAL_CORRECTOR_POSITIONS,getPVName(PV_HORIZONTAL_CORRECTOR_POSITIONS));
        pvs.put(PV_VERTICAL_CORRECTOR_NAMES,getPVName(PV_VERTICAL_CORRECTOR_NAMES));
        pvs.put(PV_VERTICAL_CORRECTOR_POSITIONS,getPVName(PV_VERTICAL_CORRECTOR_POSITIONS));
        return pvs;
    }

    /**
     * Returns a map of all monitored pv names used by the orbit correction application.
     *
     * @return map with the PV keys and names
     */
    public Map<String,String> getPVNames() {
        Map<String,String> pvs = new HashMap<>();
        pvs.put(PV_HORIZONTAL_ORBIT,getPVName(PV_HORIZONTAL_ORBIT));
        pvs.put(PV_VERTICAL_ORBIT,getPVName(PV_VERTICAL_ORBIT));
        pvs.put(PV_GOLDEN_HORIZONTAL_ORBIT,getPVName(PV_GOLDEN_HORIZONTAL_ORBIT));
        pvs.put(PV_GOLDEN_VERTICAL_ORBIT,getPVName(PV_GOLDEN_VERTICAL_ORBIT));
        pvs.put(PV_HORIZONTAL_CORRECTOR_MRAD,getPVName(PV_HORIZONTAL_CORRECTOR_MRAD));
        pvs.put(PV_VERTICAL_CORRECTOR_MRAD,getPVName(PV_VERTICAL_CORRECTOR_MRAD));
        pvs.put(PV_HORIZONTAL_CORRECTOR_MA,getPVName(PV_HORIZONTAL_CORRECTOR_MA));
        pvs.put(PV_VERTICAL_CORRECTOR_MA,getPVName(PV_VERTICAL_CORRECTOR_MA));
        pvs.put(PV_OPERATION_STATUS,getPVName(PV_OPERATION_STATUS));
        pvs.put(PV_HORIZONTAL_ORBIT_STATISTIC,getPVName(PV_HORIZONTAL_ORBIT_STATISTIC));
        pvs.put(PV_VERTICAL_ORBIT_STATISTIC,getPVName(PV_VERTICAL_ORBIT_STATISTIC));
        pvs.put(PV_GOLDEN_HORIZONTAL_ORBIT_STATISTIC,getPVName(PV_GOLDEN_HORIZONTAL_ORBIT_STATISTIC));
        pvs.put(PV_GOLDEN_VERTICAL_ORBIT_STATISTIC,getPVName(PV_GOLDEN_VERTICAL_ORBIT_STATISTIC));
        pvs.put(PV_HORIZONTAL_ORBIT_WEIGHTS,getPVName(PV_HORIZONTAL_ORBIT_WEIGHTS));
        pvs.put(PV_VERTICAL_ORBIT_WEIGHTS,getPVName(PV_VERTICAL_ORBIT_WEIGHTS));
        pvs.put(PV_ORM,getPVName(PV_ORM));
        pvs.put(PV_START_MEASURING_ORBIT,getPVName(PV_START_MEASURING_ORBIT));
        pvs.put(PV_STOP_MEASURING_ORBIT,getPVName(PV_STOP_MEASURING_ORBIT));
        pvs.put(PV_MEASURE_ORBIT_ONCE,getPVName(PV_MEASURE_ORBIT_ONCE));
        pvs.put(PV_START_CORRECTING_ORBIT,getPVName(PV_START_CORRECTING_ORBIT));
        pvs.put(PV_STOP_CORRECTING_ORBIT,getPVName(PV_STOP_CORRECTING_ORBIT));
        pvs.put(PV_CORRECT_ORBIT_ONCE,getPVName(PV_CORRECT_ORBIT_ONCE));
        pvs.put(PV_HORIZONTAL_CUTOFF,getPVName(PV_HORIZONTAL_CUTOFF));
        pvs.put(PV_VERTICAL_CUTOFF,getPVName(PV_VERTICAL_CUTOFF));
        pvs.put(PV_CORRECTION_FRACTION,getPVName(PV_CORRECTION_FRACTION));
        pvs.put(PV_HORIZONTAL_BPM_ENABLED,getPVName(PV_HORIZONTAL_BPM_ENABLED));
        pvs.put(PV_VERTICAL_BPM_ENABLED,getPVName(PV_VERTICAL_BPM_ENABLED));
        pvs.put(PV_HORIZONTAL_CORRECTOR_ENABLED,getPVName(PV_HORIZONTAL_CORRECTOR_ENABLED));
        pvs.put(PV_VERTICAL_CORRECTOR_ENABLED,getPVName(PV_VERTICAL_CORRECTOR_ENABLED));
        return pvs;
    }

    /**
     * Read a preference from the Eclipse preferences service or from the pvs properties file. If a preference is not
     * defined, the default value is returned.
     *
     * @param name the name of the preference
     * @param defaultValue the default value to use if the preference is not defined
     * @param mainFile if true the property value will be read from the pvs properties file, if false the Eclipse
     *        preference service is used
     * @return preference setting
     */
    private String getString(final String name, final String defaultValue, boolean pvsFile) {
        if (pvsFile) {
            if (properties == null) {
                properties = new Properties();
                try (InputStream stream = getPVsFile().openStream()) {
                    properties.load(stream);
                } catch (IOException e) {
                    OrbitCorrectionPlugin.LOGGER.log(Level.SEVERE,"Could not load the PVs properties file.",e);
                }
            }
            return Preferences.getInstance().properties.getProperty(name,defaultValue);
        } else {
            final IPreferencesService service = Platform.getPreferencesService();
            if (service == null) return defaultValue;
            return service.getString(OrbitCorrectionPlugin.PLUGIN_ID,name,defaultValue,null).trim();
        }
    }

    /**
     * Retrieves a file url located in the workspace.
     *
     * @param fileName the file name to open, relative to workspace root
     * @return the file url.
     * @throws PreferencesException if the file could not be loaded
     */
    private static URL getWorkspaceFile(String fileName) throws PreferencesException {
        Location loc = Platform.getInstanceLocation();
        File file = new File(new File(loc.getURL().getFile()),fileName);
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new PreferencesException("Could not load file " + fileName + ".",e);
        }
    }
}
