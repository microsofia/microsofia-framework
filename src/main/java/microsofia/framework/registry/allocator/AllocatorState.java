package microsofia.framework.registry.allocator;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.Snapshottable;
import io.atomix.copycat.server.session.ServerSession;
import io.atomix.copycat.server.session.SessionListener;
import io.atomix.copycat.server.storage.snapshot.SnapshotReader;
import io.atomix.copycat.server.storage.snapshot.SnapshotWriter;
import io.atomix.resource.ResourceStateMachine;

public class AllocatorState extends ResourceStateMachine implements SessionListener{
	private Map<Long,Map<Long,Commit<AllocatorCommands.AllocationCommand>>> requestsBySessionId;
	private Commit<AllocatorCommands.SetAllocatorCommand> allocator;
	
	public AllocatorState(Properties config) {
		super(config);
		requestsBySessionId=new HashMap<>();
	}

	@Override
	public void close(ServerSession session) {
		Map<Long,Commit<AllocatorCommands.AllocationCommand>> requests=requestsBySessionId.remove(session.id());//TODO
		if (requests!=null){
			requests.values().forEach(it->{
				//it.close();
			});
		}
	}
	
	public void allocate(Commit<AllocatorCommands.AllocationCommand> commit) {
		try{
			System.out.println("allocating request reqSession:"+commit.command().request().getSessionId()+" sessionId:"+commit.session().id());
			Map<Long,Commit<AllocatorCommands.AllocationCommand>> byRequestId=requestsBySessionId.get(commit.session().id());
			if (byRequestId==null){
				byRequestId=new HashMap<>();
				requestsBySessionId.put(commit.session().id(),byRequestId);
			}
			byRequestId.put(commit.command().request().getId(), commit);
			
			//TODO?
			this.allocator.session().publish("allocate",commit.command().request());
		}catch(Throwable th){
			th.printStackTrace();
		}
	}
	
	public void setAllocationResponse(Commit<AllocatorCommands.SetAllocationResponseCommand> response) {
		try{
			System.out.println("response arrived session:"+response.command().response().getRequest().getSessionId());
			Map<Long,Commit<AllocatorCommands.AllocationCommand>> byRequestId=requestsBySessionId.get(response.command().response().getRequest().getSessionId());
			if (byRequestId!=null){
				System.out.println("requests found byRequestId:"+byRequestId);
				Commit<AllocatorCommands.AllocationCommand> requestCommit=byRequestId.remove(response.command().response().getRequest().getId());
				System.out.println("sending by event response: "+response.command().response());
				requestCommit.session().publish("allocationResponse",response.command().response());
				//requestCommit.close();
			}//TODO else?
			//response.close();
		}catch(Throwable th){
			th.printStackTrace();
		}
	}
	
	public void setAllocator(Commit<AllocatorCommands.SetAllocatorCommand> allocator){
		System.out.println("setting allocator "+allocator.command());
		if (this.allocator!=null){
			//this.allocator.session().publish("stopAllocating");
			//this.allocator.close();
		}
		this.allocator=allocator;
		this.allocator.session().publish("startAllocating");
	}
}
