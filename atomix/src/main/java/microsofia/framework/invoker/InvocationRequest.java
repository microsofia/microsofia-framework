package microsofia.framework.invoker;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class InvocationRequest implements CatalystSerializable{
	private long id;
	private long sessionId;
	private int method;
	private Object[] args;

	public InvocationRequest(){
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
	
	public int getMethod(){
		return method;
	}
	
	public void setMethod(int i){
		method=i;
	}
	
	public Object[] getArguments(){
		return args;
	}
	
	public void setArguments(Object[] args){
		this.args=args;
	}
	
	@Override
    public int hashCode(){
    	return (int)(id+sessionId+method);
    }
    
    @Override
    public boolean equals(Object o){
    	if (!(o instanceof InvocationRequest)){
    		return false;
    	}
    	InvocationRequest req=(InvocationRequest)o;
    	return id==req.id && sessionId==req.sessionId && method==req.method;
    }

	@Override
    public void writeObject(BufferOutput<?> buffer, Serializer serializer) {
		buffer.writeLong(id);
		buffer.writeLong(sessionId);
		buffer.writeInt(method);
		if (args!=null){
			buffer.writeInt(args.length);
			for (Object o : args){
				serializer.writeObject(o, buffer);
			}
		}else{
			buffer.writeInt(0);
		}
    }

    @Override
    public void readObject(BufferInput<?> buffer, Serializer serializer) {
    	id=buffer.readLong();
    	sessionId=buffer.readLong();
    	method=buffer.readInt();
    	
    	int nb=buffer.readInt();
    	args=new Object[nb];
    	for (int i=0;i<nb;i++){
    		args[i]=serializer.readObject(buffer);
    	}
    }
    
    @Override
    public String toString(){
    	 return "[Id:"+id+"][SessionId:"+sessionId+"][Method:"+method+"]";
    }
}