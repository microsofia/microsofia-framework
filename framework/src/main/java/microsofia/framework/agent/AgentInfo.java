package microsofia.framework.agent;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import microsofia.framework.service.ServiceInfo;

public class AgentInfo extends ServiceInfo{
	private String serviceName;
	private AgentLookupConfiguration lookupConfiguration;

	public AgentInfo(){
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
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
		out.writeUTF(serviceName);
		out.writeObject(lookupConfiguration);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		serviceName=in.readUTF();
		lookupConfiguration=(AgentLookupConfiguration)in.readObject();
	}
	
	@Override
	public String toString(){
		return super.toString()+"[ServiceName:"+serviceName+"][LookupConfiguration:"+lookupConfiguration+"]";
	}
}
