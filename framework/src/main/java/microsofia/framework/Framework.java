package microsofia.framework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import io.atomix.AtomixClient;
import io.atomix.variables.DistributedLong;
import microsofia.container.ContainerBuilder;
import microsofia.container.ContainerImpl;
import microsofia.container.application.ApplicationConfig;
import microsofia.container.application.DefaultApplication;
import microsofia.container.application.DefaultApplicationProvider;
import microsofia.container.module.atomix.Cluster;
import microsofia.framework.agent.AgentConfiguration;
import microsofia.framework.agent.AgentInfo;
import microsofia.framework.agent.AgentService;
import microsofia.framework.agent.IAgentService;
import microsofia.framework.client.AbstractClientService;
import microsofia.framework.client.ClientConfiguration;
import microsofia.framework.client.ClientInfo;
import microsofia.framework.client.ClientService;
import microsofia.framework.client.IClientService;
import microsofia.framework.client.lookup.IClientLookupService;
import microsofia.framework.invoker.Invoker;
import microsofia.framework.map.Map;
import microsofia.framework.registry.RegistryInfo;
import microsofia.framework.registry.lookup.ILookupService;
import microsofia.framework.service.AbstractService;

public class Framework {
	private static Log log=LogFactory.getLog(Framework.class);
	private static final String FWK_REGISTRY 	= "fwk.registry";
	private static final String FWK_AGENT 		= "fwk.agent";
	private static final String FWK_CLIENT 		= "fwk.client";
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
	
	private void createClient() throws Exception{
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
		
		application.addModule(new AbstactClientModule(){
			
			@Singleton
			@Provides
			public AbstractClientService<?> getAbstractClientService(ClientService clientService){
				return (AbstractClientService<?>)clientService;
			}

			@Override
			protected void configure() {
				bind(IClientService.class).to(ClientService.class).asEagerSingleton();
				bind(ClientConfiguration.class).toInstance(clientConfiguration);
			}
			
		});
		application.parseClass(ClientService.class);
	}
	
	private void createAgent() throws Exception{
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
		
		application.addModule(new AbstactClientModule() {
			
			@Singleton
			@Provides
			public AbstractClientService<?> getAbstractClientService(AgentService agentService){
				return (AbstractClientService<?>)agentService;
			}
			
			@SuppressWarnings({"unchecked" })
			@Singleton
			@Provides
			@Named(AbstractService.KEY_AGENTS)
			public Map<Long, AgentInfo> getAgents(@Cluster("registry") AtomixClient atomix) throws Exception{
				return atomix.getResource(AbstractService.KEY_AGENTS,Map.class).get();
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
			for (Class<?> c: service.getInjectedClasses()){
				application.parseClass(c);
			}
		}
		if (service.getGuiceModules()!=null){
			service.getGuiceModules().forEach(application::addModule);
		}
		
		provider.addApplication(application);
		container.addApplicationProvider(provider);

		container.start();
		container.injectMembers(service);

		if (applicationConfig.getType().equals(FWK_AGENT)){
			Class<?> serviceClass=((Agent)service).getServiceClass();
			if (serviceClass!=null){
				Object serviceInstance=container.getInstance(serviceClass);
				AgentService agentService=container.getInstance(AgentService.class);
				agentService.setAgent(serviceInstance);
			}

		}else if (applicationConfig.getType().equals(FWK_CLIENT)){
			Class<?> serviceClass=((Client)service).getServiceClass();
			if (serviceClass!=null){
				Object clientInstance=container.getInstance(serviceClass);
				ClientService clientService=container.getInstance(ClientService.class);
				clientService.setClient(clientInstance);
			}
		}

		service.start();
	}
	
	public void stop() throws Throwable{
		service.stop();
		container.stop();
	}
	
	private abstract class AbstactClientModule extends AbstractModule {
		AbstactClientModule(){
		}

		@Singleton
		@Provides
		@Named(AbstractService.KEY_SERVICE_ID)
		public DistributedLong getServiceId(@Cluster("registry") AtomixClient atomix) throws Exception{
			return atomix.getLong(AbstractService.KEY_SERVICE_ID).get();
		}

		@SuppressWarnings({"unchecked" })
		@Singleton
		@Provides
		@Named(AbstractService.KEY_REGISTRIES)
		public Map<Long, RegistryInfo> getRegistries(@Cluster("registry") AtomixClient atomix) throws Exception{
			return atomix.getResource(AbstractService.KEY_REGISTRIES,Map.class).get();
		}

		@SuppressWarnings({"unchecked" })
		@Singleton
		@Provides
		@Named(AbstractService.KEY_CLIENTS)
		public Map<Long, ClientInfo> getClients(@Cluster("registry") AtomixClient atomix) throws Exception{
			return atomix.getResource(AbstractService.KEY_CLIENTS,Map.class).get();
		}

		@Singleton
		@Provides
		@Named(AbstractService.KEY_INVOKER)
		public Invoker getInvoker(@Cluster("registry") AtomixClient atomix) throws Exception{
			return atomix.getResource(AbstractService.KEY_INVOKER,Invoker.class).get();
		}

		@Singleton
		@Provides
		@Named(AbstractService.KEY_LOOKUP_SERVICE)
		public ILookupService getLookupService(@Named(AbstractService.KEY_INVOKER) Invoker invoker) throws Exception{
			return invoker.getProxy(ILookupService.class);
		}
		
		@Singleton
		@Provides
		public IClientLookupService getClientLookupService(AbstractClientService<?> clientService){
			return clientService.getClientLookupService();
		}

		@Singleton
		@Provides
		public ExecutorService getExecutorService(){
			return Executors.newCachedThreadPool();
		}
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
