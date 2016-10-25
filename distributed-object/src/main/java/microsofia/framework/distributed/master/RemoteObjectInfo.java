package microsofia.framework.distributed.master;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Table(name="DO_REMOTE_OBJECT")
@Entity
public class RemoteObjectInfo implements Externalizable{
	public enum Type{STATE_LESS, STATE_FULL};
	@Id
	@GeneratedValue
	@Column(name="RO_ID")
	private long id;
	@Column(name="RO_TYPE")
	private Type type;
	@ManyToOne
	@JoinColumn(name="RO_SLAVE_ID")
	private SlaveConfig slaveConfig;
	@Column(name="JOB_CREATION_TIME")
	private long creationTime;
	@Column(name="JOB_END_TIME")
	private long endTime;

	public RemoteObjectInfo(){
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public SlaveConfig getSlaveConfig() {
		return slaveConfig;
	}

	public void setSlaveConfig(SlaveConfig slaveConfig) {
		this.slaveConfig = slaveConfig;
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
	
	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(id);
		out.writeObject(type);
		out.writeObject(slaveConfig);
		out.writeLong(creationTime);
		out.writeLong(endTime);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id=in.readLong();
		type=(Type)in.readObject();
		slaveConfig=(SlaveConfig)in.readObject();
		creationTime=in.readLong();
		endTime=in.readLong();
	}
	
	@Override
	public String toString(){
		return "RemoteObject[Id:"+id+"][CreationTime:"+creationTime+"][EndTime:"+endTime+"][Type:"+type+"][SlaveConfig:"+slaveConfig+"]";
	}
}
