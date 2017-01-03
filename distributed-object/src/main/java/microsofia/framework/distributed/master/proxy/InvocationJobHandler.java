package microsofia.framework.distributed.master.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

import javax.inject.Inject;

import microsofia.container.Container;
import microsofia.framework.distributed.master.IObjectAllocator;
import microsofia.framework.distributed.master.Job;
import microsofia.framework.distributed.master.VirtualObjectInfo;
import microsofia.framework.distributed.master.impl.ClassMetadata;

public class InvocationJobHandler implements InvocationHandler{
	@Inject 
	private Container container;
	private ProxyBuilder proxyBuilder;
	private Class<?> theClass;
	private IObjectAllocator objectAllocator;
	private ClassMetadata classMetadata;
	private int setupMethod;
	private Object[] setupArguments;
	private int tearDownMethod;
	private Object[] tearDownArguments;
	private VirtualObjectInfo virtualObjectInfo;
	
	public InvocationJobHandler(ProxyBuilder proxyBuilder,Class<?> c,IObjectAllocator objectAllocator) {
		this.proxyBuilder=proxyBuilder;
		this.theClass=c;
		this.objectAllocator=objectAllocator;
		this.classMetadata=new ClassMetadata(theClass);
	}
	
	private Object getReturnedObject(Object proxy,Method method){
		if (method.getReturnType().equals(theClass)){
			return proxy;
		}
		return null;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Setup setup=method.getAnnotation(Setup.class);
		if (setup!=null){
			setupMethod=classMetadata.hashCode(method);
			setupArguments=args;
			return getReturnedObject(proxy, method);
		}
		
		TearDown tearDown=method.getAnnotation(TearDown.class);
		if (tearDown!=null){
			tearDownMethod=classMetadata.hashCode(method);
			tearDownArguments=args;
			return getReturnedObject(proxy, method);
		}
		
		AllocateObject allocateObject=method.getAnnotation(AllocateObject.class);
		if (allocateObject!=null){
			if (virtualObjectInfo!=null){
				throw new IllegalStateException("Cannot create a virtual object as another one is already created:"+virtualObjectInfo);
			}
			try{
				virtualObjectInfo=objectAllocator.createVirtualObject(setupMethod, Job.write(setupArguments), tearDownMethod, Job.write(tearDownArguments));
			}finally{
				setupArguments=null;
				tearDownArguments=null;
			}
			return getReturnedObject(proxy, method);
		}
		
		FreeObject freeObject=method.getAnnotation(FreeObject.class);
		if (freeObject!=null){
			if (virtualObjectInfo==null){
				throw new IllegalStateException("There is no virtual object to free.");
			}
			objectAllocator.removeVirtualObject(virtualObjectInfo.getId());
			virtualObjectInfo=null;
			return getReturnedObject(proxy, method);
		}
		
		Job job=new Job();
		job.setMethod(classMetadata.hashCode(method));
		job.setArguments(args);
		
		Priority p=method.getAnnotation(Priority.class);
		if (p!=null){
			job.setPriority(p.value());
		}else{
			job.setPriority(proxyBuilder.getPriority());
		}
		
		Weigth w=method.getAnnotation(Weigth.class);
		if (w!=null){
			job.setWeigth(w.value());
		}else{
			job.setWeigth(proxyBuilder.getWeigth());
		}
		
		long jobId=objectAllocator.addJob((virtualObjectInfo!=null ? virtualObjectInfo.getId() : -1), job);
		FutureJobInvocation<Object> futureJobInvocation=new FutureJobInvocation<>(objectAllocator,jobId,proxyBuilder.getPollingPeriod());
		container.injectMembers(futureJobInvocation);

		if (Future.class.isAssignableFrom(method.getReturnType())){
			return futureJobInvocation;
		}
		return futureJobInvocation.get();
	}
}
