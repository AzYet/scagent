package org.opendaylight.controller.scagent.northbound.utils;

public class FindHostsResult {
	private String dpid;
	private short portNum;
	public FindHostsResult(String dpid, short portNum) {
		super();
		this.dpid = dpid;
		this.portNum = portNum;
	}
	public String getDpid() {
		return dpid;
	}
	public void setDpid(String dpid) {
		this.dpid = dpid;
	}
	public short getPortNum() {
		return portNum;
	}
	public void setPortNum(short portNum) {
		this.portNum = portNum;
	}
}
