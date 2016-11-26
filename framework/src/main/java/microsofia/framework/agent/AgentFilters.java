package microsofia.framework.agent;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.function.Function;

public class AgentFilters {

	public static Function<AgentInfo,Boolean> byNameAndGroup(String n,String group){
		return new QueueFilter(n,group);
	}
	
	public static class QueueFilter implements Function<AgentInfo,Boolean>,Externalizable{
		private static final long serialVersionUID = 0L;
		private String name;
		private String group;

		public QueueFilter(){
		}
		
		public QueueFilter(String n,String g){
			this.name=n;
			this.group=g;
		}

		@Override
		public Boolean apply(AgentInfo info) {
			if (name!=null && !info.getName().equals(name)){
				return false;
			}
			if (group!=null && !info.getGroup().equals(group)){
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
}
