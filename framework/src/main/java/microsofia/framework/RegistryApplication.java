package microsofia.framework;

import javax.inject.Inject;
import com.google.inject.AbstractModule;
import microsofia.container.Container;
import microsofia.container.InitializationContext;
import microsofia.container.application.AbstractApplication;
import microsofia.container.application.ApplicationDescriptor;
import microsofia.framework.agent.IAgentService;
import microsofia.framework.registry.IRegistryService;
import microsofia.framework.registry.RegistryConfiguration;
import microsofia.framework.registry.RegistryService;
import microsofia.framework.service.IService;

public class RegistryApplication extends AbstractApplication{
	@Inject
	private Container container;

	public RegistryApplication(){
		applicationDescriptor=new ApplicationDescriptor();
		applicationDescriptor.type("registry");
		
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
		RegistryService registry=container.getInstance(RegistryService.class);
		registry.export();
		registry.init();
	}
}
