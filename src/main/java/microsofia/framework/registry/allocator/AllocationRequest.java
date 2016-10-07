package microsofia.framework.registry.allocator;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class AllocationRequest implements CatalystSerializable{
	private long id;
	private long sessionId;

	public AllocationRequest(){
	}
	
	public AllocationRequest(AllocationRequest request){
		this.id=request.getId();
		this.sessionId=request.sessionId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long id) {
		this.sessionId = id;
	}
	
	@Override
    public int hashCode(){
    	return (int)(id+sessionId);
    }
    
    @Override
    public boolean equals(Object o){
    	if (!(o instanceof AllocationRequest)){
    		return false;
    	}
    	AllocationRequest req=(AllocationRequest)o;
    	return id==req.id && sessionId==req.sessionId;
    }
    
	
	@Override
    public void writeObject(BufferOutput buffer, Serializer serializer) {
		buffer.writeLong(id);
		buffer.writeLong(sessionId);
    }

    @Override
    public void readObject(BufferInput buffer, Serializer serializer) {
    	id=buffer.readLong();
    	sessionId=buffer.readLong();
    }
    
    @Override
    public String toString(){
    	 return "[Id:"+id+"][SessionId:"+sessionId+"]";
    }
}
