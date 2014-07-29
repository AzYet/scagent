package org.opendaylight.controller.scagent.service.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.felix.dm.Component;
import org.opendaylight.controller.hosttracker.IfIptoHost;
import org.opendaylight.controller.hosttracker.hostAware.HostNodeConnector;
import org.opendaylight.controller.northbound.commons.RestMessages;
import org.opendaylight.controller.northbound.commons.exception.ServiceUnavailableException;
import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.Output;
import org.opendaylight.controller.sal.action.SetDlDst;
import org.opendaylight.controller.sal.action.SetDlSrc;
import org.opendaylight.controller.sal.action.SetNwDst;
import org.opendaylight.controller.sal.action.SetNwSrc;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.controller.sal.core.ComponentActivatorAbstractBase;
import org.opendaylight.controller.sal.core.Edge;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.core.Path;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.sal.packet.Ethernet;
import org.opendaylight.controller.sal.packet.IDataPacketService;
import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.sal.packet.IPv4;
import org.opendaylight.controller.sal.packet.Packet;
import org.opendaylight.controller.sal.packet.PacketResult;
import org.opendaylight.controller.sal.packet.RawPacket;
import org.opendaylight.controller.sal.packet.TCP;
import org.opendaylight.controller.sal.routing.IRouting;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.scagent.northbound.utils.*;
import org.opendaylight.controller.scagent.service.api.ISecurityControllerAgentService;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.opendaylight.controller.topologymanager.ITopologyManager;
import org.openflow.protocol.OFMatch;
import org.openflow.util.HexString;
import org.osgi.framework.BundleActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityControllerAgentImpl extends ComponentActivatorAbstractBase
        implements BundleActivator, BindingAwareConsumer,
        ISecurityControllerAgentService {
    protected Logger logger = LoggerFactory
            .getLogger(ISecurityControllerAgentService.class);
    private ConsumerContext session;
    private IDataPacketService dataPacketService;
    public Map<PolicyActionType, Map<String, PolicyCommand>> policyCommands = new HashMap<>();

    @Override
    public String sayHello(String args) {
        String res = String.format("args = %s: %s", args, "this is a sample service");
        logger.info(res);
        return res;

    }

    @Override
    public Object[] getImplementations() {
        logger.trace("Getting Implementations");

        Object[] res = {SecurityControllerAgentImpl.class};
        return res;
    }

    @Override
    public void configureInstance(Component c, Object imp, String containerName) {
        logger.trace("Configuring instance");

        if (imp.equals(SecurityControllerAgentImpl.class)) {

            // Define exported and used services for PacketHandler component.

            Dictionary<String, Object> props = new Hashtable<String, Object>();
            props.put("salListenerName", "SCAgentPacketHandler");

            // Export IListenDataPacket interface to receive packet-in events.
            c.setInterface(new String[]{IListenDataPacket.class.getName(),
                            ISecurityControllerAgentService.class.getName()},
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
        ITopologyManager topologyManager = (ITopologyManager) ServiceHelper
                .getGlobalInstance(ITopologyManager.class, this);
        IfIptoHost hostTracker = (IfIptoHost) ServiceHelper.getGlobalInstance(
                IfIptoHost.class, this);
        IRouting routeService = (IRouting) ServiceHelper.getGlobalInstance(
                IRouting.class, this);
        if (policyCommands.get(PolicyActionType.BYOD_INIT) == null || policyCommands.get(PolicyActionType.BYOD_INIT).isEmpty()) {
            return PacketResult.IGNORED;
        }
        PacketResult result = PacketResult.IGNORED;
        // The connector, the packet came from ("port")
        NodeConnector ingressConnector = inPkt.getIncomingNodeConnector();
        // The node that received the packet ("switch")
        Node ingressNode = ingressConnector.getNode();
        // Use DataPacketService to decode the packet.
        Packet l2pkt = dataPacketService.decodeDataPacket(inPkt);
        // till now, we only deal with Ethernet frame
        if (!(l2pkt instanceof Ethernet))
            // if l2 frame is not Ethernet,drop it.
            return PacketResult.CONSUME;

        Ethernet ether = (Ethernet) l2pkt;
        logger.info("received a ether frame,mac source : {}", HexString.toHexString(ether.getSourceMACAddress()).toString());
        for (PolicyCommand policyCommand : policyCommands.get(PolicyActionType.BYOD_INIT).values()) {
            BYODRedirectCommand initCommand = (BYODRedirectCommand) policyCommand;
            if ((long) (ingressNode.getID()) != initCommand.getDpid()) {
                break;
            } else if ((short) ingressConnector.getID() != initCommand
                    .getInPort()) {
                break;
            }

            if (policyCommands.get(PolicyActionType.BYOD_ALLOW) != null) {
                boolean shouldBreak = false;
                for (PolicyCommand byodAllow : policyCommands
                        .get(PolicyActionType.BYOD_ALLOW).values()) {
                    if (Arrays.equals(ether.getSourceMACAddress(), byodAllow
                            .getMatch().getDataLayerSource())) {
                        // if MAC is authorized, leave it to other bundles like
                        // routing service.
                        shouldBreak = true;
                        break;
                    }
                }
                if (shouldBreak)
                    break;
            }
            Object l3Pkt = l2pkt.getPayload();
            if (!(l3Pkt instanceof IPv4)) {
                // maybe ARP packet, leave it to routing bundle
                break;
            }
            IPv4 ipv4Pkt = (IPv4) l3Pkt;
            int initNetwork = 0;
            int serverIp = 0;
            try {
                initNetwork = ByteBuffer.wrap(
                        InetAddress.getByName(initCommand.getNetwork())
                                .getAddress()).getInt();
                serverIp = ByteBuffer.wrap(
                        InetAddress.getByName(initCommand.getServerIp())
                                .getAddress()).getInt();
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if ((initNetwork & (0xFFFFFFFF << (32 - initCommand.getMask()))) != (ipv4Pkt
                    .getSourceAddress() & (0xFFFFFFFF << (32 - initCommand
                    .getMask())))) {
                // if packet comes from other network, leave it untouched
                break;
            }
            Object l4Pkt = ipv4Pkt.getPayload();
            if (!(l4Pkt instanceof TCP)) {
                // if not TCP frame, should be DHCP DNS or ARP, leave it
                // untouched.
                break;
            }
            TCP tcpPkt = (TCP) l4Pkt;
            result = PacketResult.CONSUME;
            if (tcpPkt.getDestinationPort() != 80) {
                // no other TP_DST TCP frame should be sent here
                break;
            }

            // now it's an HTTP frame came from DPID&port&network specified in
            // policy, should redirect it

            // first we should try to find the server's NodeConnector, if failure,
            // then no bother to go further
            HostNodeConnector serverNodeConnector = null;
            try {
                serverNodeConnector = hostTracker.hostFind(InetAddress
                        .getByName(initCommand.getServerIp()));
                if (serverNodeConnector == null) {
                    logger.error(
                            "cannot find server node connector {}, abort! ",
                            initCommand.getServerIp());
                    break;
                }
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
                break;
            }

            // let's find a path between inPort and server
            Node serverNode = serverNodeConnector.getnodeconnectorNode();
            Path route = routeService.getRoute(ingressNode, serverNode);
            if (route == null) {
                logger.error("cannot find path between {} and {}", ingressNode,
                        serverNode);
                break;
            }
            List<Edge> edges = route.getEdges();
            Match match = new Match();
            match.setField(MatchType.DL_SRC, ether.getSourceMACAddress());
            //TODO: replace 0x0800 to final
            match.setField(MatchType.DL_TYPE, (short) 0x0800);
            match.setField(MatchType.NW_SRC, intToInetAddress(ipv4Pkt.getSourceAddress()));
            match.setField(MatchType.NW_PROTO, (byte) 0x6);
            match.setField(MatchType.TP_DST, tcpPkt.getDestinationPort());
            // (BYODRedirectCommand byodCommand, OFMatch match, NodePortTuple
            // previousAttachPoint, NodePortTuple nextAttachPoint, String
            // action, int ip, byte[] mac, byte[] devMac)
            String flowId = null;
            for (int i = 0; i < edges.size(); i++) {
                logger.info("Redirect from device to Server, No. {}",i);
                if (i == 0) {
                    flowId = pushFlowSingleEntry(initCommand, match,
                            ingressConnector, edges.get(i)
                                    .getTailNodeConnector(), "none", 0, null,
                            null, (short) 0, (short) 500);
                } else if (i == edges.size() - 1) {
                    flowId = pushFlowSingleEntry(initCommand, match,
                            edges.get(i - 1).getHeadNodeConnector(),
                            edges.get(i).getTailNodeConnector(), "none", 0,
                            null, null, (short) 0, (short) 500);
                    flowId = pushFlowSingleEntry(initCommand, match,
                            edges.get(i).getHeadNodeConnector(),
                            serverNodeConnector.getnodeConnector(), "dst", serverIp,
                            HexString.fromHexString(initCommand.getServerMac()), null, (short) 0, (short) 500);
                } else {
                    flowId = pushFlowSingleEntry(initCommand, match,
                            edges.get(i - 1).getHeadNodeConnector(),
                            edges.get(i).getTailNodeConnector(), "none", 0,
                            null, null, (short) 0, (short) 500);
                }
            }
            //set up the returning flows
            Match mt = new Match();
            mt.setField(MatchType.DL_SRC, HexString.fromHexString(initCommand.getServerMac()));
            //TODO: replace 0x0800 to final
            mt.setField(MatchType.DL_TYPE, (short) 0x0800);
            mt.setField(MatchType.NW_SRC, intToInetAddress(ipv4Pkt.getDestinationAddress()));
            mt.setField(MatchType.NW_DST, intToInetAddress(ipv4Pkt.getSourceAddress()));
            mt.setField(MatchType.NW_PROTO, (byte) 0x6);
            mt.setField(MatchType.TP_SRC, tcpPkt.getDestinationPort());
            for (int i = edges.size() - 1; i >= 0; i--) {
                logger.info("Redirect from Server to device, No. {}",i);
                String flowId2;
                if (i == 0) {
                    flowId2 = pushFlowSingleEntry(initCommand, mt,
                            edges.get(i+1).getTailNodeConnector(),
                            edges.get(i).getHeadNodeConnector(),
                            "none", 0,
                            null, null, (short) 0, (short) 500);
                    flowId2 = pushFlowSingleEntry(initCommand, mt,
                            edges.get(i).getTailNodeConnector(), ingressConnector, "none", 0, null,
                            null, (short) 0, (short) 500);
                } else if (i == edges.size() - 1) {
                    mt.setField(MatchType.NW_SRC, intToInetAddress(serverIp));
                    flowId2 = pushFlowSingleEntry(initCommand, mt,
                            serverNodeConnector.getnodeConnector(),
                            edges.get(i).getHeadNodeConnector(),
                            "src", ipv4Pkt.getDestinationAddress(),
                            null, ether.getSourceMACAddress(), (short) 0, (short) 500);
                } else {
                    flowId2 = pushFlowSingleEntry(initCommand, mt,
                            edges.get(i+1).getTailNodeConnector(),
                            edges.get(i).getHeadNodeConnector(),
                            "none", 0,
                            null, null, (short) 0, (short) 500);
                }
            }
            return result;
        }
        // We did not process the packet -> let someone else do the job.
        return result;
    }

    private String pushFlowSingleEntry(BYODRedirectCommand initCommand,
                                       Match match, NodeConnector previousConnector,
                                       NodeConnector nextNodeConnector, String action, int ip, byte[] authSvrMac,
                                       byte[] devMac, short hardTimeout, short idleTimeout) {
        IFlowProgrammerService flowProgrammer = (IFlowProgrammerService) ServiceHelper
                .getGlobalInstance(IFlowProgrammerService.class, this);
        ISwitchManager switchManager = (ISwitchManager) ServiceHelper
                .getGlobalInstance(ISwitchManager.class, this);
        if (flowProgrammer == null || switchManager == null) {
            throw new ServiceUnavailableException(
                    "SwitchManager or FlowProgrammer "
                            + RestMessages.SERVICEUNAVAILABLE.toString());
        }
        if (((long) previousConnector.getNode().getID()) != ((long) nextNodeConnector
                .getNode().getID())) {
            logger.error("error : src port and dst port not on the same bridge!");
            return null;
        }
        match.setField(MatchType.IN_PORT, previousConnector);
        ArrayList<Action> actions = new ArrayList<Action>();
        if (action.equals("dst")) {
            SetDlDst setDlDst = new SetDlDst(authSvrMac);
            actions.add(setDlDst);
            SetNwDst setNwDst = new SetNwDst(intToInetAddress(ip));
            actions.add(setNwDst);
        } else if (action.equals("src")) {
            SetDlDst setDlDst = new SetDlDst(devMac);
            actions.add(setDlDst);
            SetNwSrc setNwSrc = new SetNwSrc(intToInetAddress(ip));
            actions.add(setNwSrc);
        }
        Output output = new Output(nextNodeConnector);
        actions.add(output);
        Flow flow = new Flow(match, actions);
        flow.setHardTimeout(hardTimeout);
        flow.setIdleTimeout(idleTimeout);
        flow.setId(0xABCDEFL);
        // priority + 1 to override "HTTP --> controller" flow
        flow.setPriority((short) (initCommand.getCommandPriority() + (short) 1));
        Status status = flowProgrammer.addFlow(nextNodeConnector.getNode(),
                flow);
        String flowId = null;
        if (status.isSuccess()) {
            logger.info("successfully add a flow. flow = {}, node = {}",flow,previousConnector.getNode());
            flowId = Cypher.getMD5(new String[]{
                    previousConnector.getNode().getNodeIDString(),
                    previousConnector.getNodeConnectorIDString() + "",
                    flow.toString()});
        }else{
            logger.info("failed to add a flow. flow = {}, node = {}",flow,previousConnector.getNode());
        }

        return flowId;
    }

    static public InetAddress intToInetAddress(int i) {
        byte b[] = new byte[]{(byte) ((i >> 24) & 0xff),
                (byte) ((i >> 16) & 0xff), (byte) ((i >> 8) & 0xff),
                (byte) (i & 0xff)};
        InetAddress addr;
        try {
            addr = InetAddress.getByAddress(b);
        } catch (UnknownHostException e) {
            return null;
        }

        return addr;
    }

    @Override
    public Map<PolicyActionType, Map<String, PolicyCommand>> getAllPolicyCommands() {
        // TODO Auto-generated method stub
        return this.policyCommands;
    }

    @Override
    public PolicyCommand addPolicyCommand(PolicyCommand policyCommand) {
        logger.info("adding a policy: {}", policyCommand.toString());
        Map<String, PolicyCommand> perTypePolicyCommands = policyCommands.get(policyCommand.getType());
        if (perTypePolicyCommands == null) {
            perTypePolicyCommands = new HashMap<>();
            policyCommands.put(policyCommand.getType(), perTypePolicyCommands);
            return perTypePolicyCommands.put(policyCommand.getId(), policyCommand);
        } else if (perTypePolicyCommands.containsKey(policyCommand.getId())) {
            return perTypePolicyCommands.get(policyCommand.getId());
        } else {
            return perTypePolicyCommands.put(policyCommand.getId(), policyCommand);
        }
    }

    @Override
    public PolicyCommand removePolicyCommand(PolicyActionType type, String id) {
        // TODO Auto-generated method stub
        return policyCommands.get(type).remove(id);
    }

    public static void main(String[] args) {
        String a = "00:00:52:54:00:22:33:42";
        byte[] bytes = HexString.fromHexString(a);
        System.out.println(bytes);
    }
}
