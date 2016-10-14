package microsofia.framework.map;

import java.util.Properties;

import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.resource.ResourceFactory;
import io.atomix.resource.ResourceStateMachine;

public class MapFactory implements ResourceFactory<Map<?,?>> {

	public MapFactory(){
	}
	
	@Override
	public SerializableTypeResolver createSerializableTypeResolver() {
		return new MapCommands.TypeResolver();
	}
	
	@Override
	public ResourceStateMachine createStateMachine(Properties config) {
		return new MapState(config);
	}
	
	@Override
	public Map<?,?> createInstance(CopycatClient client, Properties options) {
		return new Map<Object,Object>(client, options);
	}
}
