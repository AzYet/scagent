package org.opendaylight.controller.scagent.service.api;

import java.util.Map;

import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.scagent.northbound.utils.PolicyActionType;
import org.opendaylight.controller.scagent.northbound.utils.PolicyCommand;

public interface ISecurityControllerAgentService extends IListenDataPacket {

	String sayHello(String args);

	public Map<org.opendaylight.controller.scagent.northbound.utils.PolicyActionType, Map<String, PolicyCommand>> getAllPolicyCommands();

	public PolicyCommand addPolicyCommand(PolicyCommand policyCommand);

    PolicyCommand removePolicyCommand(PolicyActionType type, String id);
}
