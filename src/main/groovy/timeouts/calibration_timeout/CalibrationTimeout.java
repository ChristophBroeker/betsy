package timeouts.calibration_timeout;

import timeouts.TimeoutCalibrator;
import timeouts.timeout.Timeout;

/**
 * @author Christoph Broeker
 *
 * @version 1.0
 */
public class CalibrationTimeout extends Timeout {

    private Status status = Status.KEPT;
    private long timestamp = System.currentTimeMillis();

    /**
     * This {@link CalibrationTimeout} represents a {@link Timeout} for the calibration_timeout to optimize the test duration of betsy.
     *
     * @param engineOrProcess  The {@link betsy.common.analytics.model.Engine} or the processgroup, where the {@link Timeout} is located.
     * @param stepOrProcess    The method of the engine or the {@link Process}, where the {@link Timeout} is located.
     * @param description      The description of the {@link Timeout}.
     * @param value            The value of the {@link Timeout} in milliseconds.
     * @param timeToRepetition The time to wait till repetition, if the {@link Timeout} is exceeded.
     */
    public CalibrationTimeout(String engineOrProcess, String stepOrProcess, String description, int value, int timeToRepetition) {
        super(engineOrProcess, stepOrProcess, description, value, timeToRepetition);
    }

    /**
     * This {@link CalibrationTimeout} represents a {@link Timeout} for the calibration_timeout to optimize the test duration of betsy.
     *
     * @param engineOrProcess  The {@link betsy.common.analytics.model.Engine} or the processgroup, where the {@link Timeout} is located.
     * @param stepOrProcess    The method of the engine or the {@link Process}, where the {@link Timeout} is located.
     * @param value            The value of the {@link Timeout} in milliseconds.
     * @param timeToRepetition The time to wait till repetition, if the {@link Timeout} is exceeded.
     */
    public CalibrationTimeout(String engineOrProcess, String stepOrProcess, int value, int timeToRepetition) {
        super(engineOrProcess, stepOrProcess, value, timeToRepetition);
    }

    /**
     * This {@link CalibrationTimeout} represents a {@link Timeout} for the calibration_timeout to optimize the test duration of betsy.
     *
     * @param engineOrProcess  The {@link betsy.common.analytics.model.Engine} or the processgroup, where the {@link Timeout} is located.
     * @param stepOrProcess    The method of the engine or the {@link Process}, where the {@link Timeout} is located.
     * @param description      The description of the {@link Timeout}.
     * @param value            The value of the {@link Timeout} in milliseconds.
     */
    public CalibrationTimeout(String engineOrProcess, String stepOrProcess, String description, int value) {
        super(engineOrProcess, stepOrProcess, description, value);
    }


    /**
     * This {@link CalibrationTimeout} represents a {@link Timeout} for the calibration_timeout to optimize the test duration of betsy.
     *
     * @param engineOrProcess  The {@link betsy.common.analytics.model.Engine} or the processgroup, where the {@link Timeout} is located.
     * @param stepOrProcess    The method of the engine or the {@link Process}, where the {@link Timeout} is located.
     * @param value            The value of the {@link Timeout} in milliseconds.
     */
    public CalibrationTimeout(String engineOrProcess, String stepOrProcess, int value) {
        super(engineOrProcess, stepOrProcess, value);
    }

    /**
     * This {@link CalibrationTimeout} represents a {@link Timeout} for the calibration_timeout to optimize the test duration of betsy.
     *
     * @param timeout The basic {@link Timeout} to create a {@link CalibrationTimeout}.
     */
    public CalibrationTimeout(Timeout timeout) {
        super(timeout.getEngineOrProcessGroup(), timeout.getStepOrProcess(), timeout.getDescription(), timeout.getTimeoutInMs(), timeout.getTimeToRepetitionInMs());
    }

    /**
     *
     * @return Returns the actual {@link Status} of the {@link CalibrationTimeout}.
     */
    public Status getStatus() {
        return status;
    }

    /**
     *
     * @param status The {@link Status} of the {@link CalibrationTimeout}.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return Returns the key composed of the key of the {@link Timeout} and the timestamp.
     */
    public String getCalibrationTimeoutKey(){
        StringBuilder key = new StringBuilder(getKey()).append('.').append(timestamp);
        return key.toString();
    }

    /**
     *
     * @return Returns the timestamp of the creation of {@link CalibrationTimeout}
     */
    public long getTimestamp(){
        return timestamp;
    }

    /**
     * The actual {@link Status} of the {@link CalibrationTimeout} during the calibration_timeout via the {@link TimeoutCalibrator}.
     */
    public enum Status {
        KEPT, EXCEEDED, CALIBRATED
    }
}
