package org.opendaylight.controller.scagent.service.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.dm.Component;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.controller.sal.core.ComponentActivatorAbstractBase;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.packet.Ethernet;
import org.opendaylight.controller.sal.packet.IDataPacketService;
import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.sal.packet.IPv4;
import org.opendaylight.controller.sal.packet.Packet;
import org.opendaylight.controller.sal.packet.PacketResult;
import org.opendaylight.controller.sal.packet.RawPacket;
import org.opendaylight.controller.scagent.service.api.SCAgentSampleServiceAPI;
import org.osgi.framework.BundleActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SCAgentSampleServiceImpl extends ComponentActivatorAbstractBase
		implements BundleActivator, BindingAwareConsumer,
		SCAgentSampleServiceAPI {
	protected Logger logger = LoggerFactory.getLogger(SCAgentSampleServiceAPI.class);
	private ConsumerContext session;
	private IDataPacketService dataPacketService;

	@Override
	public String sayHello(String args) {

		return String.format("args = %s: %s", args, "this is a sample service");

	}
	@Override
	public Object[] getImplementations() {
		logger.trace("Getting Implementations");

		Object[] res = { SCAgentSampleServiceImpl.class };
		return res;
	}

	@Override
	public void configureInstance(Component c, Object imp, String containerName) {
		logger.trace("Configuring instance");

		if (imp.equals(SCAgentSampleServiceImpl.class)) {

			// Define exported and used services for PacketHandler component.

			Dictionary<String, Object> props = new Hashtable<String, Object>();
			props.put("salListenerName", "SCAgentPacketHandler");

			// Export IListenDataPacket interface to receive packet-in events.
			c.setInterface(new String[] { IListenDataPacket.class.getName() },
					props);

			// Need the DataPacketService for encoding, decoding, sending data
			// packets
			c.add(createContainerServiceDependency(containerName)
					.setService(IDataPacketService.class)
					.setCallbacks("setDataPacketService",
							"unsetDataPacketService").setRequired(true));

		}
	}
	
	void setDataPacketService(IDataPacketService s) {
        logger.trace("Set DataPacketService.");
 
        dataPacketService = s;
    }
    void unsetDataPacketService(IDataPacketService s) {
        logger.trace("Removed DataPacketService.");
 
        if (dataPacketService == s) {
            dataPacketService = null;
        }
    }
	@Override
	public void onSessionInitialized(ConsumerContext arg0) {

		System.out
				.println("from scagent service activator: osgi framework is calling me. ");

		this.session = session;

	}

	@Override
	public PacketResult receiveDataPacket(RawPacket inPkt) {
		logger.trace("Received data packet.");

		// The connector, the packet came from ("port")
		NodeConnector ingressConnector = inPkt.getIncomingNodeConnector();
		// The node that received the packet ("switch")
		Node node = ingressConnector.getNode();
		// Use DataPacketService to decode the packet.
		Packet l2pkt = dataPacketService .decodeDataPacket(inPkt);

		if (l2pkt instanceof Ethernet) {
			Object l3Pkt = l2pkt.getPayload();
			if (l3Pkt instanceof IPv4) {
				IPv4 ipv4Pkt = (IPv4) l3Pkt;
				int dstAddr = ipv4Pkt.getDestinationAddress();
				InetAddress addr = intToInetAddress(dstAddr);
				System.out.println("Pkt. to " + addr.toString()
						+ " received by node " + node.getNodeIDString()
						+ " on connector "
						+ ingressConnector.getNodeConnectorIDString());
				return PacketResult.KEEP_PROCESSING;
			}
		}
		// We did not process the packet -> let someone else do the job.
		return PacketResult.IGNORED;
	}

	static private InetAddress intToInetAddress(int i) {
		byte b[] = new byte[] { (byte) ((i >> 24) & 0xff),
				(byte) ((i >> 16) & 0xff), (byte) ((i >> 8) & 0xff),
				(byte) (i & 0xff) };
		InetAddress addr;
		try {
			addr = InetAddress.getByAddress(b);
		} catch (UnknownHostException e) {
			return null;
		}

		return addr;
	}
}
