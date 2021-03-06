package org.opendaylight.controller.scagent.northbound;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonGenerator;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.opendaylight.controller.northbound.commons.RestMessages;
import org.opendaylight.controller.northbound.commons.exception.InternalServerErrorException;
import org.opendaylight.controller.northbound.commons.exception.ServiceUnavailableException;
import org.opendaylight.controller.northbound.commons.query.QueryContext;
import org.opendaylight.controller.northbound.commons.utils.NorthboundUtils;
import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.Controller;
import org.opendaylight.controller.sal.authorization.Privilege;
import org.opendaylight.controller.sal.core.ConstructionException;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.scagent.northbound.utils.*;
import org.opendaylight.controller.scagent.service.api.ISecurityControllerAgentService;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.ContextResolver;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import com.google.gson.Gson;

@Path("/")
public class SCAgentNorthbound {
    /**
     * SCAgent northbound
     */

    private String username;
    private QueryContext queryContext;

    @Context
    public void setQueryContext(ContextResolver<QueryContext> queryCtxResolver) {
        if (queryCtxResolver != null) {
            queryContext = queryCtxResolver.getContext(QueryContext.class);
        }
    }
    @Context
    public void setSecurityContext(SecurityContext context) {
        if (context != null && context.getUserPrincipal() != null) username = context.getUserPrincipal().getName();
    }
    protected String getUserName() {
        return username;
    }
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

    /*@Path("/scagent/{param}")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @TypeHint(FindHostsResult.class)
    @StatusCodes({@ResponseCode(code = 200, condition = "operational"),
            @ResponseCode(code = 503, condition = "Internal error"),
            @ResponseCode(code = 503, condition = "misfunctional")})
    public FindHostsResult scagentResource(
            @PathParam(value = "param") String param) {

        ISecurityControllerAgentService scAgentService = (ISecurityControllerAgentService) ServiceHelper
                .getGlobalInstance(ISecurityControllerAgentService.class, this);
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
            if (scAgentService == null) {
                // service not found.
                logger.info("no scagent service");
                return null;
            }
            String res = null;
            if ((res = scAgentService.sayHello(param)) != null) {
                logger.info(res);
            }

        }
        // return Response.ok(new String(param + " - something went wrong!"))
        // .status(503).build();
        return new FindHostsResult("none", (short) 0);
    }*/

    @Path("/scagent/policyaction/")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @TypeHint(Response.class)
    @StatusCodes({@ResponseCode(code = 200, condition = "operational"),
            @ResponseCode(code = 503, condition = "Internal error"),
            @ResponseCode(code = 503, condition = "misfunctional")})
    public Response getPolicies(@DefaultValue("") @QueryParam("type") String type,
                                @DefaultValue("") @QueryParam("id") String id) {
        logger.info("request to list policy type = {}, id = {}", type, id);
        if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
            return Response.ok(String.format(
                    "{\"status\" : \"ok\", \"result\" : \"%s\"}", "User is not authorized to perform this operation on container")).status(200).build();
        }
        ISecurityControllerAgentService scAgentService = (ISecurityControllerAgentService) ServiceHelper
                .getGlobalInstance(ISecurityControllerAgentService.class, this);
        Map<PolicyActionType, Map<String, PolicyCommand>> allPolicyCommands = scAgentService.getAllPolicyCommands();
        Gson gson = new Gson();
        if(type.equals("") && id.equals("")){
            ArrayList<PolicyCommand> plist = new ArrayList<>();
            for(Map<String, PolicyCommand> m : allPolicyCommands.values()){
                if(m!=null && m.size()>0){
                    plist.addAll(m.values());
                }
            }
            return Response.ok(String.format(
                    "{\"status\" : \"ok\", \"result\" : %s}", gson.toJson(plist))).status(200).build();
        }else if(!type.equals("")) {
            PolicyActionType pa;
            try {
                pa = PolicyActionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.error("type error: {}, no such policy type", type);
                return Response.ok(String.format("{\"status\" : \"error\", \"result\" : \"%s\"}", "policy type error")).status(503).build();
            }

            Map<String, PolicyCommand> typePolicies = allPolicyCommands.get(pa);
            if(id.equals("")) {
                Collection<PolicyCommand> values = new ArrayList<>();
                if (typePolicies != null) {
                    values.addAll(typePolicies.values());
                }
                return Response.ok(String.format(
                        "{\"status\" : \"ok\", \"result\" : %s}", gson.toJson(values))).status(200).build();
            }else{
                if(typePolicies != null)
                    return Response.ok(String.format(
                            "{\"status\" : \"ok\", \"result\" : %s}", gson.toJson(typePolicies.get(id)))).status(200).build();
                else
                    return Response.ok(String.format("{\"status\" : \"error\", \"result\" : %s}", "null")).status(503).build();
            }
        }else{
            return Response.ok(String.format("{\"status\" : \"error\", \"result\" : %s}", "\"input error\"")).status(503).build();
        }
    }

    @Path("/scagent/policyaction/{id}")
    @DELETE
    @StatusCodes({@ResponseCode(code = 204, condition = "operational"),
            @ResponseCode(code = 503, condition = "Internal error"),
            @ResponseCode(code = 503, condition = "misfunctional")})
    public Response delPolicyByTypeAndId(
            @PathParam(value = "id") String id) {
        logger.info("request to delete policy id = {}",  id);
        if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
            return Response.ok(String.format(
                    "{\"status\" : \"ok\", \"result\" : \"%s\"}", "User is not authorized to perform this operation on container")).status(200).build();
        }
        ISecurityControllerAgentService scAgentService = (ISecurityControllerAgentService) ServiceHelper
                .getGlobalInstance(ISecurityControllerAgentService.class, this);
        Map<PolicyActionType, Map<String, PolicyCommand>> allPolicyCommands = scAgentService.getAllPolicyCommands();
        PolicyCommand removed = null;
        for(Map<String, PolicyCommand> typePolicies:allPolicyCommands.values()){
            if(typePolicies!=null){
                removed = typePolicies.remove(id);
                if(removed !=null)
                    break;
            }
        }
        Gson gson = new Gson();
        if (removed != null) {
            return Response.ok(String.format(
                    "{\"status\" : \"ok\", \"result\" : %s}", gson.toJson(removed))).status(200).build();
        }
        return Response.ok(String.format(
                "{\"status\" : \"error\", \"result\" : %s}", "\"no such policy\"")).status(200).build();
    }
    /*@Path("/scagent/policyaction/{format}")
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @TypeHint(InputHost.class)
    @StatusCodes({@ResponseCode(code = 200, condition = "operational"),
            @ResponseCode(code = 503, condition = "Internal error"),
            @ResponseCode(code = 503, condition = "misfunctional")})
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
    }*/

    @Path("/scagent/policyaction/{id}")
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @TypeHint(BYODInitConfig.class)
    @StatusCodes({@ResponseCode(code = 200, condition = "operational"),
            @ResponseCode(code = 503, condition = "Internal error"),
            @ResponseCode(code = 503, condition = "misfunctional")})
    public Response createByodInit(@PathParam(value = "id") String param,
                                   @TypeHint(BYODInitConfig.class) BYODInitConfig config) {

        logger.info("received command id = {}", config.toString());
        if (!NorthboundUtils.isAuthorized(getUserName(), "default", Privilege.WRITE, this)) {
            return Response.ok(String.format(
                    "{\"status\" : \"ok\", \"result\" : \"%s\"}", "User is not authorized to perform this operation on container")).status(200).build();
        }
        ISecurityControllerAgentService scAgentService = (ISecurityControllerAgentService) ServiceHelper
                .getGlobalInstance(ISecurityControllerAgentService.class, this);
        if(scAgentService == null){
            return Response.ok(String.format(
                    "{\"status\" : \"error\", \"result\" : \"%s\"}", "cannot get scAgentService")).status(200).build();
        }
        PolicyCommand resCommand = scAgentService.addPolicyCommand(config.toPolicyCommand());
        if(resCommand != null){
            return Response.ok(String.format(
                    "{\"status\" : \"error\", \"result\" : \"%s\"}", "policy already exists.")).status(200).build();
        }
        String res = null;
        if(config.getType() == PolicyActionType.BYOD_INIT){
            List<PolicyCommand> initCommands = generateBYODInitCommands(config);
            HashMap<String, String> resMap = new HashMap<>();
            boolean error = false;
            for (PolicyCommand p : initCommands) {
                String s = processSingleFlowCommand(p);
                if (s == "error") {
                    error = true;
                }
                resMap.put(p.getPolicyName(), s);
            }
            JsonFactory factory = new JsonFactory();
            StringWriter writer = new StringWriter();
            try {
                JsonGenerator generator = factory.createGenerator(writer);
                generator.writeStartObject();
                generator.writeStringField("status", error ? "error" : "ok");
                generator.writeArrayFieldStart("result");
                for (Map.Entry<String, String> name : resMap.entrySet()) {
                    generator.writeStartObject();
                    generator.writeStringField(name.getKey(), name.getValue());
                    generator.writeEndObject();
                }
                generator.writeEndArray();
                generator.writeEndObject();
                generator.close();
                res = writer.toString();
                logger.info(res);
            } catch (IOException e) {
                res = "{\"status\" : \"error\", \"result\" : [\"json conversion failed. \"]}";
                e.printStackTrace();
            }
        }else if(config.getType() == PolicyActionType.BYOD_ALLOW){
            res = "{\"status\" : \"ok\", \"result\" : \"allow policy added.\"}";
            System.out.println(config);
        }
        return Response.ok(new String(res)).status(Response.Status.OK).build();

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
                String warn = String.format("{\"status\":\"error\",\"result\":[\"cannot find NodeConnector with dpid: %d and inPort = %d\"]}",
                        policyCommand.getDpid(), policyCommand.getInPort());
                logger.error(warn);
                throw new InternalServerErrorException(warn);
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
                    "{\"status\":\"error\",\"result\":[\"cannot find Node with dpid: %d\"]}", policyCommand.getDpid()));
        }
        logger.info("adding flow with node = {} , flow = {}",
                node.getNodeIDString(), flow.toString());
        Status status = flowProgrammer.addFlow(node, flow);
        String res;
        if (status.isSuccess()) {
            String flowModId = Cypher.getMD5(new String[]{
                    node.getNodeIDString(), policyCommand.getInPort() + "",
                    flow.toString()});
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
            res = flowModId;
        } else {
            res = "error";
            throw new InternalServerErrorException(String.format(
                    "{\"status\":\"error\",\"result\":\"cannot process flow with id: %s\"}",
                    policyCommand.getId()));
        }
        return res;
    }

    /**
     * add flows to flow priority pattern action 1) 0 all controller 2) 1 inport
     * drop 3) 2 inport,dhcp controller 4) 2 inport,dns controller 5) 2
     * inport,arp controller 6) 2 inport,http controller
     */
    public List<PolicyCommand> generateBYODInitCommands(BYODInitConfig config) {
        ArrayList<PolicyCommand> initCommands = new ArrayList<PolicyCommand>();
            // priority=0 , inPort , controller
            // PolicyCommand(String id, String policyName,
            // int commandPriority, PolicyActionType type,
            // MatchArguments match, List<SecurityDevice> devices,
            // int idleTimeout, int hardTimeout, long dpid, short inPort);
            PolicyCommand controllerAllCommand = new PolicyCommand("ByodInit_"
                    + config.getDpid() + ":" + config.getInPort()
                    + "0_" + config.getId(), "controllerAllCommand", (short)0,
                    PolicyActionType.ALLOW_FLOW, new MatchArguments(), null, (short)0,
                    (short)0, config.getDpid(), (short) 0);
            initCommands.add(controllerAllCommand);
            // priority=1 , inport , drop
            MatchArguments inPortMatch = new MatchArguments();
            inPortMatch.setInputPort(config.getInPort());
            PolicyCommand dropAllCommand = new PolicyCommand("ByodInit_"
                    + config.getDpid() + ":" + config.getInPort()
                    + "1_" + config.getId(), "dropAllCommand", (short)1,
                    PolicyActionType.DROP_FLOW, inPortMatch, null, (short)0, (short)0,
                    config.getDpid(), config.getInPort());
            initCommands.add(dropAllCommand);
            // allow arp, priorty = 2
            MatchArguments allowArpMatch = new MatchArguments();
            allowArpMatch.setDataLayerType((short) 0x0806);
            allowArpMatch.setInputPort(config.getInPort());
            PolicyCommand allowArpCommand = new PolicyCommand("ByodInit_"
                    + config.getDpid() + ":" + config.getInPort()
                    + "2_" + config.getId(), "AllowArp", (short)2,
                    PolicyActionType.ALLOW_FLOW, allowArpMatch, null, (short)0,(short) 0,
                    config.getDpid(), config.getInPort());
            initCommands.add(allowArpCommand);
            // allow dhcp, priorty = 2
            MatchArguments allowDhcpMatch = new MatchArguments();
            allowDhcpMatch.setDataLayerType((short) 0x0800);
            allowDhcpMatch.setNetworkProtocol((byte) 0x11);
            allowDhcpMatch.setTransportDestination((short) 67);
            allowArpMatch.setInputPort(config.getInPort());
            PolicyCommand allowDhcpCommand = new PolicyCommand("ByodInit_"
                    + config.getDpid() + ":" + config.getInPort()
                    + "3_" + config.getId(), "AllowDHCP", (short)2,
                    PolicyActionType.ALLOW_FLOW, allowDhcpMatch, null, (short)0,(short) 0,
                    config.getDpid(), config.getInPort());
            initCommands.add(allowDhcpCommand);

            // allow dns, priorty = 2
            MatchArguments allowDnsMatch = new MatchArguments();
            allowDnsMatch.setDataLayerType((short) 0x0800);
            allowDnsMatch.setNetworkProtocol((byte) 0x11);
            allowDnsMatch.setTransportDestination((short) 53);
            allowArpMatch.setInputPort(config.getInPort());
            PolicyCommand allowDnsCommand = new PolicyCommand("ByodInit_"
                    + config.getDpid() + ":" + config.getInPort()
                    + "4_" + config.getId(), "AllowDns", (short)2,
                    PolicyActionType.ALLOW_FLOW, allowDnsMatch, null, (short)0, (short)0,
                    config.getDpid(), config.getInPort());
            initCommands.add(allowDnsCommand);

            // redirect tcp 80
            MatchArguments httpMatch = new MatchArguments();
            httpMatch.setDataLayerType((short) 0x0800);
            httpMatch.setNetworkProtocol((byte) 0x6);
            httpMatch.setTransportDestination((short) 80);
            allowArpMatch.setInputPort(config.getInPort());
            PolicyCommand redirectHpptCommand = new PolicyCommand("ByodInit_"
                    + config.getDpid() + ":" + config.getInPort()
                    + "5_" + config.getId(), "redirectHttp", (short)2,
                    PolicyActionType.ALLOW_FLOW, httpMatch, null, (short)0,(short) 0,
                    config.getDpid(), config.getInPort());
            initCommands.add(redirectHpptCommand);
        return initCommands;
    }
}
