package microsofia.framework.registry.typology;

import java.util.Properties;

import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.resource.ResourceFactory;
import io.atomix.resource.ResourceStateMachine;

public class TypologyFactory implements ResourceFactory<Typology> {

	public TypologyFactory(){
	}
	
	@Override
	public SerializableTypeResolver createSerializableTypeResolver() {
		return new TypologyCommands.TypeResolver();
	}
	
	@Override
	public ResourceStateMachine createStateMachine(Properties config) {
		return new TypologyState(config);
	}
	
	@Override
	public Typology createInstance(CopycatClient client, Properties options) {
		return new Typology(client, options);
	}
}
