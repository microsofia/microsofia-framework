package microsofia.framework;

import javax.inject.Inject;

import com.google.inject.AbstractModule;

import microsofia.container.Container;
import microsofia.container.InitializationContext;
import microsofia.container.application.AbstractApplication;
import microsofia.container.application.ApplicationDescriptor;
import microsofia.framework.agent.IAgentService;
import microsofia.framework.client.Client;
import microsofia.framework.client.IClient;
import microsofia.framework.registry.IRegistryService;
import microsofia.framework.registry.RegistryConfiguration;
import microsofia.framework.service.IService;

public class ClientApplication extends AbstractApplication{
	@Inject
	private Container container;

	public ClientApplication(){
		applicationDescriptor=new ApplicationDescriptor();
		applicationDescriptor.type("client");
		
		applicationDescriptor.properties().property("registry").objectType(RegistryConfiguration.class);

		applicationDescriptor.endpoints().endpoint("fwk")
										 .client(IService.class)
										 .client(IRegistryService.class)
										 .client(IAgentService.class)
										 .client(IClient.class);
		
	}
	
	@Override
	public void preInit(InitializationContext context) {
		context.addGuiceModule(new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(IClient.class).to(Client.class).asEagerSingleton();
			}
		});
	}

	public void run() throws Throwable{
		Client client=container.getInstance(Client.class);
		client.export();
		client.connect();
	}
}
