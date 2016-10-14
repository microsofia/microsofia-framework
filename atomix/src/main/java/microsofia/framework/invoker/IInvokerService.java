package microsofia.framework.invoker;

public interface IInvokerService {

	public void start();
	
	public void invoke(Invoker invoker,InvocationRequest request);
	
	public void stopInvocation(Invoker invoker,InvocationRequest request);
	
	public void stop();
}
