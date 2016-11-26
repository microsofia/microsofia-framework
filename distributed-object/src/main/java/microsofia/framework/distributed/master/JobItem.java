package microsofia.framework.distributed.master;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class JobItem implements Externalizable{
	private Job job;
	private int setupMethod;
	private byte[] setupArguments;

	public JobItem(){
	}
	
	public void setSetup(VirtualObjectInfo virtualObjectInfo){
		setupMethod=virtualObjectInfo.getSetupMethod();
		setupArguments=virtualObjectInfo.getSetupArguments();
	}
	
	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public int getSetupMethod() {
		return setupMethod;
	}

	public void setSetupMethod(int setupMethod) {
		this.setupMethod = setupMethod;
	}

	public byte[] getSetupArguments() {
		return setupArguments;
	}

	public void setSetupArguments(byte[] setupArguments) {
		this.setupArguments = setupArguments;
	}
	
	public Object[] getSetupArgumentsAsObjects() throws Exception{
		if (setupArguments!=null){
			return (Object[]) Job.read(setupArguments);
		}else{
			return null;
		}
	}
		
	public void setSetupArguments(Object[] args) throws Exception{
		if (args!=null){
			setupArguments=Job.write(args);
		}else{
			setupArguments=null;
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(job);
		
		out.writeInt(setupMethod);
		if (setupArguments!=null){
			out.writeInt(setupArguments.length);
			out.write(setupArguments);
		}else{
			out.writeInt(0);
		}		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		job=(Job)in.readObject();
		
		setupMethod=in.readInt();
		int nb=in.readInt();
		if (nb>0){
			setupArguments=new byte[nb];
			in.readFully(setupArguments);
		}		
	}
}
