package microsofia.framework.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import io.atomix.AtomixClient;
import io.atomix.catalyst.transport.Address;
import microsofia.container.application.PropertyConfig;
import microsofia.framework.FrameworkException;
import microsofia.framework.invoker.Invoker;
import microsofia.framework.map.Map;
import microsofia.framework.registry.IRegistryService;
import microsofia.framework.registry.lookup.ILookupService;
import microsofia.framework.service.Service;
import microsofia.framework.service.ServiceAddress;

public abstract class AbstractClient extends Service{
	protected AtomixClient atomixClient;
	protected Map<ServiceAddress,ServiceAddress> registryAddresses;
	protected Map<ServiceAddress,ServiceAddress> serviceAddresses;
	protected Invoker invoker;
	protected ILookupService lookupService;
	
	protected AbstractClient(){
	}
	
	public abstract ClientConfiguration getClientConfiguration();
	
	protected abstract String getServiceAddressMap();

	@Override
	@SuppressWarnings("unchecked")
	public void start(){
		try{
			export();
			init();
			
			List<Address> adr=new ArrayList<>();
			for (ClientConfiguration.Registry r : getClientConfiguration().getRegistry()){
				adr.add(new Address(r.getHost(), r.getPort()));
			}
			
			Properties properties=PropertyConfig.toPoperties(getClientConfiguration().getProperties());
			atomixClient=AtomixClient.builder(properties).withResourceTypes((Class)Map.class,Invoker.class).build();
			atomixClient.serializer().register(ServiceAddress.class,1986);
			atomixClient.connect(adr).get();
	
			registryAddresses=atomixClient.getResource("registries", Map.class).get();
			serviceAddresses=atomixClient.getResource(getServiceAddressMap(), Map.class).get();
			invoker=atomixClient.getResource("invoker", Invoker.class).get();
			lookupService=invoker.getProxy(ILookupService.class);
	
			serviceAddresses.put(serviceAddress,serviceAddress).get();
			serviceAddresses.get(serviceAddress).get();
		}catch(Throwable th){
			throw new FrameworkException(th.getMessage(), th);
		}
	}
	
	public List<IRegistryService> getRegistries() throws Exception{
		return getProxies(IRegistryService.class, registryAddresses);
	}
	
	@Override
	public void stop(){
		unexport();
	}
}
