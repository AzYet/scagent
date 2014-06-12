package org.opendaylight.controller.scagent.northbound;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.opendaylight.controller.sal.core.Edge;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.scagent.service.api.SCAgentSampleServiceAPI;
import org.opendaylight.controller.topologymanager.ITopologyManager;

@Path("/")
public class SCAgentNorthbound {

	/**
	 * Ping test
	 */
	@Path("/scagent/{ipAddress}")
	@GET
	@StatusCodes({
			@ResponseCode(code = 200, condition = "Destination reachable"),
			@ResponseCode(code = 503, condition = "Internal error"),
			@ResponseCode(code = 503, condition = "Destination unreachable") })
	public Response scagent(@PathParam(value = "ipAddress") String param) {

		SCAgentSampleServiceAPI scSmplService = (SCAgentSampleServiceAPI) ServiceHelper
				.getGlobalInstance(SCAgentSampleServiceAPI.class, this);
		// IRouting routingService = (IRouting) ServiceHelper.getGlobalInstance(
		// IRouting.class, this);
		// ITopologyService topoService = (ITopologyService)
		// ServiceHelper.getGlobalInstance(
		// ITopologyService.class, this);
		ITopologyManager topologyManager = (ITopologyManager) ServiceHelper
				.getGlobalInstance(ITopologyManager.class, this);
		Map<Node, Set<Edge>> nodeEdges;
		String resStr = "";
		if (param.equalsIgnoreCase("nodes")) {
			nodeEdges = topologyManager.getNodeEdges();

			for (Node n : nodeEdges.keySet()) {
				resStr += n.getNodeIDString() + "; ";
			}
			return Response.ok(new String(param + ": " + resStr)).status(503)
					.build();
		} else {
			if (scSmplService == null) {

				// Ping service not found.
				return Response.ok(new String("No scagent service"))
						.status(500).build();
			}
			String res = null;
			if ((res = scSmplService.sayHello(param)) != null)
				return Response.ok(new String(res)).build();

		}
		return Response.ok(new String(param + " - something went wrong!"))
				.status(503).build();
	}

}
