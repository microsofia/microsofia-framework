package microsofia.framework;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import io.atomix.Atomix;
import io.atomix.group.DistributedGroup;
import io.atomix.variables.DistributedLong;
import microsofia.container.module.atomix.Cluster;
import microsofia.framework.agent.AgentInfo;
import microsofia.framework.client.ClientInfo;
import microsofia.framework.invoker.Invoker;
import microsofia.framework.map.Map;
import microsofia.framework.registry.RegistryInfo;
import microsofia.framework.registry.RegistryService;
import microsofia.framework.registry.lookup.LookupResult;
import microsofia.framework.service.AbstractService;

@Singleton
public class Registry implements Service{
	@Inject
	private RegistryService registryService;

	public Registry(){
	}
	
	public RegistryService getRegistryService() {
		return registryService;
	}

	public void setRegistryService(RegistryService registryService) {
		this.registryService = registryService;
	}

	@Override
	public Type getType() {
		return Type.REGISTRY;
	}

	@Override
	public List<AbstractModule> getGuiceModules() {
		return Arrays.asList(new AbstractModule() {
			
			@Override
			protected void configure() {
			}
			
			@Singleton
			@Provides
			public ExecutorService getExecutorService(){
				return Executors.newCachedThreadPool();
			}
			
			@Singleton
			@Provides
			@Named(AbstractService.KEY_LOOKUP_ID)
			public DistributedLong getGlobalLookupId(@Cluster("registry") Atomix atomix) throws Exception{
				return atomix.getLong(AbstractService.KEY_LOOKUP_ID).get();
			}
			
			@Singleton
			@Provides
			@Named(AbstractService.KEY_INVOKER_GROUP)
			public DistributedGroup getGroup(@Cluster("registry") Atomix atomix) throws Exception{
				return atomix.getGroup(AbstractService.KEY_INVOKER_GROUP).get();
			}
			
			@SuppressWarnings({"unchecked" })
			@Singleton
			@Provides
			@Named(AbstractService.KEY_AGENTS)
			public Map<Long, AgentInfo> getAgents(@Cluster("registry") Atomix atomix) throws Exception{
				return atomix.getResource(AbstractService.KEY_AGENTS,Map.class).get();
			}
			
			@SuppressWarnings({"unchecked" })
			@Singleton
			@Provides
			@Named(AbstractService.KEY_LOOKUP_RESULT)
			public Map<Long, LookupResult> getLookupResults(@Cluster("registry") Atomix atomix) throws Exception{
				return atomix.getResource(AbstractService.KEY_LOOKUP_RESULT,Map.class).get();
			}
			
			@Singleton
			@Provides
			@Named(AbstractService.KEY_SERVICE_ID)
			public DistributedLong getServiceId(@Cluster("registry") Atomix atomix) throws Exception{
				return atomix.getLong(AbstractService.KEY_SERVICE_ID).get();
			}
			
			@SuppressWarnings({"unchecked" })
			@Singleton
			@Provides
			@Named(AbstractService.KEY_REGISTRIES)
			public Map<Long, RegistryInfo> getRegistries(@Cluster("registry") Atomix atomix) throws Exception{
				return atomix.getResource(AbstractService.KEY_REGISTRIES,Map.class).get();
			}
			
			@SuppressWarnings({"unchecked" })
			@Singleton
			@Provides
			@Named(AbstractService.KEY_CLIENTS)
			public Map<Long, ClientInfo> getClients(@Cluster("registry") Atomix atomix) throws Exception{
				return atomix.getResource(AbstractService.KEY_CLIENTS,Map.class).get();
			}

			@Singleton
			@Provides
			@Named(AbstractService.KEY_INVOKER)
			public Invoker getInvoker(@Cluster("registry") Atomix atomix) throws Exception{
				return atomix.getResource(AbstractService.KEY_INVOKER,Invoker.class).get();
			}			
		});
	}

	@Override
	public List<Class<?>> getInjectedClasses() {
		return Arrays.asList(RegistryService.class);
	}

	@Override
	public void start() throws Exception {
		registryService.start();
	}

	@Override
	public void stop() throws Exception {
		registryService.stop();
	}
}
