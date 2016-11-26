package microsofia.framework.registry.lookup;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.function.Function;

public class LookupResultFilters {

	public static Function<LookupResult,Boolean> byNameAndGroup(String n,String g){
		return new LookupResultByQueueFilter(n,g);
	}

	public static class LookupResultByQueueFilter implements Function<LookupResult,Boolean>,Externalizable{
		private static final long serialVersionUID = 0L;
		private String name;
		private String group;

		public LookupResultByQueueFilter(){
		}
		
		public LookupResultByQueueFilter(String n,String g){
			this.name=n;
			this.group=g;
		}

		@Override
		public Boolean apply(LookupResult result) {
			if (name!=null && !result.getAgentInfo().getName().equals(name)){
				return false;
			}
			if (group!=null && !result.getAgentInfo().getGroup().equals(group)){
				return false;
			}
			return true;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeUTF(name);
			out.writeUTF(group);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			name=in.readUTF();
			group=in.readUTF();
		}
	}
	
	public static Function<LookupResult,Boolean> byClientPid(long pid){
		return new LookupResultByClientPidFilter(pid);
	}
	
	public static class LookupResultByClientPidFilter implements Function<LookupResult,Boolean>,Externalizable{
		private static final long serialVersionUID = 0L;
		private long pid;

		public LookupResultByClientPidFilter(){
		}
		
		public LookupResultByClientPidFilter(long pid){
			this.pid=pid;
		}

		@Override
		public Boolean apply(LookupResult result) {
			return result.getLookupRequest().getClientInfo().getPid()==pid;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeLong(pid);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			pid=in.readLong();
		}
	}
	
	public static Function<LookupResult,Boolean> byAgentPid(long pid){
		return new LookupResultByAgentPidFilter(pid);
	}
	
	public static class LookupResultByAgentPidFilter implements Function<LookupResult,Boolean>,Externalizable{
		private static final long serialVersionUID = 0L;
		private long pid;

		public LookupResultByAgentPidFilter(){
		}
		
		public LookupResultByAgentPidFilter(long pid){
			this.pid=pid;
		}

		@Override
		public Boolean apply(LookupResult result) {
			return result.getAgentInfo().getPid()==pid;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeLong(pid);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			pid=in.readLong();
		}
	}
}
