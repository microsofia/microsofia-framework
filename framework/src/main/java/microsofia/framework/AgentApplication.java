package microsofia.framework;

import javax.inject.Inject;
import com.google.inject.AbstractModule;
import microsofia.container.Container;
import microsofia.container.InitializationContext;
import microsofia.container.application.AbstractApplication;
import microsofia.container.application.ApplicationDescriptor;
import microsofia.framework.agent.AgentService;
import microsofia.framework.agent.IAgentService;
import microsofia.framework.registry.IRegistryService;
import microsofia.framework.registry.RegistryConfiguration;
import microsofia.framework.registry.RegistryService;
import microsofia.framework.service.IService;

public class AgentApplication extends AbstractApplication{
	@Inject
	private Container container;

	public AgentApplication(){
		applicationDescriptor=new ApplicationDescriptor();
		applicationDescriptor.type("agent");
		
		applicationDescriptor.properties().property("registry").objectType(RegistryConfiguration.class);

		applicationDescriptor.endpoints().endpoint("fwk")
										 .client(IService.class)
										 .client(IRegistryService.class)
										 .client(IAgentService.class);
		
	}
	
	@Override
	public void preInit(InitializationContext context) {
		context.addGuiceModule(new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(IRegistryService.class).to(RegistryService.class).asEagerSingleton();
			}
		});
	}

	public void run() throws Throwable{
		AgentService agent=container.getInstance(AgentService.class);
		agent.export();
		agent.init();
	}
}
