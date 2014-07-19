package org.opendaylight.controller.scagent.service.api;

import java.util.Map;

import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.scagent.northbound.utils.PolicyCommand;

public interface ISecurityControllerAgentService extends IListenDataPacket {

	String sayHello(String args);

	public Map<String, ? extends PolicyCommand> getAllPolicyCommands();

	public void addPolicyCommand(PolicyCommand policyCommand);

	public PolicyCommand removePolicyCommand(String id);

}
