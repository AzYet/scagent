package org.opendaylight.controller.scagent.northbound.utils;

import java.util.List;

import org.opendaylight.controller.scagent.northbound.utils.PolicyActionType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BYODInitConfig {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SwitchPort {

		private long dpid;
		private short inPort;

		public SwitchPort() {
			super();
		}

		public long getDpid() {
			return dpid;
		}

		public void setDpid(long dpid) {
			this.dpid = dpid;
		}

		public short getInPort() {
			return inPort;
		}

		public void setInPort(short inPort) {
			this.inPort = inPort;
		}

		@Override
		public String toString() {
			return "SwitchPorts [dpid=" + dpid + ", inPort=" + inPort + "]";
		}
	}

	private String id;
	private String commandName;
	private PolicyActionType type;
	private List<SwitchPort> switchPorts;
	private String serverIp;
	private String serverMac;
	private String network;
	private int mask;
	private short commandPriority;
	private int idleTimeout;
	private int hardTimeout;

	public BYODInitConfig() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	public PolicyActionType getType() {
		return type;
	}

	public void setType(PolicyActionType type) {
		this.type = type;
	}

	public List<SwitchPort> getSwitchPorts() {
		return switchPorts;
	}

	public void setSwitchPorts(List<SwitchPort> switchPorts) {
		this.switchPorts = switchPorts;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public String getServerMac() {
		return serverMac;
	}

	public void setServerMac(String serverMac) {
		this.serverMac = serverMac;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public int getMask() {
		return mask;
	}

	public void setMask(int mask) {
		this.mask = mask;
	}

	public short getCommandPriority() {
		return commandPriority;
	}

	public void setCommandPriority(short commandPriority) {
		this.commandPriority = commandPriority;
	}

	public int getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(int idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public int getHardTimeout() {
		return hardTimeout;
	}

	public void setHardTimeout(int hardTimeout) {
		this.hardTimeout = hardTimeout;
	}

	@Override
	public String toString() {
		return "BYODInitConfig [id=" + id + ", commandName=" + commandName
				+ ", switchPorts=" + switchPorts + ", serverIp=" + serverIp
				+ ", serverMac=" + serverMac + ", network=" + network
				+ ", mask=" + mask + ", commandPriority=" + commandPriority
				+ ", idleTimeout=" + idleTimeout + ", hardTimeout="
				+ hardTimeout + "]";
	}
}
