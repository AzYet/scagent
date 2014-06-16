package org.opendaylight.controller.scagent.northbound;

public class InputHost {
	private String hostMac;
	private String hostIp;
	
	public InputHost() {
		super();
	}
	public InputHost(String hostMac, String hostIp) {
		super();
		this.hostMac = hostMac;
		this.hostIp = hostIp;
	}
	public String getHostMac() {
		return hostMac;
	}
	public void setHostMac(String hostMac) {
		this.hostMac = hostMac;
	}
	public String getHostIp() {
		return hostIp;
	}
	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}
}
