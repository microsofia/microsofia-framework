package microsofia.framework.invoker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.Snapshottable;
import io.atomix.copycat.server.session.ServerSession;
import io.atomix.copycat.server.session.SessionListener;
import io.atomix.copycat.server.storage.snapshot.SnapshotReader;
import io.atomix.copycat.server.storage.snapshot.SnapshotWriter;
import io.atomix.resource.ResourceStateMachine;

public class InvokerState extends ResourceStateMachine implements SessionListener,Snapshottable{
	private Map<Long,SessionState> sessionStateBySessionId;
	private Commit<InvokerCommands.SetInvokerCommand> invokerService;
	
	public InvokerState(Properties config) {
		super(config);
		sessionStateBySessionId=new HashMap<>();
	}

	@Override
	public void close(ServerSession session) {
		SessionState sessionState=sessionStateBySessionId.remove(session.id());
		if (sessionState!=null){
			sessionState.close();
		}
	}
	
	public void invoke(Commit<InvokerCommands.InvokeCommand> commit) {
		SessionState sessionState=sessionStateBySessionId.get(commit.session().id());
		if (sessionState==null){
			sessionState=new SessionState(commit.session().id());
			sessionStateBySessionId.put(commit.session().id(),sessionState);
		}
		sessionState.addRequest(commit);
	}

	public void setInvocationResponse(Commit<InvokerCommands.SetInvocationResponseCommand> response) {
		try{
			SessionState sessionState=sessionStateBySessionId.get(response.command().response().getRequest().getSessionId());
			if (sessionState!=null){
				sessionState.setResponse(response);
			}else{
				;//session requesting died, we could send it back to the invokerservice to do some cleanings...
			}
		}finally{
			response.close();
		}
	}
	
	public void setInvokerService(Commit<InvokerCommands.SetInvokerCommand> invokerService){
		if (this.invokerService!=null){
			this.invokerService.session().publish("stop");
			this.invokerService.close();
		}
		this.invokerService=invokerService;
		this.invokerService.session().publish("start");
	}
	
	public Long getMaxId(Commit<InvokerCommands.GetMaxId> commit){
		try{
			SessionState sessionState=sessionStateBySessionId.get(commit.session().id());
			if (sessionState!=null){
				return sessionState.getMaxId();
			}
			return new Long(0);
		}finally{
			commit.close();
		}
	}
	
	@Override
	public void install(SnapshotReader reader) {
		int nb=reader.readInt();
		for (int i=0;i<nb;i++){
			SessionState sessionState=new SessionState(0);
			sessionState.read(reader);
			sessionStateBySessionId.put(sessionState.getId(), sessionState);
		}
	}

	@Override
	public void snapshot(SnapshotWriter writer) {
		writer.writeInt(sessionStateBySessionId.size());
		sessionStateBySessionId.values().forEach(it->{
			it.write(writer);
		});		
	}
	
	class SessionState{
		private long id;
		private long maxId;
		private Set<Long> requestIds;
		private Map<Long,Commit<InvokerCommands.InvokeCommand>> requests;
		
		SessionState(long id){
			this.id=id;
			requestIds=new HashSet<>();
			requests=new HashMap<>();
		}
		
		public long getId(){
			return id;
		}

		public long getMaxId(){
			return maxId;
		}
		
		public void read(SnapshotReader reader) {
			id=reader.readLong();
			maxId=reader.readLong();
			
			int nb=reader.readInt();
			for (int i=0;i<nb;i++){
				requestIds.add(reader.readLong());
			}
		}

		public void write(SnapshotWriter writer) {
			writer.writeLong(id);
			writer.writeLong(maxId);

			writer.writeInt(requestIds.size());
			requests.values().forEach(it->{
				writer.writeLong(it.command().request().getId());
			});
		}
		
		void addRequest(Commit<InvokerCommands.InvokeCommand> request){
			requests.put(request.command().request().getId(), request);
			maxId=(maxId>request.command().request().getId() ? maxId : request.command().request().getId());
			
			if (!requestIds.contains(request.command().request().getId())){
				invokerService.session().publish("invoke",request.command().request());
				requestIds.add(request.command().request().getId());
			}
		}
		
		void setResponse(Commit<InvokerCommands.SetInvocationResponseCommand> response){
			Commit<InvokerCommands.InvokeCommand> requestCommit=requests.remove(response.command().response().getRequest().getId());
			try{
				requestIds.remove(response.command().response().getRequest().getId());
				requestCommit.session().publish("response",response.command().response());
			}finally{
				requestCommit.close();
			}
		}
		
		void close(){
			requests.values().forEach(it->{
				invokerService.session().publish("stopInvocation",it.command().request());
				it.close();
			});
		}
	}
}
