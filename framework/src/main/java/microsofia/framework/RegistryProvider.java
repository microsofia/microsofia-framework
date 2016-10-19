package microsofia.framework;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.google.inject.AbstractModule;

import microsofia.framework.registry.IRegistryService;
import microsofia.framework.registry.RegistryConfiguration;
import microsofia.framework.registry.RegistryService;

public class RegistryProvider implements IServiceProvider{
	@Inject
	private RegistryService registryService;
	@Inject
	private RegistryConfiguration registryConfiguration;

	public RegistryProvider(){
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
		return null;
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
