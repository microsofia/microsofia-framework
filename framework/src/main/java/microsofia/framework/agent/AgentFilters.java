package microsofia.framework.agent;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.function.Function;

public class AgentFilters {

	public static Function<AgentInfo,Boolean> byServiceName(String sn){
		return new ServiceNameFilter(sn);
	}
	
	public static class ServiceNameFilter implements Function<AgentInfo,Boolean>,Externalizable{
		private static final long serialVersionUID = 0L;
		private String serviceName;

		public ServiceNameFilter(){
		}
		
		public ServiceNameFilter(String serviceName){
			this.serviceName=serviceName;
		}

		@Override
		public Boolean apply(AgentInfo info) {
			return info.getServiceName().equals(serviceName);
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeUTF(serviceName);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			serviceName=in.readUTF();
		}
	}
}
