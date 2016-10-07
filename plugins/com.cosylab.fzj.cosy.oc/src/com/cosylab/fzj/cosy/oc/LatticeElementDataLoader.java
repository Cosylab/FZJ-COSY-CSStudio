package com.cosylab.fzj.cosy.oc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <code>LatticeElementDataLoader</code> is data loader which loads all lattice elements from the given file.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class LatticeElementDataLoader {

    private static final int NAME_INDEX = 0;
    private static final int POSITION_INDEX = 1;
    private static final int CORRECTOR_ORIENTATION_INDEX = 3;
    private static final String DATA_DELIMITER = " ";

    /**
     * Loads and returns list of all lattice elements.
     *
     * @return loaded list of all lattice elements.
     */
    public List<LatticeElementData> loadLatticeElements() {
        List<LatticeElementData> elements = new ArrayList<>();
        try {
            URL bpmsFile = Preferences.getInstance().getBpmsFile();
            URL correctorsFile = Preferences.getInstance().getCorrectorsFile();
            if (bpmsFile != null) {
                elements.addAll(getBpms(getFileContent(bpmsFile)));
            }
            if (correctorsFile != null) {
                elements.addAll(getCorrectors(getFileContent(correctorsFile)));
            }
        } catch (IOException e) {
            OrbitCorrectionService.LOGGER.log(Level.SEVERE,
                    "Could not load the lattice elements.", e);
        }
        return elements;
    }

    /**
     * Creates and returns list of all bpms retrieved from bpms file.
     *
     * @param fileContent content of the bpms file
     *
     * @return list of all bpms.
     */
    private List<LatticeElementData> getBpms(List<String> fileContent) {
        List<LatticeElementData> bpms = new ArrayList<>();
        fileContent.forEach(line -> {
            String[] splittedLine = line.split(DATA_DELIMITER);
            if (splittedLine.length >= 2) {
                try {
                    String name = splittedLine[NAME_INDEX];
                    double position = Double.parseDouble(splittedLine[POSITION_INDEX]);
                    if (name != null && !name.isEmpty()) {
                        bpms.add(new LatticeElementData(name, position, LatticeElementType.BPM));
                    }
                } catch(NumberFormatException e) {
                    // ignore the exception, the element is not added into the list
                }
            }
        });
        return bpms;
    }

    /**
     * Creates and returns list of all correctors retrieved from correctors file.
     *
     * @param fileContent content of the correctors file
     *
     * @return list of all correctors.
     */
    private List<LatticeElementData> getCorrectors(List<String> fileContent) {
        List<LatticeElementData> correctors = new ArrayList<>();
        fileContent.forEach(line -> {
            String[] splittedLine = line.split(DATA_DELIMITER);
            if (splittedLine.length >= 4) {
                try {
                    String name = splittedLine[NAME_INDEX];
                    double position = Double.parseDouble(splittedLine[POSITION_INDEX]);
                    String orientation = splittedLine[CORRECTOR_ORIENTATION_INDEX];
                    LatticeElementType type = LatticeElementType.getElementType(orientation);
                    if (name != null && !name.isEmpty() && type != null) {
                        correctors.add(new LatticeElementData(name, position, type));
                    }
                } catch (NumberFormatException e) {
                    // ignore the exception, the element is not added into the list
                }
            }
        });
        return correctors;
    }

    /**
     * Reads file and returns the list of trimmed lines.
     *
     * @param filePath path of the file
     *
     * @return the list of trimmed lines.
     * @throws IOException if exception while reading the file occurs.
     */
    private List<String> getFileContent(URL filePath) throws IOException {
        try (Stream<String> fileStream = Files.lines(Paths.get(new File(filePath.getFile()).getPath()))) {
            Pattern matchSpaces = Pattern.compile("\\s+");
            Pattern matchQuotes = Pattern.compile("\"");
            return fileStream
                    .filter(l -> isElementLine(l))
                    .map(l -> matchQuotes.matcher(matchSpaces.matcher(l).replaceAll(DATA_DELIMITER)).replaceAll("").trim())
                    .collect(Collectors.toList());
        }
    }

    /**
     * Checks if the given string defines lattice element or not.
     *
     * @param line string to check
     * @return <code>true</code> if string defines lattice element, otherwise <code>false</code>.
     */
    private boolean isElementLine(String line) {
        return line != null && !line.isEmpty() && line.charAt(0) != '@' && line.charAt(0) != '*' && line.charAt(0) != '$';
    }
}