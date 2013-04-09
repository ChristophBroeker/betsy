package de.uniba.wiai.dsg.betsy.virtual.host.utils;

import groovy.util.AntBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.uniba.wiai.dsg.betsy.virtual.host.VBoxConfiguration;

/**
 * The {@link VBoxWebService} offers methods to start and stop the VBoxWebSrv
 * executable of VirtualBox. This service is mandatory for the usage of the
 * JAXWS that is used by betsy to control the virtual machines.
 * 
 * @author Cedric Roeck
 * @version 1.0
 */
public class VBoxWebService {

	private static VBoxWebService instance = null;

	public static synchronized VBoxWebService getInstance() {
		if (instance == null) {
			instance = new VBoxWebService();
		}
		return instance;
	}

	private final AntBuilder ant;
	private final Logger log;
	private final VBoxConfiguration vconfig;

	private Process vboxServiceProcess;

	private VBoxWebService() {
		ant = new AntBuilder();
		log = Logger.getLogger(getClass());
		vconfig = new VBoxConfiguration();
	}

	/**
	 * Installs the VBoxWebSrv by starting it and making sure it is terminated
	 * if the current application is being closed again. A ShutdownHook is used
	 * to close the process.
	 * 
	 * @throws IOException
	 *             thrown if starting the VBoxWebSrv failed
	 */
	public void install() throws IOException {
		this.start();
		// install a ShutdownHook to terminate the service if the application
		// is being closed
		Thread shutdownThread = new Thread(new Runnable() {
			@Override
			public void run() {
				VBoxWebService.this.stop();
			}
		});
		Runtime.getRuntime().addShutdownHook(shutdownThread);
	}

	/**
	 * Start the VBoxWebSrv and wait 3 seconds to assure it is running. The
	 * VBoxWebSrv is started with a disabled authentication library.
	 * 
	 * @throws IOException
	 *             thrown if starting the VBoxWebSrv failed
	 */
	public void start() throws IOException {
		// start VBoxService
		ProcessBuilder pb = new ProcessBuilder(vconfig.getVBoxWebSrv()
				.getAbsolutePath(), "-A", "null");
		vboxServiceProcess = pb.start();
		// give the webSrv some time to start
		log.debug("Waiting 3 seconds for the VBoxWebSrv to start...");

		Map<String, Object> map = new HashMap<>();
		map.put("seconds", 3);
		ant.invokeMethod("sleep", map);

		log.debug("...VBoxWebSrv started!");
	}

	/**
	 * Destroy the process VBoxWebSrv is running in
	 */
	public void stop() {
		log.debug("Stopping VBoxWebSrv...");
		if (vboxServiceProcess != null) {
			vboxServiceProcess.destroy();
		}
	}

}
