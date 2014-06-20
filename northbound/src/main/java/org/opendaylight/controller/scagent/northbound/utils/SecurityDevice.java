package org.opendaylight.controller.scagent.northbound.utils;

import org.openflow.util.HexString;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityDevice {

	protected String deviceKey;
	protected String deviceName;
	protected int deviceTag;
	protected AttachmentPointInfo ingressAttachmentPointInfo;
	protected AttachmentPointInfo outgressAttachmentPointInfo;
	
	public SecurityDevice() {
		super();
	}

	public SecurityDevice(String deviceKey, String deviceName, int deviceTag,
			AttachmentPointInfo ingressAttachmentPointInfo, AttachmentPointInfo outgressAttachmentPointInfo) {
		super();
		this.deviceKey = deviceKey;
		this.deviceName = deviceName;
		this.deviceTag = deviceTag;
		this.ingressAttachmentPointInfo = ingressAttachmentPointInfo;
		this.outgressAttachmentPointInfo = outgressAttachmentPointInfo;
	}
	
	public void fromJson(JsonNode deviceNode){
		setDeviceKey(deviceNode.path("deviceid").asText());
		setDeviceTag(deviceNode.path("tag").asInt());
		if(deviceNode.hasNonNull("ingress")){
			JsonNode inAPNode = deviceNode.path("ingress");
			AttachmentPointInfo attachmentPointInfo = new AttachmentPointInfo();
			if(inAPNode.hasNonNull("mac"))
				attachmentPointInfo.setMac(HexString.fromHexString(inAPNode.path("mac").asText()));
			if(inAPNode.hasNonNull("ap"))
				attachmentPointInfo.setAttchmentPoint(inAPNode.path("ap").asText());
			setIngressAttachmentPointInfo(attachmentPointInfo);
		}
		if(deviceNode.hasNonNull("outgress")){
			JsonNode outAPNode = deviceNode.path("outgress");
			AttachmentPointInfo attachmentPointInfo = new AttachmentPointInfo();
			if(outAPNode.hasNonNull("mac"))
				attachmentPointInfo.setMac(HexString.fromHexString(outAPNode.path("mac").asText()));
			if(outAPNode.hasNonNull("ap"))
				attachmentPointInfo.setAttchmentPoint(outAPNode.path("ap").asText());
			setOutgressAttachmentPointInfo(attachmentPointInfo);
		}
	}

	public String getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(String deviceKey) {
		this.deviceKey = deviceKey;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public int getDeviceTag() {
		return deviceTag;
	}

	public void setDeviceTag(int deviceTag) {
		this.deviceTag = deviceTag;
	}

	public AttachmentPointInfo getIngressAttachmentPointInfo() {
		return ingressAttachmentPointInfo;
	}

	public void setIngressAttachmentPointInfo(AttachmentPointInfo ingressAttachmentPointInfo) {
		this.ingressAttachmentPointInfo = ingressAttachmentPointInfo;
	}

	public AttachmentPointInfo getOutgressAttachmentPointInfo() {
		return outgressAttachmentPointInfo;
	}

	public void setOutgressAttachmentPointInfo(AttachmentPointInfo outgressAttachmentPointInfo) {
		this.outgressAttachmentPointInfo = outgressAttachmentPointInfo;
	}

	@Override
	public String toString() {
		return "SecurityDevice [deviceKey=" + deviceKey + ", deviceName="
				+ deviceName + ", deviceTag=" + deviceTag + ", inAP="
				+ ingressAttachmentPointInfo + ", outAP=" + outgressAttachmentPointInfo + "]";
	}
	
}
