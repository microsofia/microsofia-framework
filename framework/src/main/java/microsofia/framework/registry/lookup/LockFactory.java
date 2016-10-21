package microsofia.framework.registry.lookup;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class LockFactory {
	private Map<String, Lock> locks;
	
	public LockFactory(){
		locks=new HashMap<>();
	}
	
	public Object getLock(String n){
		Lock lock;
		synchronized(this){
			lock=locks.get(n);
			if (lock==null){
				lock=new Lock(n);
				locks.put(n, lock);
			}
			lock.increment();
		}
		return lock;
	}
	
	public void freeLock(Object o){
		Lock lock=(Lock)o;
		synchronized(this){
			if (lock.decrement()){
				locks.remove(lock.key);
			}
		}
	}

	private static class Lock{
		private String key;
		private int nb;
		
		Lock(String n){
			key=n;
		}
		
		void increment(){
			nb++;
		}
		
		boolean decrement(){
			nb--;
			return nb==0;
		}
	}
}
