package microsofia.framework.registry.lookup;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import microsofia.framework.client.ClientInfo;

//TODO use cpu/memory/weigth? request
public class LookupRequest implements Externalizable{
	private ClientInfo clientInfo;
	private String serviceName;
	
	public LookupRequest(){
	}

	public ClientInfo getClientInfo() {
		return clientInfo;
	}

	public void setClientInfo(ClientInfo clientInfo) {
		this.clientInfo = clientInfo;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(clientInfo);
		out.writeUTF(serviceName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		clientInfo=(ClientInfo)in.readObject();
		serviceName=in.readUTF();
	}
}
