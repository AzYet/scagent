package org.opendaylight.controller.scagent.northbound.utils;

import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyCommand  implements Comparable<PolicyCommand>{
	protected String id;
	protected String policyName;
	protected short commandPriority;
	protected PolicyActionType type;
	protected MatchArguments match;
	protected List<SecurityDevice> devices;
	protected short idleTimeout;
	protected short hardTimeout;
	protected long dpid;
	protected short inPort;

	public PolicyCommand(String policyCommandId, String policyName,
                         short commandPriority, PolicyActionType type, MatchArguments match,
			List<SecurityDevice> devices) {
		super();
		this.id = policyCommandId;
		this.policyName = policyName;
		this.commandPriority = commandPriority;
		this.type = type;
		this.match = match;
		this.devices = devices;
	}

	public PolicyCommand(String id, String policyName, short commandPriority,
			PolicyActionType type, MatchArguments match,
			List<SecurityDevice> devices, short idleTimeout, short hardTimeout) {
		super();
		this.id = id;
		this.policyName = policyName;
		this.commandPriority = commandPriority;
		this.type = type;
		this.match = match;
		this.devices = devices;
		this.idleTimeout = idleTimeout;
		this.hardTimeout = hardTimeout;
	}

	public PolicyCommand(String id, String policyName, short commandPriority,
			PolicyActionType type, MatchArguments match,
			List<SecurityDevice> devices, short idleTimeout, short hardTimeout,
			long dpid, short inPort) {
		super();
		this.id = id;
		this.policyName = policyName;
		this.commandPriority = commandPriority;
		this.type = type;
		this.match = match;
		this.devices = devices;
		this.idleTimeout = idleTimeout;
		this.hardTimeout = hardTimeout;
		this.dpid = dpid;
		this.inPort = inPort;
	}

	/*public static List<? extends PolicyCommand> fromJson(JsonNode rootNode, PolicyActionType type) {
		ArrayList<PolicyCommand> pmList = new ArrayList<PolicyCommand>();
		if(type == PolicyActionType.REDIRECT_FLOW){
			JsonNode commandlistNode=rootNode.path("commandlist");
			Iterator<JsonNode> commandlist = commandlistNode.elements();
			while (commandlist.hasNext()) { //对应一个PolicyCommand
				JsonNode flowCommandNode = commandlist.next();
				PolicyCommand policyCommand = new PolicyCommand();
				policyCommand.setType(type);
				policyCommand.setId(flowCommandNode.path("id").asText());
				int policyPriority = flowCommandNode.path("commandPriority").asInt();
				policyCommand.setCommandPriority(policyPriority);
				policyCommand.setHardTimeout(flowCommandNode.path("hardTimeout").asInt());
				policyCommand.setIdleTimeout(flowCommandNode.path("idleTimeout").asInt());

				String commandName = flowCommandNode.path("commandName").asText();
				policyCommand.setPolicyName(commandName);
				MatchArguments ma = new MatchArguments();
				ma.fromJson(flowCommandNode.path("matchArguments"));
				policyCommand.setMatch(ma);
				JsonNode devicesNode=flowCommandNode.path("devices");
				Iterator<JsonNode> deviceList = devicesNode.elements();
				List<SecurityDevice> secDeviceList = new ArrayList<SecurityDevice>();
				while (deviceList.hasNext()) { //一个device
					JsonNode deviceNode = deviceList.next();
					SecurityDevice device = new SecurityDevice();
					device.fromJson(deviceNode);
					secDeviceList.add(device);
				}
				policyCommand.setDevices(secDeviceList);
				pmList.add(policyCommand);
			}
		}else if(type == PolicyActionType.DROP_FLOW || type == PolicyActionType.ALLOW_FLOW
				|| type == PolicyActionType.BYOD_ALLOW){
			JsonNode commandlistNode=rootNode.path("commandlist");
			Iterator<JsonNode> commandlist = commandlistNode.elements();
			while (commandlist.hasNext()) { //对应一个PolicyCommand
				JsonNode flowCommandNode = commandlist.next();
				PolicyCommand policyCommand = new PolicyCommand();
				policyCommand.setType(type);
				policyCommand.setId(flowCommandNode.path("id").asText());
				int policyPriority = flowCommandNode.path("commandPriority").asInt();
				policyCommand.setCommandPriority(policyPriority);
				policyCommand.setHardTimeout(flowCommandNode.path("hardTimeout").asInt());
				policyCommand.setIdleTimeout(flowCommandNode.path("idleTimeout").asInt());
				policyCommand.setInPort((short) flowCommandNode.path("inPort").asInt());
				policyCommand.setDpid(flowCommandNode.path("dpid").asLong());
				String commandName = flowCommandNode.path("commandName").asText();
				policyCommand.setPolicyName(commandName);
				MatchArguments ma = new MatchArguments();
				ma.fromJson(flowCommandNode.path("matchArguments"));
				policyCommand.setMatch(ma);
				pmList.add(policyCommand);
			}
		}else if(type == PolicyActionType.RESTORE_REDIRECT_FLOW 
				|| type == PolicyActionType.RESTORE_ALLOW_FLOW
				|| type == PolicyActionType.RESTORE_BYOD_ALLOW
				|| type == PolicyActionType.RESTORE_DROP_FLOW){
			JsonNode commandlistNode=rootNode.path("commandlist");
			Iterator<JsonNode> commandlist = commandlistNode.elements();
			while (commandlist.hasNext()) { //对应一个PolicyCommand
				JsonNode flowCommandNode = commandlist.next();
				PolicyCommandDeployed policyCommandDeployed = SecurityControllerAgentResource.policyCommandsDeployed.get(flowCommandNode.path("id").asText());
				if(policyCommandDeployed != null)
					pmList.add(policyCommandDeployed.getPolicyCommand());
			}
		}else if(type == PolicyActionType.BYOD_INIT ){
			JsonNode commandlistNode=rootNode.path("commandlist");
			Iterator<JsonNode> commandlist = commandlistNode.elements();
			while (commandlist.hasNext()) { //对应一个PolicyCommand
				JsonNode flowCommandNode = commandlist.next();
				BYODRedirectCommand policyCommand = new BYODRedirectCommand();
				policyCommand.setType(type);
				policyCommand.setId(flowCommandNode.path("id").asText());
				int policyPriority = flowCommandNode.path("commandPriority").asInt();
				policyCommand.setCommandPriority(policyPriority);
				policyCommand.setHardTimeout(flowCommandNode.path("hardTimeout").asInt());
				policyCommand.setHardTimeout(flowCommandNode.path("hardTimeout").asInt());
				policyCommand.setDpid(flowCommandNode.path("dpid").asLong());
				policyCommand.setInPort((short) flowCommandNode.path("inPort").asInt());
				policyCommand.setNetwork(flowCommandNode.path("network").asText());
				policyCommand.setMask((byte) flowCommandNode.path("mask").asInt());
				policyCommand.setServerIp(flowCommandNode.path("serverIp").asText());
				policyCommand.setServerMac(flowCommandNode.path("serverMac").asText());
				String commandName = flowCommandNode.path("commandName").asText();
				policyCommand.setPolicyName(commandName);

				pmList.add(policyCommand);
			}
		}else if(type == PolicyActionType.RESTORE_BYOD_INIT){
			JsonNode commandlistNode=rootNode.path("commandlist");
			Iterator<JsonNode> commandlist = commandlistNode.elements();
			while (commandlist.hasNext()) { //对应一个PolicyCommand
				JsonNode flowCommandNode = commandlist.next();
				for(int i = 0 ; i <6 ; i++){
					String policyCommandId = String.format("ByodInit_%d_%s", i,flowCommandNode.path("id").asText());
					PolicyCommandDeployed policyCommandDeployed = SecurityControllerAgentResource.policyCommandsDeployed.get(policyCommandId);
					if(policyCommandDeployed != null)
						pmList.add(policyCommandDeployed.getPolicyCommand());
				}
			}
		}
		return pmList;
	}*/

	public PolicyCommand() {
		super();
	}

	public String getId() {
		return id;
	}
	public void setId(String policyCommandId) {
		this.id = policyCommandId;
	}
	public String getPolicyName() {
		return policyName;
	}
	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}
	public short getCommandPriority() {
		return commandPriority;
	}
	public void setCommandPriority(short commandPriority) {
		this.commandPriority = commandPriority;
	}
	public PolicyActionType getType() {
		return type;
	}
	public void setType(PolicyActionType type) {
		this.type = type;
	}
	public MatchArguments getMatch() {
		return match;
	}
	public void setMatch(MatchArguments match) {
		this.match = match;
	}

	public List<SecurityDevice> getDevices() {
		return devices;
	}

	public void setDevices(List<SecurityDevice> devices) {
		this.devices = devices;
	}

	public short getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(short idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public short getHardTimeout() {
		return hardTimeout;
	}

	public void setHardTimeout(short hardTimeout) {
		this.hardTimeout = hardTimeout;
	}


	@Override
	public String toString() {
		return "PolicyCommand [id=" + id + ", policyName=" + policyName
				+ ", commandPriority=" + commandPriority + ", type=" + type
				+ ", match=" + match + ", devices=" + devices
				+ ", idleTimeout=" + idleTimeout + ", hardTimeout="
				+ hardTimeout + "]";
	}

	@Override
	public int compareTo(PolicyCommand o) {
		return commandPriority == o.commandPriority ? 0 :(commandPriority > o.commandPriority ? 1 : -1);
	}

	public short getInPort() {
		return inPort;
	}

	public void setInPort(short inPort) {
		this.inPort = inPort;
	}

	public long getDpid() {
		return dpid;
	}

	public void setDpid(long swIPAddress) {
		this.dpid = swIPAddress;
	}
}
