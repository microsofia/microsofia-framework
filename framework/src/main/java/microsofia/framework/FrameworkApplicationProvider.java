package microsofia.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.inject.AbstractModule;

import microsofia.container.ContainerException;
import microsofia.container.InitializationContext;
import microsofia.container.application.ApplicationProvider;
import microsofia.container.application.DefaultApplication;
import microsofia.container.application.IApplication;
import microsofia.framework.agent.AgentConfiguration;
import microsofia.framework.agent.AgentService;
import microsofia.framework.client.Client;
import microsofia.framework.client.ClientConfiguration;
import microsofia.framework.client.IClient;
import microsofia.framework.registry.IRegistryService;
import microsofia.framework.registry.RegistryConfiguration;
import microsofia.framework.registry.RegistryService;
import microsofia.framework.service.Service;

public class FrameworkApplicationProvider extends ApplicationProvider{

	public FrameworkApplicationProvider(){
	}

	@Override
	public List<IApplication> getApplication(final InitializationContext context) {
		List<IApplication> applications=new ArrayList<>();
		
		Consumer<Object> start=it->{
			((Service)it).start();
		};
		
		Consumer<Object> stop=it->{
			((Service)it).stop();
		};

		//registry application
		createRegistryApplication(applications, start, stop);

		//agent application
		createAgentApplication(applications, start, stop);
		
		//client application
		createClientApplication(applications, start, stop);
		
		return applications;
	}

	private void createClientApplication(List<IApplication> applications, Consumer<Object> start,Consumer<Object> stop) {
		DefaultApplication clientApplication=new DefaultApplication("client",Client.class);
		clientApplication.setInitialization(it->{
			ClientConfiguration clientConfiguration;
			try{
				clientConfiguration=ClientConfiguration.createClientConfiguration(it.getApplicationConfig().getElement());
			}catch (Exception e) {
				throw new ContainerException("Cannot read client configuration in settings file.",e);
			}
			if (clientConfiguration==null){
				throw new ContainerException("Missing client configuration in settings file.");
			}
			
			it.addGuiceModule(new AbstractModule() {
				
				@Override
				protected void configure() {
					bind(IClient.class).to(Client.class).asEagerSingleton();
					
					bind(ClientConfiguration.class).toInstance(clientConfiguration);
				}
			});
		});
		clientApplication.setStartCallback(start);
		clientApplication.setStopCallback(stop);
		clientApplication.addClass(Client.class);		
		applications.add(clientApplication);
	}

	private void createAgentApplication(List<IApplication> applications, Consumer<Object> start, Consumer<Object> stop) {
		DefaultApplication agentApplication=new DefaultApplication("agent",AgentService.class);
		agentApplication.setInitialization(it->{
			AgentConfiguration agentConfiguration;
			try{
				agentConfiguration=AgentConfiguration.createAgentConfiguration(it.getApplicationConfig().getElement());
			}catch (Exception e) {
				throw new ContainerException("Cannot read agent configuration in settings file.",e);
			}
			if (agentConfiguration==null){
				throw new ContainerException("Missing agent configuration in settings file.");
			}

			it.addGuiceModule(new AbstractModule() {				
				@Override
				protected void configure() {
					
					bind(IRegistryService.class).to(RegistryService.class).asEagerSingleton();
					
					bind(AgentConfiguration.class).toInstance(agentConfiguration);
				}
			});			
		});
		agentApplication.setStartCallback(start);
		agentApplication.setStopCallback(stop);
		agentApplication.addClass(AgentService.class);		
		applications.add(agentApplication);
	}

	private void createRegistryApplication(List<IApplication> applications, Consumer<Object> start, Consumer<Object> stop) {
		DefaultApplication registryApplication=new DefaultApplication("registry",RegistryService.class);
		registryApplication.setInitialization(it->{
			RegistryConfiguration registryConfiguration;
			try{
				registryConfiguration=RegistryConfiguration.readFrom(it.getApplicationConfig().getElement());
			}catch (Exception e) {
				throw new ContainerException("Cannot read registry configuration in settings file.",e);
			}
			if (registryConfiguration==null){
				throw new ContainerException("Missing registry configuration in settings file.");
			}
			it.addGuiceModule(new AbstractModule() {
				
				@Override
				protected void configure() {
					bind(IRegistryService.class).to(RegistryService.class).asEagerSingleton();
					
					bind(RegistryConfiguration.class).toInstance(registryConfiguration);
				}
			});
		});
		registryApplication.setStartCallback(start);
		registryApplication.setStopCallback(stop);
		registryApplication.addClass(RegistryService.class);		
		applications.add(registryApplication);
	}
}
