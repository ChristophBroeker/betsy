package betsy.common.tasks;

import org.apache.log4j.Logger;
import betsy.common.timeouts.timeout.Timeout;
import betsy.common.timeouts.TimeoutException;
import betsy.common.timeouts.calibration.CalibrationTimeout;
import betsy.common.timeouts.calibration.CalibrationTimeoutRepository;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

public class WaitTasks {
    /**
     * Sleeps/waits for a specific amount of milliseconds.
     *
     * @param milliseconds the duration to sleep/wait
     */
    public static void sleep(final int milliseconds) {
        if (milliseconds <= 0) {
            LOGGER.info("Did not sleep because value is " + milliseconds + "ms");
            return;
        }

        LOGGER.info("Sleep for " + milliseconds + " ms NOW");
        sleepInternal(milliseconds);
    }

    private static void sleepInternal(int milliseconds) {
        long max = System.currentTimeMillis() + milliseconds;
        while (max > System.currentTimeMillis()) {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException ignore) {
            }
        }
    }

    public static void waitFor(Optional<Timeout> timeout, Callable<Boolean> c) throws TimeoutException {
        if(timeout.isPresent()) {
            LOGGER.info(timeout.get().getKey() + ": wait for at most " + timeout.get().getTimeoutInMs() + "ms or until condition is met.");

            long max = System.currentTimeMillis() + timeout.get().getTimeoutInMs();

            try {
                long work = 0;
                boolean wasSuccessful = false;
                while (max > System.currentTimeMillis()) {
                    work = max - System.currentTimeMillis();
                    if (c.call() && work >= 0) {
                        wasSuccessful = true;
                        break;
                    }
                    sleepInternal(timeout.get().getTimeToRepetitionInMs());
                }

                CalibrationTimeout calibrationTimeout = new CalibrationTimeout(timeout.get());
                if (wasSuccessful) {
                    calibrationTimeout.setValue(Math.toIntExact(work));
                    LOGGER.info("Condition of wait task was met in " + work + "/" + timeout.get().getTimeoutInMs() + "ms -> proceeding");
                    CalibrationTimeoutRepository.addCalibrationTimeout(calibrationTimeout);
                }else{
                    calibrationTimeout.setStatus(CalibrationTimeout.Status.EXCEEDED);
                    LOGGER.info("Condition of wait task NOT met within the specified time");
                    CalibrationTimeoutRepository.addCalibrationTimeout(calibrationTimeout);
                    throw new IllegalStateException("waited for " + timeout.get().getTimeoutInMs() + "ms, but condition was not met");
                }

            } catch (IllegalStateException e) {

                throw new TimeoutException(timeout.get());
            } catch (Exception e) {
                throw new IllegalStateException("internal error", e);
            }
        }else{
            throw new IllegalStateException("The timeout was null");
        }
    }

    public static void waitForSubstringInFile(Optional<Timeout> timeout, Path path, String substring) {
        waitFor(timeout, () -> FileTasks.hasFileSpecificSubstring(path, substring));
    }


    public static void waitForAvailabilityOfUrl(Optional<Timeout> timeout, URL url) {
        waitFor(timeout, () -> URLTasks.isUrlAvailable(url));
    }

    public static void waitForAvailabilityOfUrl(Optional<Timeout> timeout, String url) {
        try {
            waitForAvailabilityOfUrl(timeout, new URL(url));
        } catch (MalformedURLException e) {
            throw new IllegalStateException("url to check is not a valid url", e);
        }
    }

    public static void waitForContentInUrl(Optional<Timeout> timeout, URL url, String substring) {
        waitFor(timeout, () -> URLTasks.hasUrlSubstring(url, substring));
    }

    private static final Logger LOGGER = Logger.getLogger(WaitTasks.class);
}
