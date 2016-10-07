package microsofia.framework.registry.allocator;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.serializer.SerializerRegistry;
import io.atomix.copycat.Command;
import microsofia.framework.service.ServiceAddress;

public class AllocatorCommands {
	
	public static class AllocationCommand implements Command<AllocationResponse>, CatalystSerializable{
		private static final long serialVersionUID = 1L;
		protected AllocationRequest allocationRequest;

	    public AllocationCommand() {
	    }

	    public AllocationCommand(AllocationRequest allocationRequest) {
	      this.allocationRequest=allocationRequest;
	    }
	    
	    public AllocationRequest request() {
	      return allocationRequest;
	    }

	    @Override
	    public void writeObject(BufferOutput buffer, Serializer serializer) {
	    	allocationRequest.writeObject(buffer,serializer);
	    }

	    @Override
	    public void readObject(BufferInput buffer, Serializer serializer) {
	    	allocationRequest=new AllocationRequest();
	    	allocationRequest.readObject(buffer,serializer);
	    }
	}
	
	public static class SetAllocatorCommand implements Command<Void>, CatalystSerializable{
		private static final long serialVersionUID = 1L;

	    public SetAllocatorCommand() {
	    }

	    @Override
	    public void writeObject(BufferOutput buffer, Serializer serializer) {
	    }

	    @Override
	    public void readObject(BufferInput buffer, Serializer serializer) {
	    }
	}
	
	public static class SetAllocationResponseCommand implements Command<Void>, CatalystSerializable{
		private static final long serialVersionUID = 1L;
		private AllocationResponse allocationResponse;

	    public SetAllocationResponseCommand() {
	    }
	    
	    public SetAllocationResponseCommand(AllocationResponse allocationResponse) {
	    	this.allocationResponse=allocationResponse;
	    }
	    
	    public AllocationResponse response(){
	    	return allocationResponse;
	    }

	    @Override
	    public void writeObject(BufferOutput buffer, Serializer serializer) {
	    	allocationResponse.writeObject(buffer, serializer);
	    }

	    @Override
	    public void readObject(BufferInput buffer, Serializer serializer) {
	    	allocationResponse=new AllocationResponse();
	    	allocationResponse.readObject(buffer, serializer);
	    }
	}

	public static class TypeResolver implements SerializableTypeResolver {

		@Override
	    public void resolve(SerializerRegistry registry) {
			registry.register(AllocationCommand.class, 1990);
			registry.register(AllocationRequest.class,1991);
			registry.register(SetAllocatorCommand.class,1992);
			registry.register(SetAllocationResponseCommand.class,1993);
			registry.register(AllocationResponse.class,1994);
	    }
	  }
}
