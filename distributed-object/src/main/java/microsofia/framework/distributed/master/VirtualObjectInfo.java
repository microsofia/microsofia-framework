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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Table(name="DO_VIRTUAL_OBJECT")
@Entity
public class VirtualObjectInfo implements Externalizable{
	public enum Type{STATE_LESS, STATE_FULL};
	public enum Status {CREATED, FINISHED};
	@Id
	@GeneratedValue
	@Column(name="VO_ID")
	private long id;
	@Column(name="VO_TYPE")
	private Type type;
	@ManyToOne
	@JoinColumn(name="VO_SLAVE_ID")
	private SlaveInfo slaveInfo;
	@Column(name="VO_CREATION_TIME")
	private long creationTime;
	@Column(name="VO_END_TIME")
	private long endTime;
	@Column(name="VO_STATUS")
	private Status status;
	@Column(name="VO_SETUP_MTD")
	private int setupMethod;
	@Column(name="VO_SETUP_ARGS")
	@Lob
	private byte[] setupArguments;
	@Column(name="VO_TRDWN_MTD")
	private int tearnDownMethod;
	@Column(name="VO_TRDWN_ARGS")
	@Lob
	private byte[] tearnDownArguments;

	public VirtualObjectInfo(){
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
	
	public boolean isTypeStateLess(){
		return type.equals(Type.STATE_LESS);
	}
	
	public boolean isTypeStateFull(){
		return type.equals(Type.STATE_FULL);
	}

	public void setType(Type type) {
		this.type = type;
	}

	public SlaveInfo getSlaveInfo() {
		return slaveInfo;
	}

	public void setSlaveInfo(SlaveInfo slaveInfo) {
		this.slaveInfo = slaveInfo;
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
	
	public void setEndTime() {
		this.endTime = System.currentTimeMillis();
	}
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public void setStatusCreated(){
		this.status=Status.CREATED;
	}
	
	public void setStatusFinished(){
		this.status=Status.FINISHED;
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

	public int getTearnDownMethod() {
		return tearnDownMethod;
	}

	public void setTearnDownMethod(int tearnDownMethod) {
		this.tearnDownMethod = tearnDownMethod;
	}

	public byte[] getTearnDownArguments() {
		return tearnDownArguments;
	}

	public void setTearnDownArguments(byte[] tearnDownArguments) {
		this.tearnDownArguments = tearnDownArguments;
	}
	
	public Object[] getTearDownArgumentsAsObjects() throws Exception{
		if (tearnDownArguments!=null){
			return (Object[]) Job.read(tearnDownArguments);
		}else{
			return null;
		}
	}
		
	public void setTearDownArguments(Object[] args) throws Exception{
		if (args!=null){
			tearnDownArguments=Job.write(args);
		}else{
			tearnDownArguments=null;
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(id);
		out.writeObject(type);
		out.writeObject(slaveInfo);
		out.writeLong(creationTime);
		out.writeLong(endTime);
		out.writeObject(status);
		
		out.writeInt(setupMethod);
		if (setupArguments!=null){
			out.writeInt(setupArguments.length);
			out.write(setupArguments);
		}else{
			out.writeInt(0);
		}
		
		out.writeInt(tearnDownMethod);
		if (tearnDownArguments!=null){
			out.writeInt(tearnDownArguments.length);
			out.write(tearnDownArguments);
		}else{
			out.writeInt(0);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id=in.readLong();
		type=(Type)in.readObject();
		slaveInfo=(SlaveInfo)in.readObject();
		creationTime=in.readLong();
		endTime=in.readLong();
		status=(Status)in.readObject();
		
		setupMethod=in.readInt();
		int nb=in.readInt();
		if (nb>0){
			setupArguments=new byte[nb];
			in.readFully(setupArguments);
		}
		
		tearnDownMethod=in.readInt();
		nb=in.readInt();
		if (nb>0){
			tearnDownArguments=new byte[nb];
			in.readFully(tearnDownArguments);
		}
	}
	
	@Override
	public String toString(){
		return "VirtualObject[Id:"+id+"][CreationTime:"+creationTime+"][EndTime:"+endTime+"][Type:"+type+"][SlaveConfig:"+slaveInfo+"][Status:"+status+"][SetupMethod:"+setupMethod+"][TearDown:"+tearnDownMethod+"]";
	}
	
	private static ThreadLocal<Long> ids=new ThreadLocal<>();
	
	public static void setCurrentId(long id){
		ids.set(id);
	}
	
	public static Long getCurrentId(){
		return ids.get();
	}
}
