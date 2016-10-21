package microsofia.framework.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.atomix.AtomixClient;
import io.atomix.catalyst.transport.Address;
import microsofia.container.application.PropertyConfig;
import microsofia.framework.FrameworkException;
import microsofia.framework.client.lookup.ClientLookupService;
import microsofia.framework.client.lookup.IClientLookupService;
import microsofia.framework.invoker.Invoker;
import microsofia.framework.map.Map;
import microsofia.framework.registry.lookup.ILookupService;
import microsofia.framework.service.AbstractService;

public abstract class AbstractClientService<SI extends ClientInfo> extends AbstractService<AtomixClient,SI>{
	private static Log log=LogFactory.getLog(AbstractClientService.class);
	protected ILookupService lookupService;
	protected ClientLookupService clientLookupService;
	
	protected AbstractClientService(){
	}
	
	public IClientLookupService getClientLookupService() {
		return clientLookupService;
	}

	public abstract ClientConfiguration getClientConfiguration();
	
	public ILookupService getLookupService(){
		return lookupService;
	}
	
	protected abstract void internalStart() throws Exception;

	protected abstract void internalStop() throws Exception;
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void start(){
		try{
			export();

			List<Address> adr=new ArrayList<>();
			for (ClientConfiguration.Registry r : getClientConfiguration().getRegistry()){
				adr.add(new Address(r.getHost(), r.getPort()));
			}

			Properties properties=PropertyConfig.toPoperties(getClientConfiguration().getProperties());
			atomix=AtomixClient.builder(properties).withResourceTypes((Class)Map.class,Invoker.class).build();
			configureSerializer();
			atomix.connect(adr).get();
			configureResources();
			
			lookupService=invoker.getProxy(ILookupService.class);
			clientLookupService=new ClientLookupService();//TODO: review all of that, when moving atomix to native module
			clientLookupService.setAbstractClientService(this);
			clientLookupService.setLookupService(lookupService);
	
			internalStart();
		}catch(Throwable th){
			throw new FrameworkException(th.getMessage(), th);
		}
	}
	
	@Override
	public void stop(){
		try{
			unexport();
		}catch(Throwable th){
			log.debug(th,th);
		}
		try{
			internalStop();
		}catch(Throwable th){
			log.debug(th,th);
		}
		try{	
			atomix.close().get();
		}catch(Throwable th){
			log.debug(th,th);
		}
		clientLookupService.close();
		super.stop();
	}
}
