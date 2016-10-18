package microsofia.framework.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;
import java.util.Date;
import org.hyperic.sigar.Sigar;
import microsofia.rmi.ObjectAddress;

public class ServiceInfo implements Externalizable{
	private static final long serialVersionUID = 0;
	protected ObjectAddress objectAddress;
	protected InetAddress inetAddress;
	protected String npid;
	protected long pid;
	protected Date startDate;

	public ServiceInfo(){
	}
	
	public ObjectAddress getObjectAddress() {
		return objectAddress;
	}

	public void setObjectAddress(ObjectAddress objectAddress) {
		this.objectAddress = objectAddress;
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress() throws Exception{
		this.inetAddress = InetAddress.getLocalHost();
	}
	
	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	public String getNPid() {
		return npid;
	}

	public void setNPid() {
		this.npid = ""+new Sigar().getPid();
	}
	
	public void setNPid(String npid) {
		this.npid = npid;
	}

	public long getPid() {
		return pid;
	}
	
	public void setPid(long p) {
		this.pid=p;
	}
	
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate() {
		this.startDate = new Date();
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(objectAddress);
		out.writeObject(inetAddress);
		out.writeUTF(npid);
		out.writeLong(pid);
		out.writeObject(startDate);
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	objectAddress=(ObjectAddress)in.readObject();
    	inetAddress=(InetAddress)in.readObject();
    	npid=in.readUTF();
    	pid=in.readLong();
    	startDate=(Date)in.readObject();    	
    }
    
    @Override
    public int hashCode(){
    	return objectAddress.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
    	if (!(o instanceof ServiceInfo)){
    		return false;
    	}
    	return ((ServiceInfo)o).objectAddress.equals(objectAddress);
    }
    
    @Override
    public String toString(){
    	return "Service[ObjectAddress:"+objectAddress+"][StartDate:"+startDate+"][NPid:"+npid+"][Pid:"+pid+"][InetAddress:"+inetAddress+"]";
    }
}
