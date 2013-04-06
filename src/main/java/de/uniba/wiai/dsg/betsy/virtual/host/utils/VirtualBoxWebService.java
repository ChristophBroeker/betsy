package de.uniba.wiai.dsg.betsy.virtual.host.utils;

import groovy.util.AntBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class VirtualBoxWebService {

	private final AntBuilder ant = new AntBuilder();
	private final Logger log = Logger.getLogger(getClass());
	private final String path;
	private Process vboxServiceProcess;
	
	public VirtualBoxWebService(final String path) {
		if(StringUtils.isBlank(path)) {
			throw new IllegalArgumentException("Path to the VBoxWebSrv must not be null or empty!");
		}
		this.path = path;
	}
	
	public void start() throws IOException {
		// start VBoxService
		ProcessBuilder pb = new ProcessBuilder(path);
		vboxServiceProcess = pb.start();
		// give the webSrv some time to start
		log.debug("Waiting 3 seconds for the VBoxWebSrv to start...");
		
		Map<String, Object> map = new HashMap<>();
		map.put("seconds", 3);
		ant.invokeMethod("sleep", map);
		
		log.debug("...VBoxWebSrv started!");
	}
	
	public void stop() {
		log.debug("Stopping VBoxWebSrv...");
		if(vboxServiceProcess != null) {
			vboxServiceProcess.destroy();
		}
	}
	
}