package microsofia.framework;

import java.util.concurrent.CompletableFuture;

import io.atomix.AtomixClient;
import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import io.atomix.collections.DistributedMap;
import io.atomix.copycat.server.storage.Storage;

public class Test {

	public static void main(String[] argv) throws Exception{
		final Address adr=new Address("localhost", 9998);
		new Thread(){
			public void run(){
				AtomixReplica.Builder builder=AtomixReplica.builder(adr).withStorage(new Storage("logs1"));
				AtomixReplica replica=builder.build();
				replica.bootstrap().join();		
			}
		}.start();
		
		new Thread(){
			public void run(){
				AtomixReplica.Builder builder=AtomixReplica.builder(new Address("localhost", 0)).withStorage(new Storage("logs2"));
				AtomixReplica replica=builder.build();
				replica.join(adr).join();		
			}
		}.start();
		
		new Thread(){
			public void run(){
				AtomixReplica.Builder builder=AtomixReplica.builder(new Address("localhost", 0)).withStorage(new Storage("logs3"));
				AtomixReplica replica=builder.build();
				replica.join(adr).join();		
			}
		}.start();
		
		
		Thread th1=new Thread(){
			public void run(){
				try {
					AtomixClient client1=AtomixClient.builder().build();
					client1.connect(adr).get();
					CompletableFuture<DistributedMap<String, String>> future1=client1.getMap("test1");
					DistributedMap<String, String> m1=future1.get();
					m1.put("name", "charbel").get();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		th1.start();

		Thread th2=new Thread(){
			public void run(){
				try {
					AtomixClient client2=AtomixClient.builder().build();
					client2.connect(adr).get();
					CompletableFuture<DistributedMap<String, String>> future2=client2.getMap("test1");
					DistributedMap<String, String> m2=future2.get();
					System.out.println(m2.get("name").get());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		th2.start();
		th1.join();
		th2.join();
	}
}
