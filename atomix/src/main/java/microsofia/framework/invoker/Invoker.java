package microsofia.framework.invoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import io.atomix.catalyst.concurrent.BlockingFuture;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.resource.AbstractResource;
import io.atomix.resource.ResourceTypeInfo;

@ResourceTypeInfo(id=20, factory=InvokerFactory.class)
public class Invoker extends AbstractResource<Invoker> {
	private AtomicLong globalId;
	private Map<Long,CompletableFuture<InvocationResponse>> futures;
	private IInvokerService invokerService;
	private boolean closeExecutor;
	private ExecutorService executorService;

	public Invoker(CopycatClient client, Properties options) {
		super(client, options);
		futures=new Hashtable<>();
	}
	
	public void setExecutorService(ExecutorService executorService){
		this.executorService=executorService;
	}
	
	private synchronized ExecutorService getExecutorService(){
		if (executorService==null){
			closeExecutor=true;
			executorService=Executors.newCachedThreadPool();
		}
		return executorService;
	}
	
	@Override
	public CompletableFuture<Invoker> open() {
		return super.open().thenApply(result -> {
			client.<InvocationResponse>onEvent("response", this::response);
			client.onEvent("start", this::notifyStart);
			client.<InvocationRequest>onEvent("invoke", this::invokeService);
			client.<InvocationRequest>onEvent("stopInvocation", this::stopInvocation);
			client.onEvent("stop", this::notifyStop);
			return result;
	    }).thenApply(it->{
	    	it.initialize();
	    	return it;
	    });
	}

	@Override
	public CompletableFuture<Void> close() {
		return super.close().thenRun(()->{
			if (closeExecutor){
				executorService.shutdown();
			}
		});
	}
	
	private void initialize(){
		try{
			globalId=new AtomicLong(client.submit(new InvokerCommands.GetMaxId()).get());
		}catch(Exception e){
			throw new IllegalStateException(e.getMessage(), e);
		}
		
		onStateChange(state->{
			if (state.equals(State.CLOSED)){
				Exception exception=new Exception("Session is closed.");
			    futures.values().forEach(it->{
			    	it.completeExceptionally(exception);
			    });
			    futures.clear();
			}
		});
	}
	
	public CompletableFuture<Void> setInvokerService(IInvokerService is){
		this.invokerService=is;
		return client.submit(new InvokerCommands.SetInvokerCommand());
	}
	
	public CompletableFuture<InvocationResponse> invoke(InvocationRequest request) {
		CompletableFuture<InvocationResponse> future = new BlockingFuture<>();
	    long id = globalId.incrementAndGet();
	    request.setId(id);
	    request.setSessionId(client.session().id());
	    futures.put(id, future);
	    client.submit(new InvokerCommands.InvokeCommand(request)).whenComplete((result, error) -> {
	    	if (error != null) {
	    		futures.remove(id);
	    		future.completeExceptionally(error);
		    }
	    });
		return future;
	}
	
	private void notifyStart(){
		getExecutorService().submit(new Runnable() {
			@Override
			public void run() {
				invokerService.start();
			}
		});
	}
	
	private void invokeService(InvocationRequest request){
		getExecutorService().submit(new Runnable() {
			@Override
			public void run() {
				invokerService.invoke(Invoker.this,request);
			}
		});
	}
	
	private void stopInvocation(InvocationRequest request){
		getExecutorService().submit(new Runnable() {
			@Override
			public void run() {
				invokerService.stopInvocation(Invoker.this,request);
			}
		});
	}
	
	public void setInvocationResponse(InvocationResponse response) throws Exception{
		client.submit(new InvokerCommands.SetInvocationResponseCommand(response)).get();
	}
	
	private void response(InvocationResponse response) {
	    CompletableFuture<InvocationResponse> future = futures.get(response.getRequest().getId());
	    if (future != null) {
	    	future.complete(response);
	    }//caller thread gone, response lost
	}

	private void notifyStop(){
		getExecutorService().submit(new Runnable() {
			@Override
			public void run() {
				invokerService.stop();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getProxy(Class<T> c){
		T proxy=(T)Proxy.newProxyInstance(c.getClassLoader(), new Class<?>[]{c}, new InvocationHandler() {
			ClassMetadata classMetadata=new ClassMetadata(c);
			
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				InvocationRequest request=new InvocationRequest();
				request.setArguments(args);
				request.setMethod(classMetadata.hashCode(method));
				InvocationResponse response=Invoker.this.invoke(request).get();
				if (response.getError()!=null){
					throw response.getError();
				}
				return response.getResult();
			}
		});
		return proxy;
	}
}