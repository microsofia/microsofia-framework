package microsofia.framework.service;

import java.util.ArrayList;
import java.util.List;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import microsofia.rmi.ObjectAddress;
import microsofia.rmi.ServerAddress;

public class ServiceAddress implements CatalystSerializable{
	private ObjectAddress objectAddress;

	public ServiceAddress(){
	}

	public ObjectAddress getObjectAddress() {
		return objectAddress;
	}

	public void setObjectAddress(ObjectAddress objectAddress) {
		this.objectAddress = objectAddress;
	}

	public void writeObject(BufferOutput<?> buffer, Serializer serializer) {		
    	buffer.writeString(getObjectAddress().getId());
		buffer.writeString(getObjectAddress().getServerAddress().getHost());
		buffer.writeInt(getObjectAddress().getServerAddress().getPort());
		buffer.writeInt(getObjectAddress().getInterfaces().length);
		for (Class<?> c : getObjectAddress().getInterfaces()){
			buffer.writeString(c.getName());
		}
    }

    @Override
    public void readObject(BufferInput<?> buffer, Serializer serializer) {
    	setObjectAddress(new ObjectAddress());
    	getObjectAddress().setId(buffer.readString());
    	
    	ServerAddress sa=new ServerAddress();
    	getObjectAddress().setServerAddress(sa);
    	
    	sa.setHost(buffer.readString());
    	sa.setPort(buffer.readInt());

    	List<Class<?>> interfaces=new ArrayList<>();

    	int l=buffer.readInt();
    	for (int i=0;i<l;i++){
    		try {
				interfaces.add(Class.forName(buffer.readString()));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	getObjectAddress().setInterfaces(interfaces.toArray(new Class<?>[0]));
    }
    
    @Override
    public int hashCode(){
    	return objectAddress.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
    	if (!(o instanceof ServiceAddress)){
    		return false;
    	}
    	return ((ServiceAddress)o).objectAddress.equals(objectAddress);
    }
    
    @Override
    public String toString(){
    	return "Service[ObjectAddress:"+objectAddress+"]";
    }
}
