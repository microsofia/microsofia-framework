package microsofia.framework.registry.atomix;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.session.ServerSession;
import io.atomix.copycat.server.session.SessionListener;
import io.atomix.resource.ResourceStateMachine;
import microsofia.framework.service.ServiceAddress;

public class ServicesAddressState extends ResourceStateMachine implements SessionListener {//snapshottable?
	private Map<Long,ServiceAddress> addresses;

	public ServicesAddressState(Properties config) {
		super(config);
		addresses=new HashMap<>();
	}

	@Override
	public void close(ServerSession session) {
		addresses.remove(session.id());
	}

	public void add(Commit<Commands.AddCommand> commit) {
		addresses.put(commit.session().id(), commit.command().address());
		commit.close();
	}
	  
	public void remove(Commit<Commands.RemoveCommand> commit) {
		addresses.remove(commit.session().id());//check it has the same addr? commit.command().address());
		commit.close();
	}
}
