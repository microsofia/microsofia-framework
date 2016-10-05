package microsofia.framework.registry.atomix;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.serializer.SerializerRegistry;
import io.atomix.copycat.Command;
import microsofia.framework.service.ServiceAddress;

public class Commands {

	public static class AddCommand implements Command<Void>, CatalystSerializable{
		private static final long serialVersionUID = 1L;
		private ServiceAddress serviceAddress;

	    public AddCommand() {
	    }

	    public AddCommand(ServiceAddress serviceAddress) {
	      this.serviceAddress=serviceAddress;
	    }

	    public ServiceAddress address() {
	      return serviceAddress;
	    }

	    @Override
	    public void writeObject(BufferOutput buffer, Serializer serializer) {
	    	serviceAddress.writeObject(buffer,serializer);
	    }

	    @Override
	    public void readObject(BufferInput buffer, Serializer serializer) {
	    	serviceAddress=new ServiceAddress();
	    	serviceAddress.readObject(buffer,serializer);
	    }
	}
	
	public static class RemoveCommand implements Command<Void>, CatalystSerializable{
		private static final long serialVersionUID = 1L;
		private ServiceAddress serviceAddress;

	    public RemoveCommand() {
	    }

	    public RemoveCommand(ServiceAddress serviceAddress) {
	      this.serviceAddress=serviceAddress;
	    }

	    public ServiceAddress address() {
	      return serviceAddress;
	    }

	    @Override
	    public void writeObject(BufferOutput buffer, Serializer serializer) {
	    	serviceAddress.writeObject(buffer,serializer);
	    }

	    @Override
	    public void readObject(BufferInput buffer, Serializer serializer) {
	    	serviceAddress=new ServiceAddress();
	    	serviceAddress.readObject(buffer,serializer);
	    }
	}

	public static class TypeResolver implements SerializableTypeResolver {

		@Override
	    public void resolve(SerializerRegistry registry) {
			registry.register(AddCommand.class, 1980);
			registry.register(RemoveCommand.class, 1981);
			registry.register(ServiceAddress.class,1982);
	    }
	  }
}
