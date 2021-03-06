package betsy.bpel.virtual.host.virtualbox;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import betsy.bpel.virtual.host.ServiceAddress;
import betsy.bpel.virtual.host.VirtualBoxException;
import betsy.bpel.virtual.host.VirtualBoxMachine;
import betsy.bpel.virtual.host.engines.EngineNamingConstants;
import betsy.bpel.virtual.host.exceptions.VirtualEngineServiceException;
import betsy.bpel.virtual.host.virtualbox.utils.ServiceValidator;
import betsy.bpel.virtual.host.virtualbox.utils.port.PortUsageException;
import betsy.bpel.virtual.host.virtualbox.utils.port.PortVerifier;
import betsy.common.tasks.WaitTasks;
import betsy.common.timeouts.timeout.Timeout;
import betsy.common.timeouts.timeout.TimeoutRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class SnapshotCreator {

    private static final Logger log = Logger.getLogger(SnapshotCreator.class);

    private final VirtualBoxMachine virtualBoxMachine;
    private final String engineName;
    private final List<ServiceAddress> engineServices;
    private final Set<Integer> forwardingPorts;

    public SnapshotCreator(VirtualBoxMachine virtualBoxMachine, String engineName, List<ServiceAddress> engineServices, Set<Integer> forwardingPorts) {
        this.virtualBoxMachine = virtualBoxMachine;
        this.engineName = engineName;
        this.engineServices = engineServices;
        this.forwardingPorts = forwardingPorts;
    }

    public void invoke() throws VirtualBoxException {
        if (virtualBoxMachine.hasRunningSnapshot()) {
            return;
        }

        if (StringUtils.isBlank(engineName)) {
            throw new IllegalArgumentException("The name of the engine to "
                    + "import must not be blank");
        }
        if (engineServices == null || engineServices.isEmpty()) {
            throw new IllegalArgumentException("The list of services to verify"
                    + " if a vm has been started must not be null or empty");
        }

        if (virtualBoxMachine.isActive()) {
            throw new IllegalStateException("Can't create a running snapshot "
                    + "if the VM is already active. The VM should be "
                    + "poweredOff!");
        }

        try {
            PortVerifier.failForUsedPorts(forwardingPorts);
        } catch (PortUsageException e) {
            throw new VirtualBoxException("ports could not be forwarded", e);
        }

        log.debug("Create running-state snapshot");

        try {
            // configure
            virtualBoxMachine.applyPortForwarding(forwardingPorts);

            virtualBoxMachine.start();

            // ensure all is up and running
            failIfEngineServicesTimeout(engineName, engineServices);
            failIfBetsyServerTimesOut();

            WaitTasks.sleep(TimeoutRepository.getTimeout("SnapshotCreator.invoke").getTimeoutInMs());

            virtualBoxMachine.takeSnapshot(createSnapshotName(engineName), createSnapshotDescription());

            virtualBoxMachine.stop();
        } finally {
            // stop if vm is still running
            if (virtualBoxMachine.isRunning()) {
                log.warn("VM was still running, stop now");
                virtualBoxMachine.stop();
            }
        }
    }

    private void failIfEngineServicesTimeout(String engineName, List<ServiceAddress> engineServices) throws VirtualEngineServiceException {
        try {
            if (!ServiceValidator.isEngineReady(engineServices, getServiceTimeout(engineName))) {
                log.warn("engine services not found withing " + getServiceTimeout(engineName).getTimeoutInSeconds() + " seconds");
                // timeout in CountDownLatch
                throw new VirtualEngineServiceException(
                        "The required services for the engine were "
                                + "not available within " + getServiceTimeout(engineName).getTimeoutInSeconds()
                                + "s after starting the vm. If using a debian/"
                                + "ubuntu system, make sure to delete the "
                                + "'/etc/udev/rules.d/70-persistent-net.rules'"
                                + "file before exporting the appliance. "
                                + "If this error occurs "
                                + "repeatedly, please import the vm manually"
                                + " with a valid snapshot in 'Running' state.");
            }
        } catch (Exception e) {
            throw new VirtualEngineServiceException(e);
        }
    }

    private Timeout getServiceTimeout(String engineName) {
        return TimeoutRepository.getTimeout(engineName + ".service");
    }

    private void failIfBetsyServerTimesOut() throws VirtualEngineServiceException {
        if (!ServiceValidator.isBetsyServerReady(TimeoutRepository.getTimeout("SnapshotCreator.failIfBetsyServerTimesOut").getTimeoutInMs())) {
            log.warn("betsy server not found withing " + TimeoutRepository.getTimeout("SnapshotCreator.failIfBetsyServerTimesOut").getTimeoutInSeconds() + "s");
            throw new VirtualEngineServiceException(
                    "The required betsy server was "
                            + "not available within 15s "
                            + "after having found all other services. ");
        }
    }

    private String createSnapshotName(String engineName) {
        return EngineNamingConstants.VIRTUAL_NAME_PREFIX + engineName + "_import-snapshot";
    }

    private String createSnapshotDescription() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
        return "Machine is in 'saved' state. Snapshot created during import on " + sdf.format(date);
    }


}
