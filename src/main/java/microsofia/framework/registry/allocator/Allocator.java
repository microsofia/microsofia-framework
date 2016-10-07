package microsofia.framework.registry.allocator;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import io.atomix.catalyst.concurrent.BlockingFuture;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.resource.AbstractResource;
import io.atomix.resource.ResourceTypeInfo;

@ResourceTypeInfo(id=20, factory=AllocatorFactory.class)
public class Allocator extends AbstractResource<Allocator> {
	private AtomicInteger globalId;
	private Map<Integer,CompletableFuture<AllocationResponse>> futures;
	private IAllocatorLifecycle allocatorLifecycle;

	public Allocator(CopycatClient client, Properties options) {
		super(client, options);
		globalId=new AtomicInteger();
		futures=new Hashtable<>();
		client.<AllocationResponse>onEvent("allocationResponse", it->{
			System.out.println("answer received client side: "+it);//+it.getRequest().getId());
			//handleAllocationResponse(it);
		});
		client.onEvent("startAllocating", this::startAllocating);
		client.<AllocationRequest>onEvent("allocate", this::internalAllocate);
		client.onEvent("stopAllocating", this::stopAllocating);
	}
	
	private void handleAllocationResponse(AllocationResponse response) {
		System.out.println("answer received client side: "+response.getRequest().getId());
	    CompletableFuture<AllocationResponse> future = futures.get(response.getRequest().getId());
	    if (future != null) {
	    	future.complete(response);
	    }
	}

	//TODO what to do when issue on session
	public CompletableFuture<AllocationResponse> allocate(AllocationRequest request) {
		CompletableFuture<AllocationResponse> future = new BlockingFuture<>();
	    int id = globalId.incrementAndGet();
	    request.setId(id);
	    request.setSessionId(client.session().id());
	    futures.put(id, future);
	    client.submit(new AllocatorCommands.AllocationCommand(request)).whenComplete((result, error) -> {
	    	if (error != null) {
	    		futures.remove(id);
	    		future.completeExceptionally(error);
		    }
	    });
		return future;
	}
	
	private void startAllocating(){
		allocatorLifecycle.startAllocating();
	}
	
	private void internalAllocate(AllocationRequest request){
		AllocationResponse response=allocatorLifecycle.allocate(request);
		System.out.println("Server allcated:"+response);
		try {
			client.submit(new AllocatorCommands.SetAllocationResponseCommand(response)).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Response sent!");
	}

	private void stopAllocating(){
		allocatorLifecycle.stopAllocating();
	}

	/*TODO 
	 * 	listen to session closing and leader in order to setallocator/stop current allocation
	 * 
	 */
	public CompletableFuture<Void> setAllocatorLifecyle(IAllocatorLifecycle alc){
		this.allocatorLifecycle=alc;
		return client.submit(new AllocatorCommands.SetAllocatorCommand());
	}
}
