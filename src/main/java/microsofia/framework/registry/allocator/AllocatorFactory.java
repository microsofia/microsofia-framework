package microsofia.framework.registry.allocator;

import java.util.Properties;

import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.resource.ResourceFactory;
import io.atomix.resource.ResourceStateMachine;

public class AllocatorFactory implements ResourceFactory<Allocator> {

	public AllocatorFactory(){
	}
	
	@Override
	public SerializableTypeResolver createSerializableTypeResolver() {
		return new AllocatorCommands.TypeResolver();
	}
	
	@Override
	public ResourceStateMachine createStateMachine(Properties config) {
		return new AllocatorState(config);
	}
	
	@Override
	public Allocator createInstance(CopycatClient client, Properties options) {
		return new Allocator(client, options);
	}
}
