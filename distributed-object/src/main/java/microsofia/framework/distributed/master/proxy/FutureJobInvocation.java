package microsofia.framework.distributed.master.proxy;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import microsofia.framework.distributed.master.IObjectAllocator;
import microsofia.framework.distributed.master.JobResult;

public class FutureJobInvocation<V> implements Future<V>{
	private IObjectAllocator objectAllocator; 
	private long jobId;
	private long pollingPeriod;
	private AtomicBoolean canceled;
	private AtomicReference<JobResult> result;
	
	public FutureJobInvocation(IObjectAllocator objectAllocator,long jobId,long pollingPeriod){
		this.objectAllocator=objectAllocator;
		this.jobId=jobId;
		this.pollingPeriod=pollingPeriod;
		canceled=new AtomicBoolean();
		result=new AtomicReference<JobResult>();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (result.get()!=null){
			return false;
		}
		canceled.set(true);
		try{
			objectAllocator.stopJob(jobId);
		}catch(Exception e){
			throw new RuntimeException(e.getMessage(), e);
		}
		return true;
	}

	@Override
	public boolean isCancelled() {
		return canceled.get();
	}

	@Override
	public boolean isDone() {
		if (result.get()!=null){
			return true;
		}
		
		try{
			result.set(objectAllocator.takeJobResult(jobId));
		}catch(Exception e){
			throw new RuntimeException(e.getMessage(), e);
		}

		return result.get()!=null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get() throws InterruptedException, ExecutionException {
		while (result.get()==null){
			try{
				result.set(objectAllocator.takeJobResult(jobId));
			}catch(Exception e){
				throw new RuntimeException(e.getMessage(), e);
			}
			Thread.sleep(pollingPeriod);
		}
		if (result.get()!=null){
			if (result.get().getError()!=null){
				try{
					Throwable th=result.get().getErrorAsThrowable();
					throw th;
				}catch(Throwable e){
					throw new RuntimeException(e.getMessage(), e);
				}
			}else{
				try{
					return (V)result.get().getResultAsObject();
				}catch(Throwable e){
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}
		return null;
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		long max=System.currentTimeMillis()+unit.toMillis(timeout);
		while (System.currentTimeMillis()<max && result.get()==null){
			try{
				result.set(objectAllocator.takeJobResult(jobId));
			}catch(Exception e){
				throw new RuntimeException(e.getMessage(), e);
			}
			Thread.sleep(pollingPeriod);
		}
		if (result.get()!=null){
			return get();
		}
		return null;
	}
}
