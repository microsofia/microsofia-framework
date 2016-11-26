package microsofia.framework.agent;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import microsofia.framework.client.ClientInfo;

public class AgentInfo extends ClientInfo{
	private String name;
	private String group;
	private AgentLookupConfiguration lookupConfiguration;

	public AgentInfo(){
	}

	public String getName() {
		return name;
	}

	public void setName(String n) {
		this.name=n;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String g) {
		this.group=g;
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
		out.writeUTF(name);
		out.writeUTF(group);
		out.writeObject(lookupConfiguration);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		name=in.readUTF();
		group=in.readUTF();
		lookupConfiguration=(AgentLookupConfiguration)in.readObject();
	}
	
	@Override
	public String toString(){
		return super.toString()+"[Name:"+name+"][Group:"+group+"][LookupConfiguration:"+lookupConfiguration+"]";
	}
}
