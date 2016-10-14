package microsofia.framework.invoker;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class InvocationResponse implements CatalystSerializable{
	private InvocationRequest request;
	private Object result;
	private Throwable error;

	public InvocationResponse(){
	}

	public InvocationResponse(InvocationRequest request){
		this.setRequest(request);
	}
	
	public InvocationRequest getRequest() {
		return request;
	}

	public void setRequest(InvocationRequest request) {
		this.request = request;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result=result;
	}
	
	public Throwable getError(){
		return error;
	}
	
	public void setError(Throwable th){
		this.error=th;
	}
	
	@Override
    public void writeObject(BufferOutput<?> buffer, Serializer serializer) {
		getRequest().writeObject(buffer, serializer);
		if (result!=null){
			serializer.writeObject(result, buffer);
		}else{
			serializer.writeObject(null,buffer);
		}
		if (error!=null){
			serializer.writeObject(error, buffer);
		}else{
			serializer.writeObject(null,buffer);
		}
    }

    @Override
    public void readObject(BufferInput<?> buffer, Serializer serializer) {
    	setRequest(new InvocationRequest());
    	request.readObject(buffer, serializer);
    	result=serializer.readObject(buffer);
    	error=(Throwable)serializer.readObject(buffer);
    }
    
    @Override
    public int hashCode(){
    	return request.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
    	if (!(o instanceof InvocationResponse)){
    		return false;
    	}
    	InvocationResponse response=(InvocationResponse)o;
    	return request.equals(response.request);
    }
    
    @Override
    public String toString(){
    	 return "[InvocationRequest:"+request+"][Result:"+result+"][Error:"+error+"]";
    }
}
