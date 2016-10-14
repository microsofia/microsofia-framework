package microsofia.framework.invoker;

import java.util.Properties;

import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.resource.ResourceFactory;
import io.atomix.resource.ResourceStateMachine;

public class InvokerFactory implements ResourceFactory<Invoker> {

	public InvokerFactory(){
	}
	
	@Override
	public SerializableTypeResolver createSerializableTypeResolver() {
		return new InvokerCommands.TypeResolver();
	}
	
	@Override
	public ResourceStateMachine createStateMachine(Properties config) {
		return new InvokerState(config);
	}
	
	@Override
	public Invoker createInstance(CopycatClient client, Properties options) {
		return new Invoker(client, options);
	}
}
