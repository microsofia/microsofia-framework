package microsofia.framework.registry.allocator;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import microsofia.framework.service.ServiceAddress;

public class AllocationResponse implements CatalystSerializable{
	private AllocationRequest request;
	private ServiceAddress serviceAddress;

	public AllocationResponse(){
	}

	public AllocationResponse(AllocationRequest request){
		this.setRequest(request);
	}
	
	public AllocationResponse(AllocationResponse response){
		request=new AllocationRequest(response.getRequest());
		serviceAddress=new ServiceAddress(response.getServiceAddress());
	}
	
	public AllocationRequest getRequest() {
		return request;
	}

	public void setRequest(AllocationRequest request) {
		this.request = request;
	}

	public ServiceAddress getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(ServiceAddress serviceAddress) {
		this.serviceAddress = serviceAddress;
	}
	
	@Override
    public void writeObject(BufferOutput buffer, Serializer serializer) {
		getRequest().writeObject(buffer, serializer);
		getServiceAddress().writeObject(buffer, serializer);
    }

    @Override
    public void readObject(BufferInput buffer, Serializer serializer) {
    	setRequest(new AllocationRequest());
    	getRequest().readObject(buffer, serializer);
    	setServiceAddress(new ServiceAddress());
    	getServiceAddress().readObject(buffer, serializer);
    }
    
    @Override
    public int hashCode(){
    	return request.hashCode()+(serviceAddress!=null?serviceAddress.hashCode():0);
    }
    
    @Override
    public boolean equals(Object o){
    	if (!(o instanceof AllocationResponse)){
    		return false;
    	}
    	AllocationResponse response=(AllocationResponse)o;
    	boolean b=request.equals(response.request);
    	if (!b){
    		return false;
    	}
    	if (serviceAddress==null){
    		return response.serviceAddress==null;
    	}
    	return serviceAddress.equals(response.serviceAddress);
    }
    
    @Override
    public String toString(){
    	 return "[AllocationRequest:"+request+"][ServiceAddress:"+serviceAddress+"]";
    }
}
