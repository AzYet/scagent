package org.opendaylight.controller.scagent.northbound.utils;

import java.util.List;

import org.opendaylight.controller.scagent.northbound.utils.PolicyActionType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.openflow.util.HexString;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BYODInitConfig {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MatchConfig{
        protected int wildcards;
        protected short inputPort;
        protected String dataLayerSource;
        protected String dataLayerDestination;
        protected short dataLayerVirtualLan;
        protected byte dataLayerVirtualLanPriorityCodePoint;
        protected short dataLayerType;
        protected byte networkTypeOfService;
        protected byte networkProtocol;
        protected int networkSource;
        protected int networkDestination;
        protected short transportSource;
        protected short transportDestination;

        public MatchArguments toMatchArguments(){
            return new MatchArguments(wildcards,inputPort, HexString.fromHexString(dataLayerSource),HexString.fromHexString(dataLayerDestination),
                    dataLayerVirtualLan,dataLayerVirtualLanPriorityCodePoint,dataLayerType,
                    networkTypeOfService,networkProtocol,networkSource ,networkDestination ,transportSource ,
                    transportDestination);
        }

        public int getWildcards() {
            return wildcards;
        }

        public void setWildcards(int wildcards) {
            this.wildcards = wildcards;
        }

        public short getInputPort() {
            return inputPort;
        }

        public void setInputPort(short inputPort) {
            this.inputPort = inputPort;
        }

        public String getDataLayerSource() {
            return dataLayerSource;
        }

        public void setDataLayerSource(String dataLayerSource) {
            this.dataLayerSource = dataLayerSource;
        }

        public String getDataLayerDestination() {
            return dataLayerDestination;
        }

        public void setDataLayerDestination(String dataLayerDestination) {
            this.dataLayerDestination = dataLayerDestination;
        }

        public short getDataLayerVirtualLan() {
            return dataLayerVirtualLan;
        }

        public void setDataLayerVirtualLan(short dataLayerVirtualLan) {
            this.dataLayerVirtualLan = dataLayerVirtualLan;
        }

        public byte getDataLayerVirtualLanPriorityCodePoint() {
            return dataLayerVirtualLanPriorityCodePoint;
        }

        public void setDataLayerVirtualLanPriorityCodePoint(byte dataLayerVirtualLanPriorityCodePoint) {
            this.dataLayerVirtualLanPriorityCodePoint = dataLayerVirtualLanPriorityCodePoint;
        }

        public short getDataLayerType() {
            return dataLayerType;
        }

        public void setDataLayerType(short dataLayerType) {
            this.dataLayerType = dataLayerType;
        }

        public byte getNetworkTypeOfService() {
            return networkTypeOfService;
        }

        public void setNetworkTypeOfService(byte networkTypeOfService) {
            this.networkTypeOfService = networkTypeOfService;
        }

        public byte getNetworkProtocol() {
            return networkProtocol;
        }

        public void setNetworkProtocol(byte networkProtocol) {
            this.networkProtocol = networkProtocol;
        }

        public int getNetworkSource() {
            return networkSource;
        }

        public void setNetworkSource(int networkSource) {
            this.networkSource = networkSource;
        }

        public int getNetworkDestination() {
            return networkDestination;
        }

        public void setNetworkDestination(int networkDestination) {
            this.networkDestination = networkDestination;
        }

        public short getTransportSource() {
            return transportSource;
        }

        public void setTransportSource(short transportSource) {
            this.transportSource = transportSource;
        }

        public short getTransportDestination() {
            return transportDestination;
        }

        public void setTransportDestination(short transportDestination) {
            this.transportDestination = transportDestination;
        }
    }

    private String id;
    private String policyId;
	private String commandName;
	private PolicyActionType type;
    private long dpid;
    private short inPort;
	private String serverIp;
	private String serverMac;
	private String network;
	private byte mask;
    private MatchConfig matchArguments ;
	private short commandPriority;
	private short idleTimeout;
	private short hardTimeout;
    protected List<SecurityDevice> devices;
/*    {
        "id": "62c27921d44c351005366188bfbbb9bb",
            "type": "BYOD_ALLOW",
            "commandName": null,
            "commandPriority": 5,
            "matchArguments": {
        "wildcards": 0,
                "inputPort": 0,
                "dataLayerSource": "24:69:a5:5e:ae:52",
                "dataLayerDestination": "00:00:00:00:00:00",
                "dataLayerVirtualLan": 0,
                "dataLayerVirtualLanPriorityCodePoint": 0,
                "dataLayerType": 0,
                "networkTypeOfService": 0,
                "networkProtocol": 0,
                "networkSource": 0,
                "networkDestination": 0,
                "transportSource": 0,
                "transportDestination": 0
    },
        "idleTimeout": 0,
            "hardTimeout": 0,
            "dpid": 128983852086,
            "inPort": 4
    }*/

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

    public String getPolicyId() {
        return policyId;
    }

    public MatchConfig getMatchArguments() {
        return matchArguments;
    }

    public void setMatchArguments(MatchConfig matchArguments) {
        this.matchArguments = matchArguments;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public List<SecurityDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<SecurityDevice> devices) {
        this.devices = devices;
    }

    public PolicyCommand toPolicyCommand(){
        if(type==PolicyActionType.BYOD_INIT) {
            return new BYODRedirectCommand(
                    id, commandName, commandPriority, type, null, null,
                    idleTimeout, hardTimeout, dpid, inPort, network, mask, serverIp, serverMac);
        }else{
            return new PolicyCommand(
                    id,commandName,commandPriority ,
                    type ,matchArguments.toMatchArguments(),
                    devices ,idleTimeout ,hardTimeout ,
                    dpid ,inPort);
        }
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
