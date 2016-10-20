package microsofia.framework.registry.lookup;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import microsofia.framework.client.ClientInfo;

public class LookupRequest implements Externalizable{
	private ClientInfo clientInfo;
	private String queue;
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

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
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
		out.writeUTF(queue);
		out.writeInt(weight);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		clientInfo=(ClientInfo)in.readObject();
		queue=in.readUTF();
		weight=in.readInt();
	}
}
