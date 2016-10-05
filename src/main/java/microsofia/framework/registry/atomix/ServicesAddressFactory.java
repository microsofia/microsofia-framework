package microsofia.framework.registry.atomix;

import java.util.Properties;

import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.resource.ResourceFactory;
import io.atomix.resource.ResourceStateMachine;

public class ServicesAddressFactory implements ResourceFactory<ServicesAddress> {

	public ServicesAddressFactory(){
	}
	
	@Override
	public SerializableTypeResolver createSerializableTypeResolver() {
		return new Commands.TypeResolver();
	}
	
	@Override
	public ResourceStateMachine createStateMachine(Properties config) {
		return new ServicesAddressState(config);
	}
	
	@Override
	public ServicesAddress createInstance(CopycatClient client, Properties options) {
		return new ServicesAddress(client, options);
	}
}
