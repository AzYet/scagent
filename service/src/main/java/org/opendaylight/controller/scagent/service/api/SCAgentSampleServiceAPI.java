package org.opendaylight.controller.scagent.service.api;

import org.opendaylight.controller.sal.packet.IListenDataPacket;

public interface SCAgentSampleServiceAPI extends IListenDataPacket{
	
	String sayHello(String args);
	
}
