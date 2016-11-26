package microsofia.framework.registry.lookup;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import microsofia.framework.client.ClientInfo;

public class LookupRequest implements Externalizable{
	private ClientInfo clientInfo;
	private String name;
	private String group;
	private int weight;
	
	public LookupRequest(){
		weight=1;
	}

	public ClientInfo getClientInfo() {
		return clientInfo;
	}

	public void setClientInfo(ClientInfo clientInfo) {
		this.clientInfo = clientInfo;
	}

	public String getName() {
		return name;
	}

	public void setName(String n) {
		this.name = n;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String n) {
		this.group = n;
	}
	
	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(clientInfo);
		out.writeUTF(name);
		out.writeUTF(group);
		out.writeInt(weight);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		clientInfo=(ClientInfo)in.readObject();
		name=in.readUTF();
		group=in.readUTF();
		weight=in.readInt();
	}
}
