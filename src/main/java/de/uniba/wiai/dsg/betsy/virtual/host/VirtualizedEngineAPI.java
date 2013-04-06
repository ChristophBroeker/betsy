package de.uniba.wiai.dsg.betsy.virtual.host;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import betsy.data.Process;
import de.uniba.wiai.dsg.betsy.virtual.common.messages.DeployOperation;
import de.uniba.wiai.dsg.betsy.virtual.host.utils.ServiceAddress;

// TODO JavaDoc
public interface VirtualizedEngineAPI {

	// COMMENT warum nicht getName() ?
	// --> kein Ersatz, sondern Zusatz: "betsy-" + getName()  
	public String getVirtualMachineName();

	public boolean isVirtualMachineReady();

	public boolean isVMImported();

	public List<ServiceAddress> getVerifiableServiceAddresses();

	// TODO change to enginePorts
	public Set<Integer> getRequiredPorts();

	// required to forwared the port
	public Integer getEndpointPort();

	// TODO
	public String getEndpointPath(Process process);

	public DeployOperation buildDeployContainer(Process process)
			throws IOException;

	public String getVMLogfileDir();

	public String getVMDeploymentDir();

	public Integer getVMDeploymentTimeout();

	public String getTargetPackageExtension();

}