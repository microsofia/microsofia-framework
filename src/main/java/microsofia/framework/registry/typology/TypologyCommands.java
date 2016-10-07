package microsofia.framework.registry.typology;

import java.util.ArrayList;
import java.util.List;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.serializer.SerializerRegistry;
import io.atomix.copycat.Command;
import io.atomix.copycat.Query;
import microsofia.framework.service.ServiceAddress;

public class TypologyCommands {
	
	public static class GetAgentsQuery implements Query<List<ServiceAddress>>, CatalystSerializable{
		private static final long serialVersionUID = 1L;

	    public GetAgentsQuery() {
	    }

	    @Override
	    public void writeObject(BufferOutput buffer, Serializer serializer) {
	    }

	    @Override
	    public void readObject(BufferInput buffer, Serializer serializer) {
	    }
	}
	
	public static abstract class ServiceBasedCommand<S extends ServiceAddress> implements Command<Void>, CatalystSerializable{
		private static final long serialVersionUID = 1L;
		protected S serviceAddress;

	    public ServiceBasedCommand() {
	    }

	    public ServiceBasedCommand(S serviceAddress) {
	      this.serviceAddress=serviceAddress;
	    }
	    
	    public abstract Class<S> getType();

	    public S address() {
	      return serviceAddress;
	    }

	    @Override
	    public void writeObject(BufferOutput buffer, Serializer serializer) {
	    	serviceAddress.writeObject(buffer,serializer);
	    }

	    @Override
	    public void readObject(BufferInput buffer, Serializer serializer) {
	    	try {
				serviceAddress=getType().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	serviceAddress.readObject(buffer,serializer);
	    }
	}

	public static class AddAgent extends ServiceBasedCommand<ServiceAddress>{
		private static final long serialVersionUID = 0L;

		public AddAgent() {
	    }

	    public AddAgent(ServiceAddress serviceAddress) {
	      super(serviceAddress);
	    }
	    
	    @Override
	    public Class<ServiceAddress> getType(){
	    	return ServiceAddress.class;
	    }
	}
	
	public static class AddRegistry extends ServiceBasedCommand<ServiceAddress>{
		private static final long serialVersionUID = 0L;

		public AddRegistry() {
	    }

	    public AddRegistry(ServiceAddress serviceAddress) {
	      super(serviceAddress);
	    }
	    
	    @Override
	    public Class<ServiceAddress> getType(){
	    	return ServiceAddress.class;
	    }
	}
	
	public static class AddClient extends ServiceBasedCommand<ServiceAddress>{
		private static final long serialVersionUID = 0L;

		public AddClient() {
	    }

	    public AddClient(ServiceAddress serviceAddress) {
	      super(serviceAddress);
	    }
	    
	    @Override
	    public Class<ServiceAddress> getType(){
	    	return ServiceAddress.class;
	    }
	}
	
	public static class RemoveAgent extends ServiceBasedCommand<ServiceAddress>{
		private static final long serialVersionUID = 0L;

		public RemoveAgent(){
		}

		public RemoveAgent(ServiceAddress serviceAddress){
			super(serviceAddress);
		}
		
		@Override
		public Class<ServiceAddress> getType(){
			return ServiceAddress.class;
		}
	}

	public static class RemoveRegistry extends ServiceBasedCommand<ServiceAddress>{
		private static final long serialVersionUID = 0L;

		public RemoveRegistry(){
		}

		public RemoveRegistry(ServiceAddress serviceAddress){
			super(serviceAddress);
		}
		
		@Override
		public Class<ServiceAddress> getType(){
			return ServiceAddress.class;
		}
	}
	
	public static class RemoveClient extends ServiceBasedCommand<ServiceAddress>{
		private static final long serialVersionUID = 0L;

		public RemoveClient(){
		}

		public RemoveClient(ServiceAddress serviceAddress){
			super(serviceAddress);
		}
		
		@Override
		public Class<ServiceAddress> getType(){
			return ServiceAddress.class;
		}
	}
	
	public static class TypeResolver implements SerializableTypeResolver {

		@Override
	    public void resolve(SerializerRegistry registry) {
			registry.register(AddAgent.class, 1980);
			registry.register(RemoveAgent.class, 1981);
			registry.register(AddRegistry.class, 1982);
			registry.register(RemoveRegistry.class, 1983);
			registry.register(AddClient.class, 1984);
			registry.register(RemoveClient.class, 1985);
			registry.register(ServiceAddress.class,1986);
	    }
	  }
}
