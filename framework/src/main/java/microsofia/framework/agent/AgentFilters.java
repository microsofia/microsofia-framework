package microsofia.framework.agent;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.function.Function;

public class AgentFilters {

	public static Function<AgentInfo,Boolean> byQueue(String sn){
		return new QueueFilter(sn);
	}
	
	public static class QueueFilter implements Function<AgentInfo,Boolean>,Externalizable{
		private static final long serialVersionUID = 0L;
		private String queue;

		public QueueFilter(){
		}
		
		public QueueFilter(String queue){
			this.queue=queue;
		}

		@Override
		public Boolean apply(AgentInfo info) {
			return info.getQueue().equals(queue);
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeUTF(queue);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			queue=in.readUTF();
		}
	}
}
