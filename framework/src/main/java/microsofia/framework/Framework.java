package microsofia.framework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.w3c.dom.Element;

import com.google.inject.AbstractModule;

import microsofia.container.ContainerBuilder;
import microsofia.container.ContainerImpl;
import microsofia.container.application.ApplicationConfig;
import microsofia.container.application.DefaultApplication;
import microsofia.container.application.DefaultApplicationProvider;
import microsofia.framework.agent.AgentConfiguration;
import microsofia.framework.agent.AgentService;
import microsofia.framework.agent.IAgentService;
import microsofia.framework.client.ClientConfiguration;
import microsofia.framework.client.ClientService;
import microsofia.framework.client.IClientService;
import microsofia.framework.registry.IRegistryService;
import microsofia.framework.registry.RegistryConfiguration;
import microsofia.framework.registry.RegistryService;

public class Framework {
	protected ContainerImpl container;
	protected String[] args;
	protected ApplicationConfig applicationConfig;
	protected IServiceProvider serviceProvider;
	protected DefaultApplication application;
	
	public Framework(){
	}
	
	public Framework arguments(String[] a){
		this.args=a;
		return this;
	}
	
	public Framework applicationConfig(ApplicationConfig c){
		applicationConfig=c;
		return this;
	}
	
	private void createClientProvider(){
		ClientConfiguration clientConfiguration;
		try{
			clientConfiguration=ClientConfiguration.createClientConfiguration(applicationConfig.getElement());
		}catch (Exception e) {
			throw new FrameworkException("Cannot read client configuration in settings file.",e);
		}
		if (clientConfiguration==null){
			throw new FrameworkException("Missing client configuration in settings file.");
		}
		
		ServiceLoader<IClientProvider> serviceLoader=ServiceLoader.load(IClientProvider.class, getClass().getClassLoader());
		Iterator<IClientProvider> iterator=serviceLoader.iterator();

		while (iterator.hasNext()){
			IClientProvider c=iterator.next();
			if (c.getImplementation().equals(clientConfiguration.getImplementation())){
				serviceProvider=c;
				break;
			}
		}
		if (serviceProvider==null){
			throw new FrameworkException("Couldn't load client configuration with implementation "+clientConfiguration.getImplementation());
		}
		
		application.addModule(new AbstractModule() {

			@Override
			protected void configure() {
				bind(IClientService.class).to(ClientService.class).asEagerSingleton();
				bind(ClientConfiguration.class).toInstance(clientConfiguration);
			}
		});
		application.addClass(ClientService.class);
	}
	
	private void createAgentProvider(){
		AgentConfiguration agentConfiguration;
		try{
			agentConfiguration=AgentConfiguration.createAgentConfiguration(applicationConfig.getElement());
		}catch (Exception e) {
			throw new FrameworkException("Cannot read agent configuration in settings file.",e);
		}
		if (agentConfiguration==null){
			throw new FrameworkException("Missing agent configuration in settings file.");
		}
		
		ServiceLoader<IAgentProvider> serviceLoader=ServiceLoader.load(IAgentProvider.class, getClass().getClassLoader());
		Iterator<IAgentProvider> iterator=serviceLoader.iterator();

		while (iterator.hasNext()){
			IAgentProvider c=iterator.next();
			if (c.getImplementation().equals(agentConfiguration.getImplementation())){
				serviceProvider=c;
				break;
			}
		}
		if (serviceProvider==null){
			throw new FrameworkException("Couldn't load agent configuration with implementation "+agentConfiguration.getImplementation());
		}
		
		application.addModule(new AbstractModule() {

			@Override
			protected void configure() {
				bind(IAgentService.class).to(AgentService.class).asEagerSingleton();
				
				bind(AgentConfiguration.class).toInstance(agentConfiguration);
			}
		});
		application.addClass(AgentService.class);
	}
	
	private void createRegistryProvider(){
		RegistryConfiguration registryConfiguration;
		try{
			registryConfiguration=RegistryConfiguration.readFrom(applicationConfig.getElement());
		}catch (Exception e) {
			throw new FrameworkException("Cannot read registry configuration in settings file.",e);
		}
		if (registryConfiguration==null){
			throw new FrameworkException("Missing registry configuration in settings file.");
		}
		application.addModule(new AbstractModule() {

			@Override
			protected void configure() {
				bind(IRegistryService.class).to(RegistryService.class).asEagerSingleton();
				bind(RegistryConfiguration.class).toInstance(registryConfiguration);
			}
		});
		
		serviceProvider=new RegistryProvider();
	}

	public void start() throws Throwable{
		if (applicationConfig.getType().equals("fwk.client")){
			application=new DefaultApplication(applicationConfig.getType());
			createClientProvider();

		}else if (applicationConfig.getType().equals("fwk.agent")){
			application=new DefaultApplication(applicationConfig.getType());
			createAgentProvider();
			
		}else if (applicationConfig.getType().equals("fwk.registry")){
			application=new DefaultApplication(applicationConfig.getType());
			createRegistryProvider();
			
		}else{
			throw new IllegalArgumentException("Framework cannot start an application which is not of type fwk.client, fwk.agent or fwk.registry. Found:"+applicationConfig.getType());
		}

		ContainerBuilder builder=new ContainerBuilder();
		builder.arguments(args).applicationConfig(applicationConfig);
		container=(ContainerImpl)builder.build();

		DefaultApplicationProvider provider=new DefaultApplicationProvider();

		if (serviceProvider.getInjectedClasses()!=null){
			serviceProvider.getInjectedClasses().forEach(application::addClass);
		}
		if (serviceProvider.getGuiceModules()!=null){
			serviceProvider.getGuiceModules().forEach(application::addModule);
		}
		
		provider.addApplication(application);
		container.addApplicationProvider(provider);
		container.start();
		container.injectMembers(serviceProvider);

		//ClientService clientService=container.getInstance(ClientService.class);
		//TODO start it if not started ? clientService.start();		
		serviceProvider.start();
	}
	
	public void stop() throws Throwable{
		serviceProvider.stop();
		container.stop();
	}
	
	public static void main(String[] argv, Element[] element) throws Throwable{
		ApplicationConfig[] apps=null;
		if (element!=null){
			apps=ApplicationConfig.readFrom(element);
		}
		if (apps==null || apps.length==0){
			throw new IllegalStateException("No application having as a type 'fwk.client', 'fwk.agent' or 'fwk.registry' is configured to start. Please check your settings file.");
		}
		
		List<Thread> threads=new ArrayList<>();
		for (ApplicationConfig c : apps){
			Thread th=new Thread(){
				public void run(){
					try{
						Framework fwk=new Framework();
						fwk.arguments(argv).applicationConfig(c);
						fwk.start();
					} catch (Throwable e) {
						// TODO 
						e.printStackTrace();
					}
				}
			};
			threads.add(th);
			th.start();
		}
		
		threads.forEach(it->{
			try{
				it.join();
			}catch(Exception e){
				e.printStackTrace();
			}
		});
	}
}
