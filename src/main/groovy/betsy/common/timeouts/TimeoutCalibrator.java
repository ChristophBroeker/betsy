package betsy.common.timeouts;

import betsy.Main;
import betsy.bpel.BPELMain;
import betsy.bpel.soapui.SoapUIShutdownHelper;
import betsy.common.tasks.FileTasks;
import betsy.common.timeouts.calibration.CalibrationTimeout;
import betsy.common.timeouts.calibration.CalibrationTimeoutRepository;
import betsy.common.timeouts.timeout.Timeout;
import betsy.common.timeouts.timeout.TimeoutRepository;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Christoph Broeker
 * @version 1.0
 */
public class TimeoutCalibrator {

    private static final Logger LOGGER = Logger.getLogger(TimeoutCalibrator.class);

    public static void main(String[] args) {
        calibrateTimeouts(args);
    }

    private static void calibrateTimeouts(String[] args) {
        LOGGER.info("Calibration is started.");
        //If it's true, SoapUI turns off after first run
        BPELMain.shutdownSoapUiAfterCompletion(false);
        int numberOfDuration = 0;
        boolean allTimeoutsAreCalibrated;
        boolean lastRound = false;
        boolean isCalibrated = false;

        File properties = new File("timeout.properties");
        File csv = new File("calibration_timeouts.csv");
        FileTasks.deleteFile(csv.toPath());


        while (numberOfDuration < 4) {
            //execute betsy
            Main.main(addChangedTestFolderToArgs(args, numberOfDuration));
            if (numberOfDuration > 0) {
                //write all timeouts to csv for traceability
                CalibrationTimeoutRepository.writeToCSV(csv, numberOfDuration++);
                CalibrationTimeoutRepository.clean();
            } else {
                numberOfDuration++;
                CalibrationTimeoutRepository.clean();
            }
        }

        while (!isCalibrated) {
            //execute betsy
            Main.main(addChangedTestFolderToArgs(args, numberOfDuration));
            //get used timeouts
            HashMap<String, CalibrationTimeout> timeouts = CalibrationTimeoutRepository.getAllNonRedundantTimeouts();
            //write all timeouts to csv for traceability
            CalibrationTimeoutRepository.writeToCSV(csv, numberOfDuration++);
            //evaluate the timeouts
            allTimeoutsAreCalibrated = evaluateTimeouts(timeouts);
            //determine the timeouts
            timeouts = determineTimeouts(timeouts, csv);
            //clean the timeoutRepository for the next run
            CalibrationTimeoutRepository.clean();

            if (!allTimeoutsAreCalibrated || !lastRound) {
                if (allTimeoutsAreCalibrated) {
                    //write timeouts to properties
                    TimeoutIOOperations.writeToProperties(properties, new ArrayList<>(timeouts.values()));
                }
                //if all timeouts aren't calibrated and/or this isn't the last round, it isn't finished
                isCalibrated = false;
                //set all values to the repositories
                TimeoutRepository.setAllCalibrationTimeouts(timeouts);
                //if the last round all timeouts were kept, it's the last round
                lastRound = allTimeoutsAreCalibrated;
            } else {
                //if all timeouts are calibrated and this was the last round, it is finished
                isCalibrated = true;
                //shutdown SoapUI
                SoapUIShutdownHelper.shutdownSoapUIForReal();
                LOGGER.info("Calibration is finished.");
            }
            //write all timeouts to the console
            for (CalibrationTimeout timeout : timeouts.values()) {
                LOGGER.info(timeout.getKey() + " " + timeout.getStatus() + " " + timeout.getTimeoutInMs());
            }
        }
    }

    /**
     * The method extended the args with the argument for test folder.
     *
     * @param args             The arguments to extend.
     * @param testFolderNumber The number of the folder (test1, test2...).
     * @return Returns the extended arguments.
     */
    public static String[] addChangedTestFolderToArgs(String args[], int testFolderNumber) {
        Objects.requireNonNull(args, "The arguments can't be null.");
        String[] destination = new String[args.length + 1];
        if (args.length > 0) {
            System.arraycopy(args, 0, destination, 0, 1);
            destination[1] = "-f" + "test/test" + testFolderNumber;
            System.arraycopy(args, 1, destination, 2, args.length - 1);
        } else {
            LOGGER.info("Can't add test folder to args, because the args aren't greater than null.");
        }
        return destination;
    }

    /**
     * This method evaluates the the given timeouts. If one of the timeouts has the status exceeded, the method returns false.
     *
     * @param timeouts The timeouts to evaluate.
     * @return The result of the evaluation
     */
    public static boolean evaluateTimeouts(HashMap<String, CalibrationTimeout> timeouts) {
        Objects.requireNonNull(timeouts, "The timeouts can't be null.");
        boolean allTimeoutsAreCalibrated = true;
        for (CalibrationTimeout timeout : timeouts.values()) {
            if (timeout.getStatus() == CalibrationTimeout.Status.EXCEEDED) {
                allTimeoutsAreCalibrated = false;
            }
        }
        return allTimeoutsAreCalibrated;
    }

    /**
     * This method determines the values of the given timeouts. The calculated value of the timeout consists of
     * the expectation and the standardDeviation based on the values of the csv file.
     *
     * @param timeouts The timeouts, which should be calculated.
     * @param csv      The csv file with the timeouts for calculation.
     * @return Returns the new determined timeouts.
     */
    public static HashMap<String, CalibrationTimeout> determineTimeouts(HashMap<String, CalibrationTimeout> timeouts, File csv) {
        Objects.requireNonNull(timeouts, "The timeouts can't be null.");
        Objects.requireNonNull(csv, "The csv file can't be null.");
        if (timeouts.size() > 0) {
            for (CalibrationTimeout timeout : timeouts.values()) {
                timeout.setValue(calculateTimeout(timeout, 2, csv));
            }
        } else {
            LOGGER.info("The number of the timeouts has to be greater than null to determine the timeouts.");
        }
        return timeouts;
    }

    /**
     * This method calculates the expectation of the given timeouts.
     *
     * @param timeouts The timeouts for calculation of the expectation.
     * @return Returns the calculated expectation.
     */
    public static int calculateExpectation(List<CalibrationTimeout> timeouts) {
        if (timeouts.size() > 0) {
            Objects.requireNonNull(timeouts, "The timeouts can't be null.");
            int expectation = 0;
            for (CalibrationTimeout timeoutValue : timeouts) {
                expectation = expectation + timeoutValue.getMeasuredTime();
            }
            return expectation / timeouts.size();
        } else {
            LOGGER.info("The number of the timeouts has to be greater than null to calculate the expectation.");
            return 0;
        }
    }

    /**
     * This method calculates the variance for the given timeouts.
     *
     * @param timeouts    The timeouts for calculating the variance.
     * @param expectation The expectation of the timeouts.
     * @return Returns the calculated variance.
     */
    public static double calculateVariance(List<CalibrationTimeout> timeouts, int expectation) {
        if (timeouts.size() > 0) {
            Objects.requireNonNull(timeouts, "The timeouts can't be null.");
            double standardVariance = 0;
            for (CalibrationTimeout timeoutValue : timeouts) {
                standardVariance = standardVariance + Math.pow(timeoutValue.getMeasuredTime() - expectation, 2);
            }
            return standardVariance / timeouts.size();
        } else {
            LOGGER.info("The number of the timeouts has to be greater than null to calculate the variance.");
            return 0;
        }
    }

    /**
     * This method calculates the standardDeviation of given
     *
     * @param timeouts    The timeouts for calculating the standardDeviation
     * @param expectation The expectation of the timeouts.
     * @return Returns the calculated standardDeviation.
     */
    public static double standardDeviation(List<CalibrationTimeout> timeouts, int expectation) {
        if (timeouts.size() > 0) {
            Objects.requireNonNull(timeouts, "The timeouts can't be null.");
            return Math.sqrt(calculateVariance(timeouts, expectation));
        } else {
            LOGGER.info("The number of the timeouts has to be greater than null to calculate the standardDeviation.");
            return 0;
        }
    }

    /**
     * This method calculates the timeout based on the timeouts values in the csv. The calculated timeout consists of the
     * expectation and the x-fold of the standardDeviation.
     *
     * @param timeout                     The {@link Timeout}, which should be calculated.
     * @param standardDeviationMultiplier The value indicates, how often the standardDeviation is multiplied.
     * @param csv                         The csv file withe the timeout values.
     * @return returns the calculated timeout value.
     */
    public static int calculateTimeout(Timeout timeout, int standardDeviationMultiplier, File csv) {
        Objects.requireNonNull(timeout, "The timeout can't be null.");
        Objects.requireNonNull(csv, "The csv file can't be null.");
        List<CalibrationTimeout> timeouts = TimeoutIOOperations.readFromCSV(csv).stream().filter(actualTimeout -> actualTimeout.getKey().equals(timeout.getKey())).collect(Collectors.toList());
        int expectation = calculateExpectation(timeouts);
        return expectation + (standardDeviationMultiplier * new Double(standardDeviation(timeouts, expectation)).intValue());
    }
}

