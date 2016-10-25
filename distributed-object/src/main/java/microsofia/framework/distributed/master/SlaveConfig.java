package microsofia.framework.distributed.master;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="DO_SLAVE_CONFIG")
@Entity
public class SlaveConfig implements Externalizable{
	public enum Status {STOPPED,STARTED};
	@Id
	@GeneratedValue
	@Column(name="SLAVE_ID")
	private long id;
	@Column(name="THREAD_POOL_SIZE")
	private int threadPoolSize;
	@Column(name="SLAVE_STATUS")
	private Status status;
	@Column(name="SLAVE_QUEUE")
	private String queue;//TODO not enough, should use agentname
	
	public SlaveConfig(){
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(id);
		out.writeInt(threadPoolSize);
		out.writeObject(status);
		out.writeUTF(queue);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id=in.readLong();
		threadPoolSize=in.readInt();
		status=(Status)in.readObject();
		queue=in.readUTF();
	}
	
	@Override
	public String toString(){
		return "SlaveConfig[Id:"+id+"][ThreadPoolSize:"+threadPoolSize+"][Status:"+status+"][Queue:"+queue+"]";
	}
}