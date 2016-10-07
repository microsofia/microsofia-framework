package microsofia.framework.registry.typology;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import io.atomix.copycat.client.CopycatClient;
import io.atomix.resource.AbstractResource;
import io.atomix.resource.ResourceTypeInfo;
import microsofia.framework.service.ServiceAddress;

@ResourceTypeInfo(id=19, factory=TypologyFactory.class)
public class Typology extends AbstractResource<Typology> {

	  public Typology(CopycatClient client, Properties options) {
	    super(client, options);
	  }

	  //TODO what to do when issue on session
	  public CompletableFuture<Void> addAgent(ServiceAddress sa) {
		  return client.submit(new TypologyCommands.AddAgent(sa));
	  }
	  
	  public CompletableFuture<List<ServiceAddress>> getAgents() {
		  return client.submit(new TypologyCommands.GetAgentsQuery());
	  }

	  public CompletableFuture<Void> removeAgent(ServiceAddress sa) {
		  return client.submit(new TypologyCommands.RemoveAgent(sa));
	  }
	  
	  public CompletableFuture<Void> addRegistry(ServiceAddress sa) {
	    return client.submit(new TypologyCommands.AddRegistry(sa));
	  }

	  public CompletableFuture<Void> removeRegistry(ServiceAddress sa) {
		  return client.submit(new TypologyCommands.RemoveRegistry(sa));
	  }

	  public CompletableFuture<Void> addClient(ServiceAddress sa) {
		  return client.submit(new TypologyCommands.AddClient(sa));
	  }

	  public CompletableFuture<Void> removeClient(ServiceAddress sa) {
		  return client.submit(new TypologyCommands.RemoveClient(sa));
	  }
}
