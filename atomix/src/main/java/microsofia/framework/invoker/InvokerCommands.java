package microsofia.framework.invoker;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.serializer.SerializerRegistry;
import io.atomix.copycat.Command;
import io.atomix.copycat.Query;

public class InvokerCommands {
	
	public static class InvokeCommand implements Command<InvocationResponse>, CatalystSerializable{
		private static final long serialVersionUID = 1L;
		protected InvocationRequest invocationRequest;

	    public InvokeCommand() {
	    }

	    public InvokeCommand(InvocationRequest invocationRequest) {
	      this.invocationRequest=invocationRequest;
	    }
	    
	    public InvocationRequest request() {
	      return invocationRequest;
	    }

	    @Override
	    public void writeObject(BufferOutput<?> buffer, Serializer serializer) {
	    	invocationRequest.writeObject(buffer,serializer);
	    }

	    @Override
	    public void readObject(BufferInput<?> buffer, Serializer serializer) {
	    	invocationRequest=new InvocationRequest();
	    	invocationRequest.readObject(buffer,serializer);
	    }
	}
	
	public static class SetInvokerCommand implements Command<Void>, CatalystSerializable{
		private static final long serialVersionUID = 1L;

	    public SetInvokerCommand() {
	    }

	    @Override
	    public void writeObject(BufferOutput<?> buffer, Serializer serializer) {
	    }

	    @Override
	    public void readObject(BufferInput<?> buffer, Serializer serializer) {
	    }
	}
	
	public static class SetInvocationResponseCommand implements Command<Void>, CatalystSerializable{
		private static final long serialVersionUID = 1L;
		private InvocationResponse invocationResponse;

	    public SetInvocationResponseCommand() {
	    }
	    
	    public SetInvocationResponseCommand(InvocationResponse invocationResponse) {
	    	this.invocationResponse=invocationResponse;
	    }
	    
	    public InvocationResponse response(){
	    	return invocationResponse;
	    }

	    @Override
	    public void writeObject(BufferOutput<?> buffer, Serializer serializer) {
	    	invocationResponse.writeObject(buffer, serializer);
	    }

	    @Override
	    public void readObject(BufferInput<?> buffer, Serializer serializer) {
	    	invocationResponse=new InvocationResponse();
	    	invocationResponse.readObject(buffer, serializer);
	    }
	}
	
	public static class GetMaxId implements Query<Long>, CatalystSerializable {
		private static final long serialVersionUID = 0L;

	    public GetMaxId() {
	    }
	    
	    @Override
	    public void writeObject(BufferOutput<?> output, Serializer serializer) {
	    }

	    @Override
	    public void readObject(BufferInput<?> input, Serializer serializer) {
	    }
	}

	public static class TypeResolver implements SerializableTypeResolver {

		@Override
	    public void resolve(SerializerRegistry registry) {
			registry.register(InvokeCommand.class, 1990);
			registry.register(InvocationRequest.class,1991);
			registry.register(SetInvokerCommand.class,1992);
			registry.register(SetInvocationResponseCommand.class,1993);
			registry.register(GetMaxId.class,1995);
			registry.register(InvocationResponse.class,1994);
		}
	}
}
