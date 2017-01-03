package microsofia.framework.distributed.master.impl;

import java.util.Comparator;

import javax.inject.Singleton;

import microsofia.framework.distributed.master.Job;

@Singleton
public class JobComparator implements Comparator<Job>{
	private long time;
	
	public JobComparator(){
	}
	
	public void setTime(){
		this.time=System.currentTimeMillis();
	}

	@Override
	public int compare(Job j1, Job j2) {
		if (j1.getPriority()<j2.getPriority()){
			return -1;

		}else if (j1.getPriority()>j2.getPriority()){
			return 1;
			
		}else{
			long wait1=time-j1.getCreationTime();
			long wait2=time-j2.getCreationTime();
			
			long l1=j1.getWeigth()/wait1;
			long l2=j2.getWeigth()/wait2;
			
			if (l1<l2){
				return -1;
						
			}else if (l1>l2){
				return 1;
			
			}else{
				if (j1.getId()==j2.getId()){
					return 0;
				}
				if (j1.getCreationTime()<j2.getCreationTime()){
					return -1;
				}
				return 1;
			}
		}
	}		
}