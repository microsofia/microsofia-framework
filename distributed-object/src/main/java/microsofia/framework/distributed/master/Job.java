package microsofia.framework.distributed.master;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name="DO_JOB")
@Entity
public class Job implements Externalizable{
	public enum Status {CREATED, RUNNING, STOPPED, FINISHED};
	@Id
	@Column(name="JOB_ID")
	@GeneratedValue
	private long id;
	@Column(name="JOB_PRIORITY")
	private int priority;
	@Column(name="JOB_WEIGTH")
	private long weigth;
	@Column(name="JOB_METHOD_ID")
	private int method;
	@Column(name="JOB_ARGUMENTS")
	@Lob
	private byte[] arguments;
	@Column(name="JOB_STATUS")
	private Status status;
	@ManyToOne
	@JoinColumn(name="JOB_VO_ID")
	private VirtualObjectInfo virtualObjectInfo;
	@Column(name="CREATION_TIME")
	private long creationTime;
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="JOB_RESULT_ID")
	private JobResult jobResult;

	public Job(){
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public long getWeigth(){
		return weigth;
	}
	
	public void setWeigth(long l){
		weigth=l;
	}

	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		this.method = method;
	}

	public byte[] getArguments() {
		return arguments;
	}

	public void setArguments(byte[] arguments) {
		this.arguments = arguments;
	}
	
	public Object[] getArgumentsAsObjects() throws Exception{
		if (arguments!=null){
			return (Object[]) read(arguments);
		}else{
			return null;
		}
	}
		
	public void setArguments(Object[] args) throws Exception{
		if (args!=null){
			arguments=write(args);
		}else{
			arguments=null;
		}
	}

	public Status getStatus() {
		return status;
	}
	
	public boolean isStatusCreated(){
		return status.equals(Status.CREATED);
	}
	
	public boolean isStatusStopped(){
		return status.equals(Status.STOPPED);
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public void setStatusCreated() {
		this.status = Status.CREATED;
	}
	
	public void setStatusRunning() {
		this.status = Status.RUNNING;
	}
	
	public void setStatusStopped() {
		this.status = Status.STOPPED;
	}
	
	public void setStatusFinished() {
		this.status = Status.FINISHED;
	}

	public VirtualObjectInfo getVirtualObjectInfo() {
		return virtualObjectInfo;
	}

	public void setVirtualObjectInfo(VirtualObjectInfo virtualObjectInfo) {
		this.virtualObjectInfo = virtualObjectInfo;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}
	
	public void setCreationTime() {
		this.creationTime = System.currentTimeMillis();
	}

	public JobResult getJobResult() {
		return jobResult;
	}

	public void setJobResult(JobResult jobResult) {
		this.jobResult = jobResult;
	}

	public static byte[] write(Object o) throws Exception{
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos=new ObjectOutputStream(bos);
		oos.writeObject(o);
		return bos.toByteArray();
	}
	
	public static Object read(byte[] b) throws Exception{
		ByteArrayInputStream bis=new ByteArrayInputStream(b);
		ObjectInputStream ois=new ObjectInputStream(bis);
		return ois.readObject();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(id);
		out.writeInt(priority);
		out.writeLong(weigth);
		out.writeInt(method);
		if (arguments!=null){
			out.writeInt(arguments.length);
			out.write(arguments);
		}else{
			out.writeInt(0);
		}
		out.writeObject(status);
		out.writeObject(virtualObjectInfo);
		out.writeLong(creationTime);
		out.writeObject(jobResult);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id=in.readLong();
		priority=in.readInt();
		weigth=in.readLong();
		method=in.readInt();
		int nb=in.readInt();
		if (nb>0){
			arguments=new byte[nb];
			in.readFully(arguments);
		}
		status=(Status)in.readObject();
		virtualObjectInfo=(VirtualObjectInfo)in.readObject();
		creationTime=in.readLong();
		jobResult=(JobResult)in.readObject();
	}
	
	@Override
	public String toString(){
		return "Job[Id:"+id+"][Priority:"+priority+"][Weigth:"+weigth+"][Method:"+method+"][Status:"+status+"][VirtualObjectInfo:["+virtualObjectInfo+"][CreationTime:"+creationTime+"]";
	}
}
