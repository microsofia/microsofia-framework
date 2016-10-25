package microsofia.framework.client;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.atomix.AtomixClient;
import microsofia.container.module.atomix.Cluster;
import microsofia.container.module.atomix.ClusterConfiguration;
import microsofia.framework.FrameworkException;
import microsofia.framework.client.lookup.ClientLookupService;
import microsofia.framework.client.lookup.IClientLookupService;
import microsofia.framework.invoker.Invoker;
import microsofia.framework.map.Map;
import microsofia.framework.registry.lookup.ILookupService;
import microsofia.framework.service.AbstractService;
import microsofia.framework.service.AtomixConfigurator;

public abstract class AbstractClientService<SI extends ClientInfo> extends AbstractService<AtomixClient,SI>{
	private static Log log=LogFactory.getLog(AbstractClientService.class);
	@Inject
	@ClusterConfiguration(configurator={AtomixConfigurator.class},resources={Map.NonAnnotatedMap.class,Invoker.class})
	@Cluster("registry")
	protected AtomixClient atomix;
	@Inject
	@Named(KEY_LOOKUP_SERVICE)
	protected ILookupService lookupService;
	@Inject
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
	public AtomixClient getAtomix(){
		return atomix;
	}
	
	@Override
	public void start(){
		try{
			export();

			configureService();
	
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
		clientLookupService.close();
		super.stop();
	}
}
