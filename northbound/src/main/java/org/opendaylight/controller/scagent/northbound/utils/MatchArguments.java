package org.opendaylight.controller.scagent.northbound.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.sal.utils.HexEncode;
import org.openflow.protocol.OFMatch;
import org.openflow.util.HexString;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchArguments {

	final public static int OFPFW_ALL = ((1 << 22) - 1);
	public static final short VLAN_UNTAGGED = (short)0xffff;

	protected int wildcards;
	protected short inputPort;
	protected byte[] dataLayerSource;
	protected byte[] dataLayerDestination;
	protected short dataLayerVirtualLan;
	protected byte dataLayerVirtualLanPriorityCodePoint;
	protected short dataLayerType;
	protected byte networkTypeOfService;
	protected byte networkProtocol;
	protected int networkSource;
	protected int networkDestination;
	protected short transportSource;
	protected short transportDestination;

	/**
	 * @param args
	 */
	public MatchArguments() {
		this.wildcards = OFPFW_ALL;
		this.dataLayerDestination = new byte[] { 0x0, 0x0, 0x0, 0x0, 0x0,
				0x0 };
		this.dataLayerSource = new byte[] { 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 };
		this.dataLayerVirtualLan = VLAN_UNTAGGED;
		this.dataLayerVirtualLanPriorityCodePoint = 0;
		this.dataLayerType = 0;
		this.inputPort = 0;
		this.networkProtocol = 0;
		this.networkTypeOfService = 0;
		this.networkSource = 0;
		this.networkDestination = 0;
		this.transportDestination = 0;
		this.transportSource = 0;
	}

	/**
	 * 
	 * @return an OFMatch object without in_port
	 */
	public OFMatch toOFMatch(){
		Integer wildcard_hints = OFMatch.OFPFW_ALL;
		OFMatch match = new OFMatch();
		if(!Arrays.equals(dataLayerSource, new byte[]{0,0,0,0,0,0})){
			wildcard_hints &=  ~OFMatch.OFPFW_DL_SRC;
			match.setDataLayerSource(dataLayerSource);
		}
		if(!Arrays.equals(dataLayerDestination, new byte[]{0,0,0,0,0,0})){
			wildcard_hints &=  ~OFMatch.OFPFW_DL_DST;
			match.setDataLayerDestination(dataLayerDestination);
		}
		if(dataLayerVirtualLan != VLAN_UNTAGGED){
			wildcard_hints &= ~OFMatch.OFPFW_DL_VLAN;
			match.setDataLayerVirtualLan(dataLayerVirtualLan);
			if(dataLayerVirtualLanPriorityCodePoint != 0){
				match.setDataLayerVirtualLanPriorityCodePoint(dataLayerVirtualLanPriorityCodePoint);
			}
		}
		if(dataLayerType != 0){
			wildcard_hints &= ~OFMatch.OFPFW_DL_TYPE;
			match.setDataLayerType(dataLayerType);
		}
		if(networkSource != 0){
			wildcard_hints &= ~OFMatch.OFPFW_NW_DST_MASK;
			match.setNetworkSource(networkSource);
		}
		if(networkDestination != 0){
			wildcard_hints &= ~OFMatch.OFPFW_NW_SRC_MASK;
			match.setNetworkDestination(networkDestination);
		}
		if(networkProtocol != 0){
			wildcard_hints &= ~OFMatch.OFPFW_NW_PROTO;
			match.setNetworkProtocol(networkProtocol);
		}
		if(networkTypeOfService != 0){
			wildcard_hints &= ~OFMatch.OFPFW_NW_TOS;
			match.setNetworkProtocol(networkTypeOfService);
		}
		if(transportSource != 0){
			wildcard_hints &= ~OFMatch.OFPFW_TP_SRC;
			match.setTransportSource(transportSource);
		}
		if(transportDestination != 0){
			wildcard_hints &= ~OFMatch.OFPFW_TP_DST;
			match.setTransportDestination(transportDestination);
		}
		match.setWildcards(wildcard_hints);
		return match;
	}
	
	public Match toODLMatch(){
		Match match = new Match();
		if(!Arrays.equals(dataLayerSource, new byte[]{0,0,0,0,0,0})){
			match.setField(MatchType.DL_SRC,dataLayerSource);
		}
		if(!Arrays.equals(dataLayerDestination, new byte[]{0,0,0,0,0,0})){
			match.setField(MatchType.DL_DST,dataLayerDestination);
		}
		if(dataLayerVirtualLan != VLAN_UNTAGGED){
			match.setField(MatchType.DL_VLAN,dataLayerVirtualLan);
			if(dataLayerVirtualLanPriorityCodePoint != 0){
				match.setField(MatchType.DL_VLAN_PR,dataLayerVirtualLanPriorityCodePoint);
			}
		}
		if(dataLayerType != 0){
			match.setField(MatchType.DL_TYPE,dataLayerType);
		}
		if(networkSource != 0){
			match.setField(MatchType.NW_SRC,networkSource);
		}
		if(networkDestination != 0){
			match.setField(MatchType.NW_DST,networkDestination);
		}
		if(networkProtocol != 0){
			match.setField(MatchType.NW_PROTO,networkProtocol);
		}
		if(networkTypeOfService != 0){
			match.setField(MatchType.NW_TOS,networkTypeOfService);
		}
		if(transportSource != 0){
			match.setField(MatchType.TP_SRC,transportSource);
		}
		if(transportDestination != 0){
			match.setField(MatchType.TP_DST,transportDestination);
		}
		return match;
	}

	/**
	 * 比较两个Match的关系 
	 * @param otherMatch
	 * @return this is a xx of otherMatch
	 */
	public MatchRelation compareWith(MatchArguments otherMatch){
		boolean superSet = true;
		boolean equal = true;
		boolean subSet = true;

		if(Arrays.equals(this.dataLayerSource, 
				new byte[]{ 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 })){//this 未指定src mac
			if(!Arrays.equals(otherMatch.dataLayerSource, 	//other指定了 src mac
					new byte[]{ 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 })){
				subSet = false;							//不可能是子集
				equal = false;
			}
		}else{	//this指定了 src mac
			if(Arrays.equals(otherMatch.dataLayerSource, 	//other未指定了 src mac
					new byte[]{ 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 })){
				superSet = false;							//不可能是父集
				equal =  false;
			}else if(Arrays.equals(otherMatch.dataLayerSource, 	//other指定了 src mac
					this.dataLayerSource)){
				//				unrelated = false;			//即使指定相同的域，也可能不相关
			}else{	//二者有一域不同，则肯定不相关
				return MatchRelation.UNRELATED;
			}
		}

		if(Arrays.equals(this.dataLayerDestination, 
				new byte[]{ 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 })){//this 未指定dst mac
			if(!Arrays.equals(otherMatch.dataLayerDestination, 	//other指定了 dst mac
					new byte[]{ 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 })){
				subSet = false;							//不可能是子父集
				equal = false;
			}
		}else{	//this指定了 dst mac
			if(Arrays.equals(otherMatch.dataLayerDestination, 	//other未指定了 dst mac
					new byte[]{ 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 })){
				superSet = false;							//不可能是父集
				equal =  false;
			}else if(Arrays.equals(otherMatch.dataLayerDestination, 	//other指定了 dst mac
					this.dataLayerDestination)){
				//				unrelated = false;
			}else{	//二者有一域不同，则肯定不相关
				return MatchRelation.UNRELATED;
			}
		}

		if(this.dataLayerType == 0){
			if(otherMatch.dataLayerType != 0){
				subSet = false;
				equal = false;
			}
		}else if(otherMatch.dataLayerType != 0){
			if(this.dataLayerType != otherMatch.dataLayerType){//this 未指定dataLayerType
				return MatchRelation.UNRELATED;
			}
		}else{
			superSet = false;
			equal = false;
		}

		/*		if(this.dataLayerVirtualLan != otherMatch.dataLayerVirtualLan){//this 未指定dataLayerType
			return MatchRelation.UNRELATED;
		}*/

		//TODO: dataLayerVirtualLanPriorityCodePoint 


		//TODO: 未考虑Mask的情况
		if(this.networkSource == 0){//this 未指定networkSource
			if(otherMatch.networkSource != 0){
				subSet = false;							//不可能是子集
				equal = false;
			}
		}else{	//this指定了 networkSource
			if(otherMatch.networkSource == 0){
				superSet = false;							//不可能是父集
				equal =  false;
			}else if(this.networkSource == otherMatch.networkSource){
				//				unrelated = false;
			}else{	//二者有一域不同，则肯定不相关
				return MatchRelation.UNRELATED;
			}
		}

		if(this.networkDestination == 0){//this 未指定networkDestination
			if(otherMatch.networkDestination != 0){
				subSet = false;							//不可能是子集
				equal = false;
			}
		}else{	//this指定了 networkDestination
			if(otherMatch.networkDestination == 0){
				superSet = false;							//不可能是父集
				equal =  false;
			}else if(this.networkDestination == otherMatch.networkDestination){
				//				intersect = false;
			}else{	//二者有一域不同，则肯定不相关
				return MatchRelation.UNRELATED;
			}
		}

		if(this.networkProtocol == 0){
			if(otherMatch.networkProtocol != 0){
				subSet = false;
				equal = false;
			}
		}else if(otherMatch.networkProtocol != 0){
			if(this.networkProtocol != otherMatch.networkProtocol){//this 未指定networkProtocol
				return MatchRelation.UNRELATED;
			}
		}else{
			superSet = false;
			equal = false;
		}

		//TODO: TOS

		if(this.transportSource == 0){//this 未指定transportSource
			if(otherMatch.transportSource != 0){
				subSet = false;							//不可能是子集
				equal = false;
			}
		}else{	//this指定了 transportSource
			if(otherMatch.transportSource == 0){
				superSet = false;							//不可能是父集
				equal =  false;
			}else if(this.transportSource == otherMatch.transportSource){//ip.src相同
				//				intersect = false;
			}else{	//二者有一域不同，则肯定不相关
				return MatchRelation.UNRELATED;
			}
		}

		if(this.transportDestination == 0){//this 未指定transportDestination
			if(otherMatch.transportDestination != 0){
				subSet = false;							//不可能是子集
				equal = false;
			}
		}else{	//this指定了 transportDestination
			if(otherMatch.transportDestination == 0){
				superSet = false;							//不可能是父集
				equal =  false;
			}else if(this.transportDestination == otherMatch.transportDestination){
				//				intersect = false;
			}else{	//二者有一域不同，则肯定不相关
				return MatchRelation.UNRELATED;
			}
		}
		if(equal)return MatchRelation.EQUAL;
		//		if(unrelated)return Relation.UNRELATED;
		if(subSet)return MatchRelation.SUBSET;
		if(superSet)return MatchRelation.SUPERSET;
		return MatchRelation.INTERSECT;
	}

	public void fromJson(JsonNode matchArgumentsNode){
		if(matchArgumentsNode != null){
			if(matchArgumentsNode.hasNonNull("wildcards")){
				int asInt = matchArgumentsNode.path("wildcards").asInt();
				if(asInt != 0)
					this.setWildcards(asInt);
			}
			if(matchArgumentsNode.hasNonNull("inputport")){
				int asInt = matchArgumentsNode.path("inputport").asInt();
				if(asInt != 0)
					this.setInputPort((short)asInt);
			}
			if(matchArgumentsNode.hasNonNull("dataLayerSource")){
				String asText = matchArgumentsNode.path("dataLayerSource").asText();
				if(!asText.equals("00:00:00:00:00:00"))
					this.setDataLayerSource(HexString.fromHexString(asText));	
			}
			if(matchArgumentsNode.hasNonNull("dataLayerDestination")){
				String asText = matchArgumentsNode.path("dataLayerDestination").asText();
				if(!asText.equals("00:00:00:00:00:00"))
					this.setDataLayerDestination(HexString.fromHexString(asText));
			}
			if(matchArgumentsNode.hasNonNull("dataLayerVirtualLan")){
				int asInt = matchArgumentsNode.path("dataLayerVirtualLan").asInt();
				if(asInt!=0)
					this.setDataLayerVirtualLan((short)asInt );
			}
			if(matchArgumentsNode.hasNonNull("dataLayerVirtualLanPriorityCodePoint")){
				int asInt = matchArgumentsNode.path("dataLayerVirtualLanPriorityCodePoint").asInt();
				if(asInt!=0)
					this.setDataLayerVirtualLanPriorityCodePoint((byte)asInt);
			}
			if(matchArgumentsNode.hasNonNull("dataLayerType")){
				int asInt = matchArgumentsNode.path("dataLayerType").asInt();
				if(asInt!=0)
					this.setDataLayerType((short) asInt);
			}
			if(matchArgumentsNode.hasNonNull("networkTypeOfService")){
				int asInt = matchArgumentsNode.path("networkTypeOfService").asInt();
				if(asInt!=0){
					this.setNetworkTypeOfService((byte) asInt);
				}
			}
			if(matchArgumentsNode.hasNonNull("networkProtocol")){
				int asInt = matchArgumentsNode.path("networkProtocol").asInt();
				if(asInt != 0)
					this.setNetworkProtocol((byte) asInt);
			}
			if(matchArgumentsNode.hasNonNull("networkSource")){
				String asText = matchArgumentsNode.path("networkSource").asText();
				if(!asText.equals("0")){
					byte[] address;
					try {
						address = InetAddress.getByName(asText).getAddress();
						int int1 = ByteBuffer.wrap(address).getInt();
						this.setNetworkSource(int1);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if(matchArgumentsNode.hasNonNull("networkDestination")){
				String asText = matchArgumentsNode.path("networkDestination").asText();
				if(!asText.equals("0")){
					byte[] address;
					try {
						address = InetAddress.getByName(asText).getAddress();
						int int1 = ByteBuffer.wrap(address).getInt();
						this.setNetworkDestination(int1);					
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			if(matchArgumentsNode.hasNonNull("transportSource")){
				int asInt = matchArgumentsNode.path("transportSource").asInt();
				if(asInt!=0){
					this.setTransportSource((byte) asInt);
				}
			}
			if(matchArgumentsNode.hasNonNull("transportDestination")){
				int asInt = matchArgumentsNode.path("transportDestination").asInt();
				if(asInt!=0){
					this.setTransportDestination((byte) asInt);
				}
			}
		}
	}


	public static void main(String[] argvs){

		MatchArguments m1 = new MatchArguments();
		MatchArguments m2 = new MatchArguments();
		System.out.println(m1);
		m2.setDataLayerDestination(new byte[]{1,1,1,1,1,1});
		System.out.println(m1.compareWith(m2));
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

	public byte[] getDataLayerSource() {
		return dataLayerSource;
	}

	public void setDataLayerSource(byte[] dataLayerSource) {
		this.dataLayerSource = dataLayerSource;
	}

	public byte[] getDataLayerDestination() {
		return dataLayerDestination;
	}

	public void setDataLayerDestination(byte[] dataLayerDestination) {
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

	public void setDataLayerVirtualLanPriorityCodePoint(
			byte dataLayerVirtualLanPriorityCodePoint) {
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
	    wildcards &= ~OFMatch.OFPFW_TP_DST;
		this.transportDestination = transportDestination;
	}

	@Override
	public String toString() {
		String res = "";
		if(wildcards != OFPFW_ALL)res += "wildcards = " + wildcards+", ";
		if(inputPort != 0)res += "inputPort = " + inputPort+", ";
		if(!Arrays.equals(dataLayerSource, new byte[]{0,0,0,0,0,0}))
			res += "dataLayerSource = " + HexEncode.bytesToHexString(dataLayerSource).toString()+", ";
		if(!Arrays.equals(dataLayerDestination, new byte[]{0,0,0,0,0,0}))
			res += "dataLayerDestination = " + HexEncode.bytesToHexString(dataLayerDestination).toString()+", ";
		if(dataLayerVirtualLan != VLAN_UNTAGGED)res += "dataLayerVirtualLan = " + dataLayerVirtualLan+", ";
		if(dataLayerVirtualLanPriorityCodePoint != 0)res += "dataLayerVirtualLanPriorityCodePoint = " + dataLayerVirtualLanPriorityCodePoint+", ";
		if(dataLayerType != 0)res += "dataLayerType = " + dataLayerType+", ";
		if(networkTypeOfService != 0)res += "networkTypeOfService = " + networkTypeOfService+", ";
		if(networkProtocol != 0)res += "networkProtocol = " + networkProtocol+", ";
		if(networkSource != 0)res += "networkSource = " + networkSource+", ";
		if(networkDestination != 0)res += "networkDestination = " + networkDestination+", ";
		if(transportSource != 0)res += "transportSource = " + transportSource+", ";
		if(transportDestination != 0)res += "transportDestination = " + transportDestination+", ";		
		return res == "" ? "no field specified(wildcards all)":res;
	}
	
}
