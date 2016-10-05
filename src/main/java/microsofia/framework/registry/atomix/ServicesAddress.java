package microsofia.framework.registry.atomix;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import io.atomix.copycat.client.CopycatClient;
import io.atomix.resource.AbstractResource;
import io.atomix.resource.ResourceTypeInfo;
import microsofia.framework.service.ServiceAddress;

@ResourceTypeInfo(id=19, factory=ServicesAddressFactory.class)
public class ServicesAddress extends AbstractResource<ServicesAddress> {

	  public ServicesAddress(CopycatClient client, Properties options) {
	    super(client, options);
	  }

	  @Override
	  public CompletableFuture<ServicesAddress> open() {
	    return super.open().thenApply(result -> {
//	      client.onEvent("lock", this::handleEvent);
//	      client.onEvent("fail", this::handleFail);
	      return result;
	    });
	  }

//	  private void handleEvent(LockCommands.LockEvent event) {
//	    CompletableFuture<Long> future = futures.get(event.id());
//	    if (future != null) {
//	      this.lock = event.id();
//	      future.complete(event.version());
//	    }
//	  }
//
//	  private void handleFail(LockCommands.LockEvent event) {
//	    CompletableFuture<Long> future = futures.get(event.id());
//	    if (future != null) {
//	      future.complete(null);
//	    }
//	  }

	  public CompletableFuture<Void> add(ServiceAddress sa) {
	    return client.submit(new Commands.AddCommand(sa));
	  }

	  public CompletableFuture<Void> remove(ServiceAddress sa) {
		  return client.submit(new Commands.RemoveCommand(sa));
	  }
}
