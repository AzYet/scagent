package org.opendaylight.controller.scagent.northbound.utils;

import java.util.List;

import org.opendaylight.controller.scagent.northbound.utils.PolicyActionType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BYODInitConfig {

	private String id;
	private String commandName;
	private PolicyActionType type;
    private long dpid;
    private short inPort;
	private String serverIp;
	private String serverMac;
	private String network;
	private byte mask;
	private short commandPriority;
	private short idleTimeout;
	private short hardTimeout;

    public BYODRedirectCommand toByodRedirectCommand(){
        BYODRedirectCommand byodRedirectCommand = new BYODRedirectCommand(
                id,commandName,commandPriority,type,null,null,idleTimeout,hardTimeout,dpid,
                inPort,network,mask,serverIp,serverMac);
    return byodRedirectCommand;
    }

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

	public byte getMask() {
		return mask;
	}

	public void setMask(byte mask) {
		this.mask = mask;
	}

	public short getCommandPriority() {
		return commandPriority;
	}

	public void setCommandPriority(short commandPriority) {
		this.commandPriority = commandPriority;
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
		return "BYODInitConfig [id=" + id + ", commandName=" + commandName
				+  ", serverIp=" + serverIp
				+ ", serverMac=" + serverMac + ", network=" + network
				+ ", mask=" + mask + ", commandPriority=" + commandPriority
				+ ", idleTimeout=" + idleTimeout + ", hardTimeout="
				+ hardTimeout + "]";
	}
}
