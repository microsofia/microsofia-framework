package microsofia.framework.agent;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import microsofia.framework.service.ServiceInfo;

public class AgentInfo extends ServiceInfo{
	private String queue;
	private AgentLookupConfiguration lookupConfiguration;

	public AgentInfo(){
	}

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public AgentLookupConfiguration getLookupConfiguration() {
		return lookupConfiguration;
	}

	public void setLookupConfiguration(AgentLookupConfiguration lookupConfiguration) {
		this.lookupConfiguration = lookupConfiguration;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(queue);
		out.writeObject(lookupConfiguration);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		queue=in.readUTF();
		lookupConfiguration=(AgentLookupConfiguration)in.readObject();
	}
	
	@Override
	public String toString(){
		return super.toString()+"[Queue:"+queue+"][LookupConfiguration:"+lookupConfiguration+"]";
	}
}
