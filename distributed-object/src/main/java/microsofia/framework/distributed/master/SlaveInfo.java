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

@Table(name="DO_SLAVE")
@Entity
public class SlaveInfo implements Externalizable{
	public enum Status {STOPPED,STARTED};
	@Id
	@GeneratedValue
	@Column(name="SLAVE_ID")
	private long id;
	@Column(name="THREAD_POOL_SIZE")
	private int threadPoolSize;
	@Column(name="SLAVE_STATUS")
	private Status status;
	@Column(name="SLAVE_NAME")
	private String name;
	@Column(name="SLAVE_GROUP")
	private String group;
	
	public SlaveInfo(){
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

	public String getName() {
		return name;
	}

	public void setName(String n) {
		this.name = n;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String n) {
		this.group= n;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(id);
		out.writeInt(threadPoolSize);
		out.writeObject(status);
		out.writeUTF(name);
		out.writeUTF(group);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id=in.readLong();
		threadPoolSize=in.readInt();
		status=(Status)in.readObject();
		name=in.readUTF();
		group=in.readUTF();
	}
	
	@Override
	public String toString(){
		return "SlaveConfig[Id:"+id+"][ThreadPoolSize:"+threadPoolSize+"][Status:"+status+"][Name:"+name+"][Group:"+group+"]";
	}
}