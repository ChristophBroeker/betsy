package betsy.virtual.host;

import betsy.data.BetsyProcess;
import betsy.virtual.common.messages.collect_log_files.LogFilesRequest;
import betsy.virtual.common.messages.deploy.DeployRequest;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * The {@link VirtualizedEngineAPI} offers methods that are required if dealing
 * with virtualized testing using betsy.
 *
 * @author Cedric Roeck
 * @version 1.0
 */
public interface VirtualizedEngineAPI {

    /**
     * Get the name of the VirtualMachine that is used by this engines.<br>
     * The name shall be the prefix 'betsy-' concatenated with the name of the
     * virtualized engine.
     *
     * @return name of the engine's VirtualMachine
     */
    public String getVirtualMachineName();

    /**
     * Get every {@link ServiceAddress} that must be available for the engine
     * before testing it.
     *
     * @return the list of required services for the engine
     */
    public List<ServiceAddress> getVerifiableServiceAddresses();

    /**
     * Get all ports that are required by the engine to be testable.
     *
     * @return list of required ports
     */
    public Set<Integer> getRequiredPorts();

    public DeployRequest buildDeployRequest(BetsyProcess process) throws IOException;

    public LogFilesRequest buildLogFilesRequest();

}
