package microsofia.framework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

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
	private static Log log=LogFactory.getLog(Framework.class);
	private static final String FWK_REGISTRY = "fwk.registry";
	private static final String FWK_AGENT = "fwk.agent";
	private static final String FWK_CLIENT = "fwk.client";
	protected String[] args;
	protected ApplicationConfig applicationConfig;
	protected Service service;
	protected DefaultApplication application;
	protected ContainerImpl container;
	
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
	
	private void createClient(){
		ClientConfiguration clientConfiguration;
		try{
			clientConfiguration=ClientConfiguration.createClientConfiguration(applicationConfig.getElement());
		}catch (Exception e) {
			throw new FrameworkException("Cannot read client configuration in settings file.",e);
		}
		if (clientConfiguration==null){
			throw new FrameworkException("Missing client configuration in settings file.");
		}
		
		ServiceLoader<Client> serviceLoader=ServiceLoader.load(Client.class, getClass().getClassLoader());
		Iterator<Client> iterator=serviceLoader.iterator();

		while (iterator.hasNext()){
			Client c=iterator.next();
			if (c.getImplementation().equals(clientConfiguration.getImplementation())){
				service=c;
				break;
			}
		}
		if (service==null){
			throw new FrameworkException("Couldn't load client configuration with implementation "+clientConfiguration.getImplementation());
		}
		
		application.addModule(new AbstractModule() {

			@Singleton
			@Provides
			public ExecutorService getExecutorService(){
				return Executors.newCachedThreadPool();
			}
			
			@Override
			protected void configure() {
				bind(IClientService.class).to(ClientService.class).asEagerSingleton();
				bind(ClientConfiguration.class).toInstance(clientConfiguration);
			}
		});
		application.parseClass(ClientService.class);
	}
	
	private void createAgent(){
		AgentConfiguration agentConfiguration;
		try{
			agentConfiguration=AgentConfiguration.createAgentConfiguration(applicationConfig.getElement());
		}catch (Exception e) {
			throw new FrameworkException("Cannot read agent configuration in settings file.",e);
		}
		if (agentConfiguration==null){
			throw new FrameworkException("Missing agent configuration in settings file.");
		}
		
		ServiceLoader<Agent> serviceLoader=ServiceLoader.load(Agent.class, getClass().getClassLoader());
		Iterator<Agent> iterator=serviceLoader.iterator();

		while (iterator.hasNext()){
			Agent c=iterator.next();
			if (c.getImplementation().equals(agentConfiguration.getImplementation())){
				service=c;
				break;
			}
		}
		if (service==null){
			throw new FrameworkException("Couldn't load agent configuration with implementation "+agentConfiguration.getImplementation());
		}
		
		application.addModule(new AbstractModule() {

			@Singleton
			@Provides
			public ExecutorService getExecutorService(){
				return Executors.newCachedThreadPool();
			}
			
			@Override
			protected void configure() {
				bind(IAgentService.class).to(AgentService.class).asEagerSingleton();
				bind(AgentConfiguration.class).toInstance(agentConfiguration);
			}
		});
		application.parseClass(AgentService.class);
	}
	
	private void createRegistry(){
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
		
		service=new Registry();
	}

	public void start() throws Throwable{
		if (applicationConfig.getType().equals(FWK_CLIENT)){
			application=new DefaultApplication(applicationConfig.getType());
			createClient();

		}else if (applicationConfig.getType().equals(FWK_AGENT)){
			application=new DefaultApplication(applicationConfig.getType());
			createAgent();
			
		}else if (applicationConfig.getType().equals(FWK_REGISTRY)){
			application=new DefaultApplication(applicationConfig.getType());
			createRegistry();
			
		}else{
			throw new IllegalArgumentException("Framework cannot start an application which is not of type fwk.client, fwk.agent or fwk.registry. Found:"+applicationConfig.getType());
		}

		ContainerBuilder builder=new ContainerBuilder();
		builder.arguments(args).applicationConfig(applicationConfig);
		container=(ContainerImpl)builder.build();

		DefaultApplicationProvider provider=new DefaultApplicationProvider();

		if (service.getInjectedClasses()!=null){
			service.getInjectedClasses().forEach(application::parseClass);
		}
		if (service.getGuiceModules()!=null){
			service.getGuiceModules().forEach(application::addModule);
		}
		
		provider.addApplication(application);
		container.addApplicationProvider(provider);

		container.start();
		container.injectMembers(service);
		service.start();
	}
	
	public void stop() throws Throwable{
		service.stop();
		container.stop();
	}
	
	public static void main(String[] argv, Element[] element) throws Throwable{
		ApplicationConfig[] apps=null;
		if (element!=null){
			apps=ApplicationConfig.readFrom(element);
		}
		if (apps==null || apps.length==0){
			throw new IllegalStateException("No application found. Please check your settings file.");
		}
		
		Vector<Throwable> ths=new Vector<>();
		List<Thread> threads=new ArrayList<>();
		for (ApplicationConfig c : apps){
			Thread th=new Thread(){
				public void run(){
					try{
						Framework fwk=new Framework();
						fwk.arguments(argv).applicationConfig(c);
						fwk.start();
					} catch (Throwable e) {
						e.printStackTrace();
						log.error(e,e);
						ths.add(e);
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
				log.error(e,e);
			}
		});
		if (ths.size()>0){
			throw ths.get(0);//should throw all of them
		}
	}
}
