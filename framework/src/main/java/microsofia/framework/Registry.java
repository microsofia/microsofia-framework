package microsofia.framework;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import microsofia.framework.registry.RegistryService;

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
