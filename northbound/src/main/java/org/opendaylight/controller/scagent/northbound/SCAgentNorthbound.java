package org.opendaylight.controller.scagent.northbound;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.opendaylight.controller.northbound.commons.exception.ServiceUnavailableException;
import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.Controller;
import org.opendaylight.controller.sal.core.Edge;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.sal.routing.IRouting;
import org.opendaylight.controller.sal.utils.HexEncode;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.scagent.service.api.SCAgentSampleServiceAPI;
import org.opendaylight.controller.topologymanager.ITopologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.NotFoundException;


@Path("/")
public class SCAgentNorthbound {
	/**
	 * SCAgent northbound
	 */
	public static Logger logger = LoggerFactory
			.getLogger(SCAgentNorthbound.class);

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
	public Response postHandler(@PathParam(value = "format") String param,
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
				logger.error("Cannot find host by ip: "
						+ inHost.getHostIp());
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
}
