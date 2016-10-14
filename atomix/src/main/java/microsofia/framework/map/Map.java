package microsofia.framework.map;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import io.atomix.copycat.client.CopycatClient;
import io.atomix.resource.AbstractResource;
import io.atomix.resource.ReadConsistency;
import io.atomix.resource.ResourceTypeInfo;

@ResourceTypeInfo(id=28, factory=MapFactory.class)
public class Map<K,V> extends AbstractResource<Map<K,V>> {

	public Map(CopycatClient client, Properties options) {
		super(client, options);
	}

	public CompletableFuture<Boolean> isEmpty() {
		return client.submit(new MapCommands.IsEmpty());
	}
	
	public CompletableFuture<Boolean> isEmpty(ReadConsistency consistency) {
		return client.submit(new MapCommands.IsEmpty(consistency.level()));
	}

	public CompletableFuture<Integer> size() {
		return client.submit(new MapCommands.Size());
	}
	
	public CompletableFuture<Integer> size(ReadConsistency consistency) {
		return client.submit(new MapCommands.Size(consistency.level()));
	}

	public CompletableFuture<Boolean> containsKey(K key) {
		return client.submit(new MapCommands.ContainsKey(key));
	}
	
	public CompletableFuture<Boolean> containsKey(K key,ReadConsistency cons) {
		return client.submit(new MapCommands.ContainsKey(key,cons.level()));
	}

	public CompletableFuture<Boolean> containsValue(V value) {
		return client.submit(new MapCommands.ContainsValue(value));
	}

	public CompletableFuture<Boolean> containsValue(V value,ReadConsistency cons) {
		return client.submit(new MapCommands.ContainsValue(value,cons.level()));
	}
	
	@SuppressWarnings("unchecked")
	public CompletableFuture<V> get(K key) {
		return client.submit(new MapCommands.Get(key)).thenApply(result -> (V) result);
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<V> get(K key,ReadConsistency cons) {
		return client.submit(new MapCommands.Get(key,cons.level())).thenApply(result -> (V) result);
	}
	
	@SuppressWarnings("unchecked")
	public CompletableFuture<V> getOrDefault(K key, V defaultValue) {
		return client.submit(new MapCommands.GetOrDefault(key, defaultValue)).thenApply(result -> (V) result);
	}
	
	@SuppressWarnings("unchecked")
	public CompletableFuture<V> getOrDefault(K key, V defaultValue,ReadConsistency cons) {
		return client.submit(new MapCommands.GetOrDefault(key, defaultValue,cons.level())).thenApply(result -> (V) result);
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<V> put(K key, V value) {
		return client.submit(new MapCommands.Put(key, value)).thenApply(result -> (V) result);
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<V> putIfAbsent(K key, V value) {
		return client.submit(new MapCommands.PutIfAbsent(key, value)).thenApply(result -> (V) result);
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<V> remove(K key) {
		return client.submit(new MapCommands.Remove(key)).thenApply(result -> (V) result);
	}

	public CompletableFuture<Boolean> remove(K key, V value) {
		return client.submit(new MapCommands.RemoveIfPresent(key, value));
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<V> replace(K key, V value) {
		return client.submit(new MapCommands.Replace(key, value)).thenApply(result -> (V) result);
	}

	public CompletableFuture<Boolean> replace(K key, V oldValue, V newValue) {
		return client.submit(new MapCommands.ReplaceIfPresent(key, oldValue, newValue));
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<Set<K>> keySet() {
		return client.submit(new MapCommands.KeySet()).thenApply(keys -> (Set<K>) keys);
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<Set<K>> keySet(ReadConsistency cons) {
		return client.submit(new MapCommands.KeySet(cons.level())).thenApply(keys -> (Set<K>) keys);
	}
	
	@SuppressWarnings("unchecked")
	public CompletableFuture<Collection<V>> values() {
		return client.submit(new MapCommands.Values()).thenApply(values -> (Collection<V>) values);
	}
	
	@SuppressWarnings("unchecked")
	public CompletableFuture<Collection<V>> values(ReadConsistency cons) {
		return client.submit(new MapCommands.Values(cons.level())).thenApply(values -> (Collection<V>) values);
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<Set<java.util.Map.Entry<K, V>>> entrySet() {
		return client.submit(new MapCommands.EntrySet()).thenApply(entries -> (Set<java.util.Map.Entry<K, V>>) entries);
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<Set<java.util.Map.Entry<K, V>>> entrySet(ReadConsistency cons) {
		return client.submit(new MapCommands.EntrySet(cons.level())).thenApply(entries -> (Set<java.util.Map.Entry<K, V>>) entries);
	}
	
	public CompletableFuture<Void> clear() {
		return client.submit(new MapCommands.Clear());
	}
}
