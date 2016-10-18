package microsofia.framework.registry.lookup;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import microsofia.framework.agent.AgentInfo;
import microsofia.framework.client.ClientInfo;

public class LookupResult implements Externalizable{
	private long id;
	private ClientInfo clientInfo;
	private AgentInfo agentInfo;
	
	public LookupResult(){
	}
	
	public long getId(){
		return id;
	}
	
	public void setId(long id){
		this.id=id;
	}

	public ClientInfo getClientInfo() {
		return clientInfo;
	}

	public void setClientInfo(ClientInfo clientInfo) {
		this.clientInfo = clientInfo;
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
		out.writeObject(clientInfo);
		out.writeObject(agentInfo);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id=in.readLong();
		clientInfo=(ClientInfo)in.readObject();
		agentInfo=(AgentInfo)in.readObject();
	}
}
