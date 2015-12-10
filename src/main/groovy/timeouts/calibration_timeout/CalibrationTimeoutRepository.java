package timeouts.calibration_timeout;

import timeouts.timeout.Timeout;
import timeouts.timeout.Timeouts;

import java.util.HashMap;

/**
 * @author Christoph broeker
 * @version 1.0
 */
public class CalibrationTimeoutRepository {

    public static final CalibrationTimeouts CALIBRATION_TIMEOUTS = new CalibrationTimeouts();

    /**
     * This method returns all timeouts as {@link CalibrationTimeout}.
     *
     * @return A {@link HashMap} with the key of {@link CalibrationTimeout} and the {@link CalibrationTimeout}.
     */
    public static HashMap<String, CalibrationTimeout> getAllCalibrationTimeouts() {
        return CALIBRATION_TIMEOUTS.getAllCalibrationTimeouts();
    }

    /**
     * With this method it is possible to add a {@link CalibrationTimeout} to the repository.
     *
     * @param calibrationTimeout The calibrationTime to add to the repository.
     */
    public static void addTimeout(CalibrationTimeout calibrationTimeout) {
        CALIBRATION_TIMEOUTS.addTimeout(calibrationTimeout);
    }

    /**
     * This method sets the values of the {@link Timeout} to the {@link CalibrationTimeoutRepository}, if the {@link Timeout} exists in the {@link Timeouts}.
     *
     * @param calibrationTimeout The {@link Timeout}, which should be set.
     */
    public static void setCalibrationTimeout(CalibrationTimeout calibrationTimeout) {
        CALIBRATION_TIMEOUTS.setTimeoutCalibration(calibrationTimeout);
    }

    /**
     * This method sets the values of the {@link CalibrationTimeout} to the {@link CalibrationTimeoutRepository}, if the {@link CalibrationTimeout} exists in the {@link Timeouts}.
     *
     * @param calibrationTimeouts A {@link HashMap} with the key and the {@link CalibrationTimeout}.
     */
    public static void setAllCalibrationTimeouts(HashMap<String, CalibrationTimeout> calibrationTimeouts) {
        calibrationTimeouts.values().forEach(CalibrationTimeoutRepository::setCalibrationTimeout);
    }

    /**
     * This method writes all given {@link Timeout} to the properties, if the {@link CalibrationTimeout} exists in the {@link Timeouts}.
     */
    public static void writeAllCalibrationTimeoutsToProperties() {
        CALIBRATION_TIMEOUTS.writeAllCalibrationTimeoutsToProperties();
    }

    /**
     * Writes the the values of the {@link Timeout} in the {@link CalibrationTimeoutRepository} to a csv file.
     */
    public static void writeToCSV() {
        CALIBRATION_TIMEOUTS.writeToCSV();
    }

    /**
     *  This method removes all values from the repository.
     */
    public static void clean(){
        CALIBRATION_TIMEOUTS.clean();
    }
}
