package microsofia.framework.service;

import io.atomix.Atomix;
import microsofia.container.module.atomix.IAtomixConfigurator;
import microsofia.framework.agent.AgentFilters;
import microsofia.framework.agent.AgentInfo;
import microsofia.framework.agent.AgentLookupConfiguration;
import microsofia.framework.client.ClientInfo;
import microsofia.framework.registry.RegistryInfo;
import microsofia.framework.registry.lookup.LookupRequest;
import microsofia.framework.registry.lookup.LookupResult;
import microsofia.framework.registry.lookup.LookupResultFilters;
import microsofia.rmi.ObjectAddress;

public class AtomixConfigurator implements IAtomixConfigurator{

	public AtomixConfigurator(){
	}

	@Override
	public void configureAtomix(Atomix atomix) {
		atomix.serializer().register(ObjectAddress.class,1989);
		atomix.serializer().register(ServiceInfo.class,1988);
		atomix.serializer().register(RegistryInfo.class,1987);
		atomix.serializer().register(AgentInfo.class,1986);
		atomix.serializer().register(ClientInfo.class,1985);
		atomix.serializer().register(LookupRequest.class,1984);
		atomix.serializer().register(LookupResult.class,1983);
		atomix.serializer().register(AgentFilters.QueueFilter.class,1982);
		atomix.serializer().register(LookupResultFilters.LookupResultByAgentPidFilter.class,1981);
		atomix.serializer().register(LookupResultFilters.LookupResultByClientPidFilter.class,1980);
		atomix.serializer().register(LookupResultFilters.LookupResultByQueueFilter.class,1979);
		atomix.serializer().register(AgentLookupConfiguration.class,1978);
	}
}
