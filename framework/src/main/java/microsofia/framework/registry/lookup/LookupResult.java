package microsofia.framework.registry.lookup;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import microsofia.framework.agent.AgentInfo;

public class LookupResult implements Externalizable{
	private long id;
	private LookupRequest lookupRequest;
	private AgentInfo agentInfo;
	
	public LookupResult(){
	}
	
	public long getId(){
		return id;
	}
	
	public void setId(long id){
		this.id=id;
	}

	public LookupRequest getLookupRequest() {
		return lookupRequest;
	}

	public void setLookupRequest(LookupRequest lookupRequest) {
		this.lookupRequest = lookupRequest;
	}

	public AgentInfo getAgentInfo() {
		return agentInfo;
	}

	public void setAgentInfo(AgentInfo agentInfo) {
		this.agentInfo = agentInfo;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(id);
		out.writeObject(lookupRequest);
		out.writeObject(agentInfo);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id=in.readLong();
		lookupRequest=(LookupRequest)in.readObject();
		agentInfo=(AgentInfo)in.readObject();
	}
}
