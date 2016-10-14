package microsofia.framework.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import io.atomix.catalyst.util.Assert;
import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.session.ServerSession;
import io.atomix.copycat.server.session.SessionListener;
import io.atomix.resource.ResourceStateMachine;

public class MapState extends ResourceStateMachine implements SessionListener {
	private Map<Long,Set<Value>> valuesBySessionId;
	private Map<Object,Value> valuesByKey;
	
	public MapState(Properties config) {
		super(config);
		valuesBySessionId=new HashMap<>();
		valuesByKey=new HashMap<>();
	}

	@Override
	public void close(ServerSession session) {
		Set<Value> values=valuesBySessionId.remove(session.id());
		if (values!=null){
			for (Value value : values){
				valuesByKey.remove(value.key);
				value.commit.close();
			}
		}
	}
	
	public boolean containsKey(Commit<MapCommands.ContainsKey> commit) {
		try {
			return valuesByKey.containsKey(commit.operation().key());
	    } finally {
	    	commit.close();
	    }
	}

	public boolean containsValue(Commit<MapCommands.ContainsValue> commit) {
		try {
			for (Value value : valuesByKey.values()) {
				if (value.commit.operation().value().equals(commit.operation().value())) {
					return true;
				}
			}
			return false;
	    } finally {
	    	commit.close();
	    }
	}

	public Object get(Commit<MapCommands.Get> commit) {
		try {
			Value value = valuesByKey.get(commit.operation().key());
			return value != null ? value.commit.operation().value() : null;
	    } finally {
	    	commit.close();
	    }
	}

	public Object getOrDefault(Commit<MapCommands.GetOrDefault> commit) {
		try {
	    	Value value = valuesByKey.get(commit.operation().key());
	    	return value != null ? value.commit.operation().value() : commit.operation().defaultValue();
	    } finally {
	    	commit.close();
	    }
	}
	
	private void put(long id,Value value){
    	Set<Value> values=valuesBySessionId.get(id);
    	if (values==null){
    		values=new HashSet<>();
    		valuesBySessionId.put(id,values);
    	}
    	values.add(value);
    	
    	valuesByKey.put(value.commit.command().key(), value);
	}
	
	private Value removeValue(Object key){
		Value value = valuesByKey.remove(key);
		if (value!=null){
			Set<Value> values=valuesBySessionId.get(value.id);
			values.remove(value);
			if (values.isEmpty()){
				valuesBySessionId.remove(value.id);
			}
		}
		return value;
	}

	public Object put(Commit<MapCommands.Put> commit) {
	    try {
	    	Value newValue=new Value(commit);
	    	Value oldValue = removeValue(commit.operation().key());
	    	put(commit.session().id(),newValue);

	    	if (oldValue != null) {
	    		try {
	    			return oldValue.commit.operation().value();
	    		} finally {
	    			oldValue.commit.close();
	    		}
	    	}
	    	return null;
	    } catch (Exception e) {
	    	commit.close();
	    	throw e;
	    }
	}

	public Object putIfAbsent(Commit<MapCommands.PutIfAbsent> commit) {
		try {
	    	Value value = valuesByKey.get(commit.operation().key());
	    	if (value == null) {
	    		Value newValue=new Value(commit);
	    		put(commit.session().id(),newValue);
	    		return null;

	    	} else {
	    		commit.close();
	    		return value.commit.operation().value();
	    	}
	    } catch (Exception e) {
	    	commit.close();
	    	throw e;
	    }
	}

	public Object remove(Commit<MapCommands.Remove> commit) {
		try {
	    	Value value = removeValue(commit.operation().key());
	    	if (value != null) {
	    		try {
	    			return value.commit.operation().value();
	    		} finally {
	    			value.commit.close();
	    		}
	    	}
	    	return null;
	    } finally {
	    	commit.close();
	    }
	}

	public boolean removeIfPresent(Commit<MapCommands.RemoveIfPresent> commit) {
		try {
	    	Value value = valuesByKey.get(commit.operation().key());
	    	if (value == null || ((value.commit.operation().value() == null && commit.operation().value() != null) || 
	    	   (value.commit.operation().value() != null && !value.commit.operation().value().equals(commit.operation().value())))) {
	    		return false;
	    	} else {
	    		try {
	    			removeValue(commit.operation().key());
	    			return true;
	    		} finally {
	    			value.commit.close();
	    		}
	    	}
	    } finally {
	    	commit.close();
	    }
	}

	public Object replace(Commit<MapCommands.Replace> commit) {
		Value value = valuesByKey.get(commit.operation().key());
	    if (value != null) {
	    	try {
	    		Value newValue=new Value(commit);
	    		removeValue(commit.operation().key());
	    		put(commit.session().id(),newValue);
	    		return value.commit.operation().value();
	    	} finally {
	    		value.commit.close();
	    	}
	    } else {
	    	commit.close();
	    }
		return null;
	}

	public boolean replaceIfPresent(Commit<MapCommands.ReplaceIfPresent> commit) {
		Value value = valuesByKey.get(commit.operation().key());
	    if (value == null) {
	    	commit.close();
	    	return false;
	    }

	    if ((value.commit.operation().value() == null && commit.operation().replace() == null) || 
	    	(value.commit.operation().value() != null && value.commit.operation().value().equals(commit.operation().replace()))) {
	    	removeValue(commit.operation().key());

	    	Value newValue=new Value(commit);
	    	put(commit.session().id(), newValue);
	    	
	    	value.commit.close();
	    	return true;
	    } else {
	    	commit.close();
	    }
	    return false;
	}

	public Collection<Object> values(Commit<MapCommands.Values> commit) {
		try {
			Collection<Object> values = new ArrayList<>();
			for (Value value : valuesByKey.values()) {
				values.add(value.value);
			}
			return values;
		} finally {
			commit.close();
		}
	}

	public Set<Object> keySet(Commit<MapCommands.KeySet> commit) {
		try {
	    	return new HashSet<>(valuesByKey.keySet());
	    } finally {
	    	commit.close();
	    }
	}

	public Set<Map.Entry<Object, Object>> entrySet(Commit<MapCommands.EntrySet> commit) {
		try {
	    	Set<Map.Entry<Object, Object>> entries = new HashSet<>();
	    	for (Map.Entry<Object, Value> entry : valuesByKey.entrySet()) {
	    		entries.add(new MapEntry(entry.getKey(), entry.getValue().commit.operation().value()));
	    	}
	    	return entries;
	    } finally {
	    	commit.close();
	    }
	}

	public int size(Commit<MapCommands.Size> commit) {
		try {
			return valuesByKey.size();
	    } finally {
	    	commit.close();
	    }
	}

	public boolean isEmpty(Commit<MapCommands.IsEmpty> commit) {
		try {
	    	return valuesByKey == null || valuesByKey.isEmpty();
	    } finally {
	    	commit.close();
	    }
	}

	public void clear(Commit<MapCommands.Clear> commit) {
		try {
	    	delete();
	    } finally {
	    	commit.close();
	    }
	}

	@Override
	public void delete() {
		Iterator<Map.Entry<Object, Value>> iterator = valuesByKey.entrySet().iterator();
	    while (iterator.hasNext()) {
	    	Map.Entry<Object, Value> entry = iterator.next();
	    	Value value = entry.getValue();
	    	value.commit.close();
	    }
	    valuesByKey.clear();
	    valuesBySessionId.clear();
	}

	private static class Value {
		private final Commit<? extends MapCommands.KeyValueCommand<?>> commit;
		private long id;
		private Object key;
		private Object value;

		private Value(Commit<? extends MapCommands.KeyValueCommand<?>> commit) {
			this.commit = commit;
			id=commit.session().id();
			key=commit.command().key();
			value=commit.command().value();
		}
	}

	private static class MapEntry implements Map.Entry<Object, Object> {
		private final Object key;
	    private Object value;

	    private MapEntry(Object key, Object value) {
	    	this.key = Assert.notNull(key, "key");
	    	this.value = value;
	    }

	    @Override
	    public Object getKey() {
	    	return key;
	    }

	    @Override
	    public Object getValue() {
	    	return value;
	    }

	    @Override
	    public Object setValue(Object value) {
	    	Object oldValue = this.value;
	    	this.value = value;
	    	return oldValue;
	    }
	}
}
