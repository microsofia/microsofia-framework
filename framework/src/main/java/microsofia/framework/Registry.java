package microsofia.framework;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import microsofia.framework.invoker.InvokerServiceAdapter;
import microsofia.framework.registry.RegistryConfiguration;
import microsofia.framework.registry.RegistryService;
import microsofia.framework.registry.lookup.LockFactory;
import microsofia.framework.registry.lookup.LookupService;
import microsofia.framework.registry.lookup.strategy.CompositeStrategy;

public class Registry implements Service{
	@Inject
	private RegistryService registryService;
	@Inject
	private RegistryConfiguration registryConfiguration;

	public Registry(){
	}
	
	public RegistryService getRegistryService() {
		return registryService;
	}

	public void setRegistryService(RegistryService registryService) {
		this.registryService = registryService;
	}

	public RegistryConfiguration getRegistryConfiguration() {
		return registryConfiguration;
	}

	public void setRegistryConfiguration(RegistryConfiguration registryConfiguration) {
		this.registryConfiguration = registryConfiguration;
	}

	@Override
	public Type getType() {
		return Type.REGISTRY;
	}

	@Override
	public String getImplementation() {
		return "";
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
			public LookupService getLookupService(CompositeStrategy compositeStrategy){
				return new LookupService(compositeStrategy);
			}
			
			@Singleton
			@Provides
			public CompositeStrategy getCompositeStrategy(){
				return new CompositeStrategy();
			}
			
			@Singleton
			@Provides
			public LockFactory getLockFactory(){
				return new LockFactory();
			}
			
			@Singleton
			@Provides
			public InvokerServiceAdapter getInvokerServiceAdapter(LookupService lookupService){
				return new InvokerServiceAdapter(lookupService);
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
