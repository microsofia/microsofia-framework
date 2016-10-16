package microsofia.framework.service;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;

import org.hyperic.sigar.Sigar;

public class ServiceInfo implements Serializable{
	private static final long serialVersionUID = 0;
	private InetAddress inetAddress;
	private String npid;
	private Date startDate;

	public ServiceInfo(){
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

	public String getPid() {
		return npid;
	}

	public void setPid() {
		this.npid = ""+new Sigar().getPid();
	}
	
	public void setPid(String npid) {
		this.npid = npid;
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
	
	public String toString(){
		return "[StartDate:"+startDate+"][Pid:"+npid+"][InetAddress:"+inetAddress+"]";
	}
}
