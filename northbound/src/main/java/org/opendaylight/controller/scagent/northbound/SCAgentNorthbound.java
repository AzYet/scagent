package org.opendaylight.controller.scagent.northbound;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.opendaylight.controller.hosttracker.IfIptoHost;
import org.opendaylight.controller.hosttracker.hostAware.HostNodeConnector;
import org.opendaylight.controller.northbound.commons.RestMessages;
import org.opendaylight.controller.northbound.commons.exception.InternalServerErrorException;
import org.opendaylight.controller.northbound.commons.exception.ServiceUnavailableException;
import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.Controller;
import org.opendaylight.controller.sal.core.ConstructionException;
import org.opendaylight.controller.sal.core.Edge;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.sal.routing.IRouting;
import org.opendaylight.controller.sal.utils.HexEncode;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.scagent.northbound.utils.BYODInitConfig;
import org.opendaylight.controller.scagent.northbound.utils.BYODInitConfig.SwitchPort;
import org.opendaylight.controller.scagent.northbound.utils.Cypher;
import org.opendaylight.controller.scagent.northbound.utils.FindHostsResult;
import org.opendaylight.controller.scagent.northbound.utils.InputHost;
import org.opendaylight.controller.scagent.northbound.utils.MatchArguments;
import org.opendaylight.controller.scagent.northbound.utils.PolicyActionType;
import org.opendaylight.controller.scagent.northbound.utils.PolicyCommand;
import org.opendaylight.controller.scagent.northbound.utils.PolicyCommandDeployed;
import org.opendaylight.controller.scagent.northbound.utils.PolicyCommandRelated;
import org.opendaylight.controller.scagent.northbound.utils.SwitchFlowModCount;
import org.opendaylight.controller.scagent.service.api.SCAgentSampleServiceAPI;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.opendaylight.controller.topologymanager.ITopologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class SCAgentNorthbound {
	/**
	 * SCAgent northbound
	 */
	public static Logger logger = LoggerFactory
			.getLogger(SCAgentNorthbound.class);

	// 将所有策略保存在内存中
	public static Map<String, PolicyCommandDeployed> policyCommandsDeployed = new HashMap<String, PolicyCommandDeployed>();
	static Map<String, PolicyCommand> sourcePolicyCommandsWithoutInPort = new HashMap<String, PolicyCommand>();
	// <DPID,SwitchFlowModCount>
	static Map<Long, Map<String, SwitchFlowModCount>> switchFlowModCountMap = new HashMap<Long, Map<String, SwitchFlowModCount>>();
	// <FlowModId,SwitchFlowModCount>
	static Map<String, SwitchFlowModCount> flowModCountMap = new HashMap<String, SwitchFlowModCount>();
	// <FlowModId,SwitchFlowModCount>
	static Map<String, SwitchFlowModCount> globalFlowModCountMap = new HashMap<String, SwitchFlowModCount>();

	@Path("/scagent/{param}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@TypeHint(FindHostsResult.class)
	@StatusCodes({ @ResponseCode(code = 200, condition = "operational"),
			@ResponseCode(code = 503, condition = "Internal error"),
			@ResponseCode(code = 503, condition = "misfunctional") })
	public FindHostsResult scagentResource(
			@PathParam(value = "param") String param) {

		SCAgentSampleServiceAPI scSmplService = (SCAgentSampleServiceAPI) ServiceHelper
				.getGlobalInstance(SCAgentSampleServiceAPI.class, this);
		ITopologyManager topologyManager = (ITopologyManager) ServiceHelper
				.getGlobalInstance(ITopologyManager.class, this);
		IfIptoHost hostTracker = (IfIptoHost) ServiceHelper.getGlobalInstance(
				IfIptoHost.class, this);
		IRouting routeService = (IRouting) ServiceHelper.getGlobalInstance(
				IRouting.class, this);
		Map<Node, Set<Edge>> nodeEdges;
		String resStr = "";
		String[] parts = null;
		if (param.equalsIgnoreCase("nodes")) {
			nodeEdges = topologyManager.getNodeEdges();

			for (Node n : nodeEdges.keySet()) {
				resStr += n.getNodeIDString() + "; ";
			}
			// return Response.ok(new String(param + ": " + resStr)).status(503)
			// .build();
		} else if ((parts = param.split("-")).length == 2) {
			if (parts[0].equalsIgnoreCase("ip")) {
				try {
					HostNodeConnector nodeConnector = hostTracker
							.hostFind(InetAddress.getByName(parts[1]));
					if (nodeConnector != null) {
						return new FindHostsResult(nodeConnector
								.getnodeconnectorNode().getNodeIDString()
								.toString(), (short) nodeConnector
								.getnodeConnector().getID());

						// return Response.ok(
						// new String(nodeConnector.getnodeconnectorNode()
						// .getNodeIDString()
						// + " "
						// + nodeConnector.getnodeConnector()
						// .getNodeConnectorIdAsString()))
						// .build();
					} else {
						// return Response
						// .ok(String.format("404! host %s not found",
						// parts[1])).status(500).build();
					}
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (parts[0].equalsIgnoreCase("mac")) {
				System.out.println("finding hosts by mac");
				Set<HostNodeConnector> allHosts = hostTracker.getAllHosts();
				if (allHosts != null && allHosts.size() > 0) {
					System.out.println("getting hosts list success.");
					for (HostNodeConnector hnn : allHosts) {
						if (Arrays.equals(hnn.getDataLayerAddressBytes(),
								HexEncode.bytesFromHexString(parts[1]))) {
							return new FindHostsResult(hnn
									.getnodeconnectorNode().getNodeIDString(),
									(short) hnn.getnodeConnector().getID());
							// return Response
							// .ok(new String(
							// hnn.getnodeconnectorNode()
							// .getNodeIDString()
							// + "  "
							// + hnn.getnodeConnector()
							// .getNodeConnectorIdAsString()))
							// .build();
						}
					}
					// return Response.ok(new String(resStr)).build();
				} else {
					// return Response
					// .ok(String.format("404! host mac: %s not found",
					// parts[1])).status(404).build();
				}

			}
		} else if ((parts = param.split("-")).length == 3) {
			if (parts[0].equalsIgnoreCase("route")) {
				try {
					HostNodeConnector nodeConnector1 = hostTracker
							.hostFind(InetAddress.getByName(parts[1]));
					HostNodeConnector nodeConnector2 = hostTracker
							.hostFind(InetAddress.getByName(parts[2]));
					if (topologyManager != null && nodeConnector1 != null
							&& nodeConnector2 != null) {
						org.opendaylight.controller.sal.core.Path route = routeService
								.getRoute(
										nodeConnector1.getnodeconnectorNode(),
										nodeConnector2.getnodeconnectorNode());
						if (route != null)
							for (Edge e : route.getEdges()) {
								resStr += e.getTailNodeConnector().getNode()
										+ "-"
										+ e.getTailNodeConnector().getID()
										+ "==>"
										+ e.getHeadNodeConnector().getNode()
										+ "-"
										+ e.getHeadNodeConnector().getID()
										+ "  ";
							}
						// return Response.ok(resStr).build();
					} else {
						// return Response
						// .ok(String.format("404! host %s not found",
						// parts[1])).status(500).build();
					}
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			if (scSmplService == null) {
				// service not found.
				// return Response.ok(new String("No scagent service"))
				// .status(500).build();
			}
			String res = null;
			if ((res = scSmplService.sayHello(param)) != null) {
				// return Response.ok(new String(res)).build();
			}

		}
		// return Response.ok(new String(param + " - something went wrong!"))
		// .status(503).build();
		return new FindHostsResult("none", (short) 0);
	}

	@Path("/scagent/policyaction/{format}")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@TypeHint(InputHost.class)
	@StatusCodes({ @ResponseCode(code = 200, condition = "operational"),
			@ResponseCode(code = 503, condition = "Internal error"),
			@ResponseCode(code = 503, condition = "misfunctional") })
	public Response testPostHandler(@PathParam(value = "format") String param,
			@TypeHint(InputHost.class) InputHost inHost) {

		IfIptoHost hostTracker = (IfIptoHost) ServiceHelper.getGlobalInstance(
				IfIptoHost.class, this);
		IFlowProgrammerService flowProgrammer = (IFlowProgrammerService) ServiceHelper
				.getGlobalInstance(IFlowProgrammerService.class, this);
		if (hostTracker == null || flowProgrammer == null) {
			throw new ServiceUnavailableException(
					"HostTracker or flowProgrammer "
							+ RestMessages.SERVICEUNAVAILABLE.toString());
		}
		try {
			HostNodeConnector nodeConnector = hostTracker.hostFind(InetAddress
					.getByName(inHost.getHostIp()));
			if (nodeConnector == null) {
				logger.error("Cannot find host by ip: " + inHost.getHostIp());
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.build();
			}
			logger.info("found nodeConnector: %s-- %s", nodeConnector
					.getnodeconnectorNode().getNodeIDString(), nodeConnector
					.getnodeConnector().getNodeConnectorIDString());
			Match match = new Match();
			match.setField(MatchType.DL_SRC,
					nodeConnector.getDataLayerAddressBytes());
			match.setField(MatchType.IN_PORT, nodeConnector.getnodeConnector());
			ArrayList<Action> actions = new ArrayList<Action>();
			actions.add(new Controller());
			Flow flow = new Flow(match, actions);
			flow.setHardTimeout((short) 500);
			flow.setIdleTimeout((short) 500);
			flow.setPriority((short) 5);
			flow.setId(0xffL);
			Status status = flowProgrammer.addFlow(
					nodeConnector.getnodeconnectorNode(), flow);
			if (status.isSuccess()) {
				return Response.status(Response.Status.CREATED).build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.build();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	}

	@Path("/scagent/policyaction/BYOD_INIT/{id}")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@TypeHint(BYODInitConfig.class)
	@StatusCodes({ @ResponseCode(code = 200, condition = "operational"),
			@ResponseCode(code = 503, condition = "Internal error"),
			@ResponseCode(code = 503, condition = "misfunctional") })
	public Response createByodInit(@PathParam(value = "id") String param,
			@TypeHint(BYODInitConfig.class) BYODInitConfig config) {

		logger.info("received BYOD_INIT command = {}", config.toString());
		List<PolicyCommand> initCommands = generateBYODInitCommands(config);
		for (PolicyCommand p : initCommands) {
			processSingleFlowCommand(p);
		}
		return Response.status(Response.Status.OK).build();

	}

	public String processSingleFlowCommand(PolicyCommand policyCommand) {
		IFlowProgrammerService flowProgrammer = (IFlowProgrammerService) ServiceHelper
				.getGlobalInstance(IFlowProgrammerService.class, this);
		ISwitchManager switchManager = (ISwitchManager) ServiceHelper
				.getGlobalInstance(ISwitchManager.class, this);
		if (flowProgrammer == null || switchManager == null) {
			throw new ServiceUnavailableException(
					"SwitchManager or FlowProgrammer "
							+ RestMessages.SERVICEUNAVAILABLE.toString());
		}
		Node node = null;
		try {
			node = new Node("OF", policyCommand.getDpid());
		} catch (ConstructionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Match match = policyCommand.getMatch().toODLMatch();
		if (policyCommand.getInPort() != 0) {
			// should pass type NodeConnector
			Set<NodeConnector> upNodeConnectors = switchManager
					.getUpNodeConnectors(node);
			if (upNodeConnectors.isEmpty()) {
				logger.info("no node connectors for node :{}", node.getID());
			}
			boolean isFoundNodeConnector = false;
			for (NodeConnector nodeConnector : upNodeConnectors) {
				if ((short) nodeConnector.getID() == policyCommand.getInPort()) {
					isFoundNodeConnector = true;
					match.setField(MatchType.IN_PORT, nodeConnector);
					break;
				}
			}
			if (!isFoundNodeConnector) {
				throw new InternalServerErrorException(
						String.format(
								"cannot find NodeConnector with dpid: %d and inPort = %d",
								policyCommand.getDpid(),
								policyCommand.getInPort()));
			}
		}
		ArrayList<Action> actions = new ArrayList<Action>();
		if (policyCommand.getType() == PolicyActionType.ALLOW_FLOW
				|| policyCommand.getType() == PolicyActionType.BYOD_ALLOW) {
			actions.add(new Controller());
		}
		Flow flow = new Flow(match, actions);
		flow.setHardTimeout((short) policyCommand.getHardTimeout());
		flow.setIdleTimeout((short) policyCommand.getIdleTimeout());
		flow.setPriority((short) policyCommand.getCommandPriority());
		flow.setId(0xabcdeL);
		if (node == null) {
			throw new InternalServerErrorException(String.format(
					"cannot find Node with dpid: %d", policyCommand.getDpid()));
		}
		logger.info("adding flow with node = {} , flow = {}",
				node.getNodeIDString(), flow.toString());
		Status status = flowProgrammer.addFlow(node, flow);
		if (status.isSuccess()) {
			String flowModId = Cypher.getMD5(new String[] {
					node.getNodeIDString(), policyCommand.getInPort() + "",
					flow.toString() });
			Map<String, SwitchFlowModCount> sMap = switchFlowModCountMap
					.get(flowModId);

			if (sMap == null) {
				sMap = new HashMap<String, SwitchFlowModCount>();
				switchFlowModCountMap.put(policyCommand.getDpid(), sMap);
			}
			SwitchFlowModCount sfmc = sMap.get(flowModId);
			if (sfmc == null) {
				sfmc = new SwitchFlowModCount(policyCommand.getDpid(), flow, 1);
				sMap.put(flowModId, sfmc);
				globalFlowModCountMap.put(flowModId, sfmc);
			} else {
				sfmc.increaseCount();
			}
			ArrayList<String> flowModIds = new ArrayList<String>();
			flowModIds.add(flowModId);
			policyCommandsDeployed.put(policyCommand.getId(),
					new PolicyCommandDeployed(flowModId, policyCommand,
							new HashMap<String, PolicyCommandRelated>(),
							flowModIds));
			return flowModId;
		} else {
			throw new InternalServerErrorException(String.format(
					"cannot process flow command with id: %s",
					policyCommand.getId()));
		}
	}

	/**
	 * add flows to flow priority pattern action 1) 0 all controller 2) 1 inport
	 * drop 3) 2 inport,dhcp controller 4) 2 inport,dns controller 5) 2
	 * inport,arp controller 6) 2 inport,http controller
	 **/
	public List<PolicyCommand> generateBYODInitCommands(BYODInitConfig config) {
		ArrayList<PolicyCommand> initCommands = new ArrayList<PolicyCommand>();
		for (SwitchPort switchPort : config.getSwitchPorts()) {
			// priority=0 , inPort , controller
			// PolicyCommand(String id, String policyName,
			// int commandPriority, PolicyActionType type,
			// MatchArguments match, List<SecurityDevice> devices,
			// int idleTimeout, int hardTimeout, long dpid, short inPort);
			PolicyCommand controllerAllCommand = new PolicyCommand("ByodInit_"
					+ switchPort.getDpid() + ":" + switchPort.getInPort()
					+ "0_" + config.getId(), "controllerAllCommand", 0,
					PolicyActionType.ALLOW_FLOW, new MatchArguments(), null, 0,
					0, switchPort.getDpid(), (short) 0);
			initCommands.add(controllerAllCommand);
			// priority=1 , inport , drop
			MatchArguments inPortMatch = new MatchArguments();
			inPortMatch.setInputPort(switchPort.getInPort());
			PolicyCommand dropAllCommand = new PolicyCommand("ByodInit_"
					+ switchPort.getDpid() + ":" + switchPort.getInPort()
					+ "1_" + config.getId(), "dropAllCommand", 1,
					PolicyActionType.DROP_FLOW, inPortMatch, null, 0, 0,
					switchPort.getDpid(), switchPort.getInPort());
			initCommands.add(dropAllCommand);
			// allow arp, priorty = 2
			MatchArguments allowArpMatch = new MatchArguments();
			allowArpMatch.setDataLayerType((short) 0x0806);
			allowArpMatch.setInputPort(switchPort.getInPort());
			PolicyCommand allowArpCommand = new PolicyCommand("ByodInit_"
					+ switchPort.getDpid() + ":" + switchPort.getInPort()
					+ "2_" + config.getId(), "AllowArp", 2,
					PolicyActionType.ALLOW_FLOW, allowArpMatch, null, 0, 0,
					switchPort.getDpid(), switchPort.getInPort());
			initCommands.add(allowArpCommand);
			// allow dhcp, priorty = 2
			MatchArguments allowDhcpMatch = new MatchArguments();
			allowDhcpMatch.setDataLayerType((short) 0x0800);
			allowDhcpMatch.setNetworkProtocol((byte) 0x11);
			allowDhcpMatch.setTransportDestination((short) 67);
			allowArpMatch.setInputPort(switchPort.getInPort());
			PolicyCommand allowDhcpCommand = new PolicyCommand("ByodInit_"
					+ switchPort.getDpid() + ":" + switchPort.getInPort()
					+ "3_" + config.getId(), "AllowDHCP", 2,
					PolicyActionType.ALLOW_FLOW, allowDhcpMatch, null, 0, 0,
					switchPort.getDpid(), switchPort.getInPort());
			initCommands.add(allowDhcpCommand);

			// allow dns, priorty = 2
			MatchArguments allowDnsMatch = new MatchArguments();
			allowDnsMatch.setDataLayerType((short) 0x0800);
			allowDnsMatch.setNetworkProtocol((byte) 0x11);
			allowDnsMatch.setTransportDestination((short) 53);
			allowArpMatch.setInputPort(switchPort.getInPort());
			PolicyCommand allowDnsCommand = new PolicyCommand("ByodInit_"
					+ switchPort.getDpid() + ":" + switchPort.getInPort()
					+ "4_" + config.getId(), "AllowDns", 2,
					PolicyActionType.ALLOW_FLOW, allowDnsMatch, null, 0, 0,
					switchPort.getDpid(), switchPort.getInPort());
			initCommands.add(allowDnsCommand);

			// redirect tcp 80
			MatchArguments httpMatch = new MatchArguments();
			httpMatch.setDataLayerType((short) 0x0800);
			httpMatch.setNetworkProtocol((byte) 0x6);
			httpMatch.setTransportDestination((short) 80);
			allowArpMatch.setInputPort(switchPort.getInPort());
			PolicyCommand redirectHpptCommand = new PolicyCommand("ByodInit_"
					+ switchPort.getDpid() + ":" + switchPort.getInPort()
					+ "5_" + config.getId(), "redirectHttp", 2,
					PolicyActionType.ALLOW_FLOW, httpMatch, null, 0, 0,
					switchPort.getDpid(), switchPort.getInPort());
			initCommands.add(redirectHpptCommand);
		}
		return initCommands;
	}
}
