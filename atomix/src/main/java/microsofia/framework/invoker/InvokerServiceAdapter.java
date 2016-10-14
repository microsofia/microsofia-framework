package microsofia.framework.invoker;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InvokerServiceAdapter implements IInvokerService {
	private static Log log=LogFactory.getLog(InvokerServiceAdapter.class);
	protected Object service;
	protected ClassMetadata classMetadata;
	
	public InvokerServiceAdapter(Object service){
		this.service=service;
		classMetadata=new ClassMetadata(service.getClass());
	}		

	@Override
	public void start(){
		if (service instanceof IStartable){
			((IStartable)service).start();
		}
	}
	
	@Override
	public void invoke(Invoker invoker,InvocationRequest request){
		InvocationResponse response=new InvocationResponse(request);
		try{
			Method method=classMetadata.getMethod(request.getMethod());
			if (method==null){
				throw new IllegalArgumentException("Method with hashcode "+request.getMethod()+" not found in object "+service);
			}
			response.setResult(method.invoke(service, request.getArguments()));
		}catch(Throwable th){
			response.setError(th);
		}
		try{
			invoker.setInvocationResponse(response);
		}catch(Exception e){
			log.error(e, e);
		}
	}
	
	@Override
	public void stopInvocation(Invoker invoker,InvocationRequest request){
		//TODO how to stop one request ?
	}
	
	@Override
	public void stop(){
		if (service instanceof IStoppable){
			((IStoppable)service).stop();
		}
	}

	public static interface IStartable{
		public void start();
	}

	public static interface IStoppable{
		public void stop();
	}
}
