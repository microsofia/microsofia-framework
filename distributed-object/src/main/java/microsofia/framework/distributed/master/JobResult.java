package microsofia.framework.distributed.master;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name="DO_JOB_RESULT")
@Entity
public class JobResult implements Externalizable{
	public enum Status {CREATED, FINISHED, ARCHIVED};
	@Id
	@Column(name="JR_RESULT_ID")
	private long id;
	@OneToOne(optional=false)
	@JoinColumn(name="JOB_ID")
	private Job job;
	@Column(name="JR_ERROR")
	@Lob
	private byte[] error;
	@Column(name="JR_RESULT")
	@Lob
	private byte[] result;
	@Column(name="JR_START_TIME")
	private long startTime;
	@Column(name="JR_END_TIME")
	private long endTime;
	@Column(name="JR_STATUS")
	private Status status;
	
	public JobResult(){
	}
	
	public JobResult(Job job){
		this.id=job.getId();
		this.job=job;
		status=Status.CREATED;
		setStartTime();
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public byte[] getError() {
		return error;
	}

	public Throwable getErrorAsThrowable() throws Exception{
		if (error!=null){
			return (Throwable)Job.read(error);
		}else{
			return null;
		}
	}
	
	public void setError(byte[] error) {
		this.error = error;
	}
	
	public void setError(Object o) throws Exception{
		if (o!=null){
			error=Job.write(o);
		}else{
			error=null;
		}
	}
	
	public <T> T getError(Class<T> c) throws Exception{
		if (error!=null){
			Object o=Job.read(error);
			return c.cast(o);
		}else{
			return null;
		}
	}
		
	public void setError(Throwable th) throws Exception{
		if (th!=null){
			error=Job.write(th);
		}else{
			error=null;
		}
	}

	public byte[] getResult() {
		return result;
	}
	
	public <T> T getResult(Class<T> c) throws Exception{
		if (result!=null){
			Object o=Job.read(result);
			return c.cast(o);
		}else{
			return null;
		}
	}
	
	public Object getResultAsObject() throws Exception{
		if (result!=null){
			return Job.read(result);
		}else{
			return null;
		}
	}
	
	public void setResult(byte[] result) {
		this.result = result;
	}
	
	public void setResult(Object o) throws Exception{
		if (o!=null){
			result=Job.write(o);
		}else{
			result=null;
		}
	}
	
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	public void setStartTime() {
		this.startTime = System.currentTimeMillis();
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	public void setEndTime() {
		this.endTime = System.currentTimeMillis();
	}

	public Status getStatus() {
		return status;
	}
	
	public boolean isStatusFinished(){
		return status==Status.FINISHED;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public void setStatusFinished(){
		this.status=Status.FINISHED;
	}
	
	public void setStatusArchived(){
		this.status=Status.ARCHIVED;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(id);
		out.writeObject(job);
		if (error!=null){
			out.writeInt(error.length);
			out.write(error);
		}else{
			out.writeInt(0);
		}
		if (result!=null){
			out.writeInt(result.length);
			out.write(result);
		}else{
			out.writeInt(0);
		}
		out.writeLong(startTime);
		out.writeLong(endTime);
		out.writeObject(status);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id=in.readLong();
		job=(Job)in.readObject();

		int nb=in.readInt();
		if (nb>0){
			error=new byte[nb];
			in.readFully(error);
		}
		
		nb=in.readInt();
		if (nb>0){
			result=new byte[nb];
			in.readFully(result);
		}
		startTime=in.readLong();
		endTime=in.readLong();
		status=(Status)in.readObject();
	}
	
	@Override
	public String toString(){
		return "JobResult[Id:"+id+"][Job:"+job+"][StartTime:"+startTime+"][EndTime:"+endTime+"][Status:"+status+"]";
	}
}
