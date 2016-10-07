package microsofia.framework.registry.typology;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.Snapshottable;
import io.atomix.copycat.server.session.ServerSession;
import io.atomix.copycat.server.session.SessionListener;
import io.atomix.copycat.server.storage.snapshot.SnapshotReader;
import io.atomix.copycat.server.storage.snapshot.SnapshotWriter;
import io.atomix.resource.ResourceStateMachine;
import microsofia.framework.service.ServiceAddress;

public class TypologyState extends ResourceStateMachine implements SessionListener,Snapshottable {
	private Map<Long,ServiceAddress> registryAddressById;
	private Map<ServiceAddress,Long> registryAddressByAdr;
	private Map<Long,ServiceAddress> agentAddressById;
	private Map<ServiceAddress,Long> agentAddressByAdr;
	private Map<Long,ServiceAddress> clientAddressById;
	private Map<ServiceAddress,Long> clientAddressByAdr;
	
	public TypologyState(Properties config) {
		super(config);
		registryAddressById=new HashMap<>();
		registryAddressByAdr=new HashMap<>();
		agentAddressById=new HashMap<>();
		agentAddressByAdr=new HashMap<>();
		clientAddressById=new HashMap<>();
		clientAddressByAdr=new HashMap<>();
	}

	@Override
	public void close(ServerSession session) {
		ServiceAddress sa=registryAddressById.remove(session.id());
		if (sa!=null){
			if (session.id()==registryAddressByAdr.get(sa)){
				registryAddressByAdr.remove(sa);
			}

		}else{
			sa=agentAddressById.remove(session.id());
			if (sa!=null){
				if (session.id()==agentAddressByAdr.get(sa)){
					agentAddressByAdr.remove(sa);
				}
				
			}else{
				sa=clientAddressById.remove(session.id());
				if (sa!=null){
					if (session.id()==clientAddressByAdr.get(sa)){
						clientAddressByAdr.remove(sa);
					}
				}
			}
		}
	}
	
	public void addAgent(Commit<TypologyCommands.AddAgent> commit) {
		System.out.println("adding agent"+commit.command().address());
		agentAddressById.put(commit.session().id(), commit.command().address());
		agentAddressByAdr.put(commit.command().address(),commit.session().id());		
		commit.close();
	}
	
	public List<ServiceAddress> getAgents(Commit<TypologyCommands.GetAgentsQuery> query) {
		System.out.println("getting agents"+query.command());
		List<ServiceAddress> list=Arrays.asList(agentAddressByAdr.keySet().toArray(new ServiceAddress[0]));
		query.close();
		return list;
	}
	  
	public void removeAgent(Commit<TypologyCommands.RemoveAgent> commit) {
		System.out.println("removing agent"+commit.command().address());
		ServiceAddress sa=agentAddressById.remove(commit.session().id());
		if (sa!=null){
			agentAddressByAdr.remove(sa);
		}
		commit.close();
	}

	public void addRegistry(Commit<TypologyCommands.AddRegistry> commit) {
		System.out.println("adding reg"+commit.command().address());
		registryAddressById.put(commit.session().id(), commit.command().address());
		registryAddressByAdr.put(commit.command().address(),commit.session().id());
		commit.close();
	}
	  
	public void removeRegistry(Commit<TypologyCommands.RemoveRegistry> commit) {
		System.out.println("removing reg"+commit.command().address());
		ServiceAddress sa=registryAddressById.remove(commit.session().id());
		if (sa!=null){
			registryAddressByAdr.remove(sa);
		}
		commit.close();
	}
	
	public void addClient(Commit<TypologyCommands.AddClient> commit) {
		System.out.println("adding client"+commit.command().address());
		clientAddressById.put(commit.session().id(), commit.command().address());
		clientAddressByAdr.put(commit.command().address(),commit.session().id());
		commit.close();
	}
	  
	public void removeClient(Commit<TypologyCommands.RemoveClient> commit) {
		System.out.println("removing client "+commit.command().address());
		ServiceAddress sa=clientAddressById.remove(commit.session().id());
		if (sa!=null){
			clientAddressByAdr.remove(sa);
		}
		commit.close();
	}

	@Override
	public void install(SnapshotReader reader) {
		int l=reader.readInt();
		for (int i=0;i<l;i++){
			ServiceAddress sa=(ServiceAddress)reader.readObject();
			Long id=reader.readLong();
			registryAddressById.put(id, sa);
			registryAddressByAdr.put(sa, id);
		}
		
		l=reader.readInt();
		for (int i=0;i<l;i++){
			ServiceAddress sa=(ServiceAddress)reader.readObject();
			Long id=reader.readLong();
			agentAddressById.put(id, sa);
			agentAddressByAdr.put(sa,id);
		}
		
		l=reader.readInt();
		for (int i=0;i<l;i++){
			ServiceAddress sa=(ServiceAddress)reader.readObject();
			Long id=reader.readLong();
			clientAddressById.put(id, sa);
			clientAddressByAdr.put(sa,id);
		}
	}

	@Override
	public void snapshot(SnapshotWriter writer) {
		writer.writeInt(registryAddressByAdr.size());
		registryAddressByAdr.entrySet().forEach(it->{
			writer.writeObject(it.getKey());
			writer.writeLong(it.getValue());
		});
		
		writer.writeInt(agentAddressByAdr.size());
		agentAddressByAdr.entrySet().forEach(it->{
			writer.writeObject(it.getKey());
			writer.writeLong(it.getValue());
		});
		
		writer.writeInt(clientAddressByAdr.size());
		clientAddressByAdr.entrySet().forEach(it->{
			writer.writeObject(it.getKey());
			writer.writeLong(it.getValue());
		});
	}
}
