package microsofia.framework.map;

@FunctionalInterface
public interface MapListener<K,V> {

	public void entryRemoved(K key,V value);
}
