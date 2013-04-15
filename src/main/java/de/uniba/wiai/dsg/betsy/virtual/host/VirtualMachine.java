package de.uniba.wiai.dsg.betsy.virtual.host;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.virtualbox_4_2.IConsole;
import org.virtualbox_4_2.IMachine;
import org.virtualbox_4_2.INATEngine;
import org.virtualbox_4_2.INetworkAdapter;
import org.virtualbox_4_2.IProgress;
import org.virtualbox_4_2.ISession;
import org.virtualbox_4_2.ISnapshot;
import org.virtualbox_4_2.LockType;
import org.virtualbox_4_2.MachineState;
import org.virtualbox_4_2.NATProtocol;
import org.virtualbox_4_2.VBoxException;
import org.virtualbox_4_2.VirtualBoxManager;

import betsy.data.engines.Engine;
import de.uniba.wiai.dsg.betsy.Configuration;
import de.uniba.wiai.dsg.betsy.virtual.host.exceptions.PortUsageException;
import de.uniba.wiai.dsg.betsy.virtual.host.exceptions.VirtualizedEngineServiceException;
import de.uniba.wiai.dsg.betsy.virtual.host.exceptions.vm.PortRedirectException;
import de.uniba.wiai.dsg.betsy.virtual.host.exceptions.vm.VBoxExceptionCode;
import de.uniba.wiai.dsg.betsy.virtual.host.utils.PortVerifier;
import de.uniba.wiai.dsg.betsy.virtual.host.utils.ServiceAddress;
import de.uniba.wiai.dsg.betsy.virtual.host.utils.ServiceValidator;

/**
 * The {@link VirtualMachine} represents a virtual machine running on
 * VirtualBox. It can be started, stopped and snapshots may be created of its
 * current state.
 * 
 * @author Cedric Roeck  
 * @version 1.0
 */
public class VirtualMachine {

	private final Configuration config = Configuration.getInstance();

	private final IMachine machine;
	private final VirtualBoxManager vbManager;
	private ISession session;

	private static final Logger log = Logger.getLogger(VirtualMachine.class);

	public VirtualMachine(VirtualBoxManager vbManager, IMachine machine) {
		this.machine = Objects.requireNonNull(machine);
		this.vbManager = Objects.requireNonNull(vbManager);
		this.session = vbManager.getSessionObject();
	}

	/**
	 * Start the {@link IMachine}. VirtualBox currently supports three different
	 * states. Two of them are showing the GUI, one is saving the resources,
	 * hides the GUI and therefore is 'headless'.<br>
	 * <br>
	 * The {@link IMachine} should not be running before.
	 * 
	 * @param headless
	 *            if true, the {@link VirtualMachine} will be running in the
	 *            background and no GUI window will appear.
	 */
	public void start(final boolean headless) {
		log.trace("Starting VM");
		if (!isActive()) {
			IProgress startProgress = null;
			if (headless) {
				startProgress = machine.launchVMProcess(session, "headless",
						null);
			} else {
				startProgress = machine.launchVMProcess(session, "gui", null);
			}
			if (!startProgress.getCompleted()) {
				startProgress.waitForCompletion(60000);
			}
		} else {
			log.warn("Can't start VM, is already active");
		}
	}

	/**
	 * Stopping the {@link IMachine} and causing the VirtualBox's VM to be in
	 * 'PoweredOff' state.<br>
	 * If the VM is still running after the timeout, it will be killed using
	 * VirtualBox's emergency kill switch.<br>
	 * <br>
	 * Should only be used of the {@link IMachine} is in a running state.
	 */
	public void stop() {
		log.trace("Stopping VM");
		try {
			if (isRunning()) {
				IConsole console = session.getConsole();
				IProgress stopProgress = console.powerDown();
				stopProgress.waitForCompletion(15000);
				// if not stopped now it will be killed...
			}
		} catch (VBoxException exception) {
			if (VBoxExceptionCode.valueOf(exception).equals(
					VBoxExceptionCode.VBOX_E_INVALID_VM_STATE)) {
				// ignore
				log.warn("Could not power off, VM was in invalid state:",
						exception);
			} else {
				// rethrow as unexpected VBoxException
				throw exception;
			}
		} finally {
			// verify if stopped, else kill
			if (isActive()) {
				kill();
			}
			try {
				session.unlockMachine();
			} catch (VBoxException exception) {
				// ignore if was not locked
			}
		}
		log.trace("finished stop method");
	}

	/**
	 * Save the current running state of the {@link IMachine}. After saving the
	 * state the {@link IMachine} won't be running anymore.<br>
	 * It can be used as an alternative to {@link #stop()} and is very helpful
	 * while creating a new {@link Engine}. Nevertheless is also takes
	 * significantly longer. <br>
	 * <br>
	 * Should only be used of the {@link IMachine} is in a running state.
	 */
	public void saveState() {
		log.trace("Saving VM state");
		try {
			if (isRunning()) {
				IConsole console = session.getConsole();
				IProgress saveProgress = console.saveState();
				while (!saveProgress.getCompleted()) {
					saveProgress.waitForCompletion(30000);
				}
			}
		} catch (VBoxException exception) {
			if (VBoxExceptionCode.valueOf(exception).equals(
					VBoxExceptionCode.VBOX_E_INVALID_VM_STATE)) {
				// ignore
				log.warn("Could not save VM state, was in invalid state:",
						exception);
			} else {
				// unknown
				log.error("Unexpected VBoxException: ", exception);
				throw exception;
			}
		} finally {
			// verify if stopped, else kill
			if (isActive()) {
				kill();
			}
			try {
				session.unlockMachine();
			} catch (VBoxException exception) {
				// ignore if was not locked
			}
		}
	}

	/**
	 * Killing the {@link IMachine} using the 'emergencystop'.<br>
	 * Stops the VM in nearly every situation but might also cause data loss or
	 * corrupted states. <br>
	 * <br>
	 * Should only be used of the {@link IMachine} is in a running state.
	 */
	public void kill() {
		log.debug("killing machine");
		machine.launchVMProcess(session, "emergencystop", null);
		try {
			session.unlockMachine();
		} catch (VBoxException exception) {
			// ignore if was not locked
		}
	}

	/**
	 * Check whether the {@link IMachine} is in an active running state.
	 * Therefore the State must be either Running, Paused or Stuck (after an
	 * severe error).<br>
	 * If the VM is running it can be stopped.
	 * 
	 * @return true if the VM is running, paused or stuck
	 */
	public boolean isRunning() {
		try {
			MachineState state = this.machine.getState();
			if (state.equals(MachineState.Running)) {
				return true;
			}
			if (state.equals(MachineState.Paused)) {
				return true;
			}
			if (state.equals(MachineState.Stuck)) {
				return true;
			}
		} catch (VBoxException exception) {
			// in error cases the state can't be always read.
			log.error("Couldn't determine running state:", exception);
		}
		return false;
	}

	/**
	 * Check whether the {@link IMachine} is in a running state.<br>
	 * Even if this method is very similar to {@link #isRunning()}, the
	 * difference is that every mid-state such as saving, powering-off, etc. is
	 * still a running state, even if the VM is currently not responding to the
	 * User's input.
	 * 
	 * @return true if the VM is active
	 */
	public boolean isActive() {
		try {
			MachineState state = this.machine.getState();
			log.debug("Is VM active? State: " + state.toString());

			if (state.equals(MachineState.Aborted)) {
				return false;
			} else if (state.equals(MachineState.PoweredOff)) {
				return false;
			} else if (state.equals(MachineState.Saved)) {
				return false;
			} else {
				// in every other state the VM is active
				return true;
			}
		} catch (VBoxException exception) {
			// in error cases the state can't be always read.
			log.warn("Couldn't determine active state:", exception);
		}
		return false;
	}

	/**
	 * Check whether the VM has at least one {@link ISnapshot} that is marked as
	 * 'online' and therefore contains an already started system.
	 * 
	 * @return true if VM contains at least one 'online' snapshot
	 */
	public boolean hasRunningSnapshot() {
		ISnapshot snapshot = machine.getCurrentSnapshot();
		if (snapshot != null) {
			// online snapshot?
			return snapshot.getOnline();
		}
		return false;
	}

	/**
	 * Taking a snapshot of this {@link VirtualMachine} based on the current
	 * state.<br>
	 * A bug in VirtualBox (https://www.virtualbox.org/ticket/9255) forces us to
	 * pause the VM before taking the snapshot. Afterwards we will always resume
	 * the VM, even if it was already paused before.
	 * 
	 * @param name
	 *            the name of the snapshot to be created
	 * @param desc
	 *            the description of the snapshot to be created
	 * @return the created snapshot
	 */
	public ISnapshot takeSnapshot(final String name, final String desc) {
		log.debug("Taking VM snapshot");
		IConsole console = session.getConsole();

		log.debug("Pausing VM before taking a snapshot");
		console.pause();

		IProgress snapshotProgress = console.takeSnapshot(name, desc);
		while (!snapshotProgress.getCompleted()) {
			snapshotProgress.waitForCompletion(30000);
		}

		// before resuming make sure the snapshot has been saved
		while (this.machine.getState().equals(MachineState.Saving)) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// ignore
			}
		}

		log.debug("Resuming VM after taking a snapshot");
		console.resume();

		// get latestSnapshot and return
		return this.machine.getCurrentSnapshot();
	}

	/**
	 * Resetting the {@link IMachine} to the latest {@link ISnapshot}.<br>
	 * <br>
	 * Requires the {@link IMachine} to have at least one {@link ISnapshot}.
	 */
	public void resetToLatestSnapshot() {
		ISession subSession = null;
		try {
			log.debug("Resetting " + machine.getName() + " to latest snapshot");

			IConsole console = null;
			subSession = vbManager.getSessionObject();
			machine.lockMachine(subSession, LockType.Write);
			console = subSession.getConsole();

			ISnapshot snapshot = machine.getCurrentSnapshot();
			if (snapshot == null) {
				throw new IllegalStateException("Can't reset the VM to the "
						+ "latest snapshot if the VM does not have any "
						+ "snapshot.");
			}

			IProgress snapshotProgress = console.restoreSnapshot(snapshot);
			while (!snapshotProgress.getCompleted()) {
				snapshotProgress.waitForCompletion(30000);
			}
		} finally {
			if (subSession != null) {
				try {
					subSession.unlockMachine();
				} catch (VBoxException exception) {
					// ignore if was not locked
					log.warn("Unlocking subSession after restoration failed:",
							exception);
				}
			}
		}
	}

	/**
	 * Setup port forwarding for all the given forwardingPorts. The port on the
	 * host is always equal to the port on the guest system. All existing
	 * redirections are deleted before applying the new redirections.<br>
	 * <br>
	 * This method is using the JAXWS methods which do have a severe issue until
	 * version 4.2.11 if running VirtualBox on OS X.
	 * (https://www.virtualbox.org/ticket/11635)
	 * 
	 * @param forwardingPorts
	 *            all ports to create a forwarding for.
	 * @throws PortRedirectException
	 *             thrown if redirection could not be created
	 */
	public void applyPortForwarding(final Set<Integer> forwardingPorts)
			throws PortRedirectException {
		if (!isAlreadyRedirected(forwardingPorts)) {
			log.debug("Applying new port redirects...");

			// remove old redirections first
			clearPortForwarding();

			// add bVMS port if not yet contained
			forwardingPorts.add(48888);

			INATEngine natEngine = getNATEngine();
			for (Integer port : forwardingPorts) {
				// assuming every service uses TCP, which is true for HTTP
				natEngine.addRedirect("", NATProtocol.TCP, "", port, "", port);
			}

			long timeout = 10000;
			long start = -System.currentTimeMillis();

			while (natEngine.getRedirects().size() != forwardingPorts.size()) {
				if (System.currentTimeMillis() + start > timeout) {
					throw new PortRedirectException("Could not set redirected "
							+ "ports within 10s");
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		} else {
			log.trace("All ports are already redirected, skip.");
		}
	}

	private boolean isAlreadyRedirected(final Set<Integer> forwardingPorts) {
		// get existing redirects
		INATEngine natEngine = getNATEngine();
		List<String> redirects = natEngine.getRedirects();

		int matchingForwards = 0;

		for (String redirect : redirects) {
			// resolve host and guest port
			String[] rds = redirect.split(",");
			String hostPort = rds[3];
			String guestPort = rds[5];
			// verify both are equal, ignoring any other manually created
			// redirection
			if (hostPort.equals(guestPort)) {
				// verify is in list
				if (forwardingPorts.contains(hostPort)) {
					// is ok, increase count
					matchingForwards++;
				}
			}
		}

		return matchingForwards == forwardingPorts.size();
	}

	/**
	 * Setup port forwarding for all the given forwardingPorts. The port on the
	 * host is always equal to the port on the guest system. All existing
	 * redirections are deleted before applying the new redirections.<br>
	 * <br>
	 * This method is using a workaround using the CLI. The JAXWS methods do
	 * have a severe issue until version 4.2.11 if running VirtualBox on OS X.
	 * (https://www.virtualbox.org/ticket/11635)
	 * 
	 * @param forwardingPorts
	 *            all ports to create a forwarding for.
	 * @throws PortRedirectException
	 *             thrown if redirection could not be created
	 */
	public void applyPortForwardingWA(final Set<Integer> forwardingPorts)
			throws PortRedirectException {
		if (!isAlreadyRedirected(forwardingPorts)) {
			log.debug("Applying new port redirects...");

			// make sure to add the bVMS port
			forwardingPorts.add(48888);

			// remove old redirections first
			clearPortForwardingWA();

			INATEngine natEngine = getNATEngine();

			String vbpath = config.getValueAsString("virtualisation.vbox.path");
			String vboxManagePath = config
					.getValueAsString("virtualisation.vbox.vboxmanage");
			File vbm = new File(vbpath, vboxManagePath);

			Runtime r = Runtime.getRuntime();

			for (Integer port : forwardingPorts) {
				try {
					String[] cmd = { vbm.getAbsolutePath(), "modifyvm",
							machine.getName(), "--natpf1",
							",tcp,," + port + ",," + port };
					Process proc = r.exec(cmd);
					InputStream inStr = proc.getInputStream();
					BufferedReader buff = new BufferedReader(
							new InputStreamReader(inStr));
					String str;
					while ((str = buff.readLine()) != null) {
						log.debug("console output:" + str);
					}

				} catch (IOException exception) {
					log.warn("Can't add port redirect rule:", exception);
				}

			}

			long timeout = 10000;
			long start = -System.currentTimeMillis();

			while (natEngine.getRedirects().size() != forwardingPorts.size()) {
				if (System.currentTimeMillis() + start > timeout) {
					throw new PortRedirectException(
							"Could not set redirected ports within 10s");
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		} else {
			log.trace("All ports are already redirected, skip.");
		}
	}

	/**
	 * Delete all existing port redirections of the machines {@link INATEngine}. <br>
	 * <br>
	 * This method is using a workaround using the CLI. The JAXWS methods do
	 * have a severe issue until version 4.2.11 if running VirtualBox on OS X.
	 * 
	 * @throws PortRedirectException
	 *             thrown if a port redirection could not be deleted
	 */
	public void clearPortForwardingWA() throws PortRedirectException {
		log.debug("Deleting all existing port redirections");

		INATEngine natEngine = getNATEngine();
		for (String redirect : natEngine.getRedirects()) {
			String[] rds = redirect.split(",");
			String redirectName = rds[0];

			String vbpath = config.getValueAsString("virtualisation.vbox.path");
			String vboxManagePath = config
					.getValueAsString("virtualisation.vbox.vboxmanage");
			File vbm = new File(vbpath, vboxManagePath);

			Runtime r = Runtime.getRuntime();
			String[] cmd = { vbm.getAbsolutePath(), "modifyvm",
					machine.getName(), "--natpf1", "delete", redirectName };
			try {
				Process proc = r.exec(cmd);
				InputStream inStr = proc.getInputStream();
				BufferedReader buff = new BufferedReader(new InputStreamReader(
						inStr));
				String str;
				while ((str = buff.readLine()) != null) {
					log.debug("console output:" + str);
				}

			} catch (IOException exception) {
				log.warn("Can't delete port redirect rule:", exception);
			}
		}

		long timeout = 10000;
		long start = -System.currentTimeMillis();

		while (natEngine.getRedirects().size() != 0) {
			if (System.currentTimeMillis() + start > timeout) {
				throw new PortRedirectException(
						"Could not delete redirected ports within 10s");
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	/**
	 * Delete all existing port redirections of the machines {@link INATEngine}. <br>
	 * <br>
	 * This method is using the JAXWS methods which do have a severe issue until
	 * version 4.2.11 if running VirtualBox on OS X.
	 * (https://www.virtualbox.org/ticket/11635)
	 * 
	 * @throws PortRedirectException
	 *             thrown if a port redirection could not be deleted
	 */
	public void clearPortForwarding() throws PortRedirectException {
		log.debug("Deleting all existing port redirections");
		INATEngine natEngine = getNATEngine();
		for (String redirect : natEngine.getRedirects()) {
			String[] rds = redirect.split(",");
			String redirectName = rds[0];
			natEngine.removeRedirect(redirectName);
		}

		long timeout = 10000;
		long start = -System.currentTimeMillis();

		while (natEngine.getRedirects().size() != 0) {
			if (System.currentTimeMillis() + start > timeout) {
				throw new PortRedirectException("Could not delete all "
						+ "redirected ports within 10s");
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	private INATEngine getNATEngine() {
		// first adapter is always the NAT adapter
		INetworkAdapter na = machine.getNetworkAdapter(0l);
		return na.getNATEngine();
	}

	/**
	 * Create a running snapshot of the {@link VirtualMachine}.<br>
	 * To create a running snapshot the VM must not be active. Then, the
	 * following steps are executed:
	 * <ul>
	 * <li>Apply port forwarding</li>
	 * <li>Start the VM</li>
	 * <li>Wait for the engines services to be ready</li>
	 * <li>Wait for betsy's server on the VM to be ready</li>
	 * <li>Take a snapshot</li>
	 * <li>Stop the VM</li>
	 * </ul>
	 * <br>
	 * The {@link VirtualMachine} will be stopped after creating the snapshot or
	 * if an exception did occur.
	 * 
	 * @param engineName
	 *            the name of the engine the VM belong to
	 * @param engineServices
	 *            a {@link List} of service addresses that are required by the
	 *            engine. If they are available the VM is started and ready.
	 * @param forwardingPorts
	 *            the ports required by the engine
	 * @param headless
	 *            if true, the snapshot will be created without showing the
	 *            VirtualBox GUI
	 * 
	 * @throws VirtualizedEngineServiceException
	 *             thrown if a service of the engine was not available
	 * @throws PortRedirectException
	 *             thrown if a port could not be redirected
	 * @throws InterruptedException
	 *             thrown if waiting on the services was interrupted
	 * @throws PortUsageException
	 *             thrown if a required port is already in use by another
	 *             application
	 */
	public void createRunningSnapshot(final String engineName,
			final List<ServiceAddress> engineServices,
			final Set<Integer> forwardingPorts, final boolean headless)
			throws VirtualizedEngineServiceException, PortRedirectException,
			InterruptedException, PortUsageException {

		if (StringUtils.isBlank(engineName)) {
			throw new IllegalArgumentException("The name of the engine to "
					+ "import must not be blank");
		}
		if (engineServices == null || engineServices.isEmpty()) {
			throw new IllegalArgumentException("The list of services to verify"
					+ " if a vm has been started must not be null or empty");
		}

		if (isActive()) {
			throw new IllegalStateException("Can't create a running snapshot "
					+ "if the VM is already active. The VM should be "
					+ "poweredOff!");
		}

		PortVerifier.verify(forwardingPorts);

		log.debug("Create running-state snapshot");

		try {
			applyPortForwarding(forwardingPorts);

			// start the VM for the first time
			this.start(headless);

			final int secondsToWait = config.getValueAsInteger(
					"virtualisation.engines." + engineName + ".serviceTimeout",
					300);
			ServiceValidator sv = new ServiceValidator();
			boolean ready = sv.isEngineReady(engineServices, secondsToWait);

			if (!ready) {
				log.warn("engine services not found withing " + secondsToWait
						+ "s");
				// timeout in CountDownLatch
				throw new VirtualizedEngineServiceException(
						"The required services for the engine were "
								+ "not available within " + secondsToWait
								+ "s after starting the vm. If using a debian/"
								+ "ubuntu system, make sure to delete the "
								+ "'/etc/udev/rules.d/70-persistent-net.rules'"
								+ "file before exporting the appliance. "
								+ "If this error occurs "
								+ "repeatedly, please import the vm manually"
								+ " with a valid snapshot in 'Running' state.");
			}

			ready = sv.isBetsyServerReady(15000);
			if (!ready) {
				log.warn("betsy server not found withing 15s");
				throw new VirtualizedEngineServiceException(
						"The required betsy server was "
								+ "not available within 15s "
								+ "after having found all other services. ");
			}

			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");

			// take a snapshot of the started VM
			String snapName = VirtualizedEngine.VIRTUAL_NAME_PREFIX
					+ engineName + "_import-snapshot";
			String snapDesc = "Machine is in 'saved' state. Snapshot created during import on "
					+ sdf.format(date);

			this.takeSnapshot(snapName, snapDesc);

			// stops the VM again and unlocks session
			this.stop();
		} catch (MalformedURLException exception) {
			throw new VirtualizedEngineServiceException("Could not verify "
					+ "engine servies availability. One address is invalid: ",
					exception);
		} finally {
			// stop if vm is still running
			if (this.isRunning()) {
				log.warn("VM was still running, stop now");
				this.stop();
			}
		}
	}
}