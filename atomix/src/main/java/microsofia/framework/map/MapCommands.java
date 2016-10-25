package microsofia.framework.map;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.serializer.SerializerRegistry;
import io.atomix.catalyst.util.Assert;
import io.atomix.copycat.Command;
import io.atomix.copycat.Query;

public class MapCommands {
		
	public static abstract class MapCommand<V> implements Command<V>, CatalystSerializable {
		private static final long serialVersionUID = 0L;
		
		protected MapCommand(){
		}
		
	    @Override
	    public CompactionMode compaction() {
	    	return CompactionMode.QUORUM;
	    }

		@Override
	    public void writeObject(BufferOutput<?> buffer, Serializer serializer) {
	    }

	    @Override
	    public void readObject(BufferInput<?> buffer, Serializer serializer) {
	    }
	}
	
	public static class AddListener extends MapCommand<Void>{
		private static final long serialVersionUID = 0L;
		
		public AddListener(){
		}
	}
	
	public static class RemoveListener extends MapCommand<Void>{
		private static final long serialVersionUID = 0L;
		
		public RemoveListener(){
		}
	}

	public static abstract class MapQuery<V> implements Query<V>, CatalystSerializable {
		private static final long serialVersionUID = 0L;
		protected ConsistencyLevel cons;

	    protected MapQuery() {
	    }
	    
	    protected MapQuery(ConsistencyLevel consistency) {
	    	this.cons = consistency;
	    }
	    
	    @Override
	    public ConsistencyLevel consistency(){
	    	return cons;
	    }

	    @Override
	    public void writeObject(BufferOutput<?> output, Serializer serializer) {
	    	if (cons != null) {
	    		output.writeByte(cons.ordinal());
	        } else {
	        	output.writeByte(-1);
	        }
	    }

	    @Override
	    public void readObject(BufferInput<?> input, Serializer serializer) {
	    	int ordinal = input.readByte();
	        if (ordinal != -1) {
	        	cons = ConsistencyLevel.values()[ordinal];
	        }
	    }
	}

	public static abstract class KeyCommand<V> extends MapCommand<V> {
		private static final long serialVersionUID = 0L;
		protected Object key;

	    public KeyCommand() {
	    }

	    public KeyCommand(Object key) {
	      this.key = Assert.notNull(key, "key");
	    }

	    public Object key() {
	    	return key;
	    }

	    @Override
	    public void writeObject(BufferOutput<?> buffer, Serializer serializer) {
	    	serializer.writeObject(key, buffer);
	    }

	    @Override
	    public void readObject(BufferInput<?> buffer, Serializer serializer) {
	    	key = serializer.readObject(buffer);
	    }
	}

	public static abstract class KeyQuery<V> extends MapQuery<V> {
		private static final long serialVersionUID = 0L;
		protected Object key;

	    public KeyQuery() {
	    }

	    public KeyQuery(Object key) {
	      this.key = Assert.notNull(key, "key");
	    }
	    
	    public KeyQuery(Object key, ConsistencyLevel consistency) {
	    	super(consistency);
	        this.key = Assert.notNull(key, "key");
	    }

	    public Object key() {
	      return key;
	    }

	    @Override
	    public void writeObject(BufferOutput<?> buffer, Serializer serializer) {
	    	super.writeObject(buffer, serializer);
	    	serializer.writeObject(key, buffer);
	    }

	    @Override
	    public void readObject(BufferInput<?> buffer, Serializer serializer) {
	    	super.readObject(buffer, serializer);
	    	key = serializer.readObject(buffer);
	    }
	}

	public static class ContainsKey extends KeyQuery<Boolean> {
		private static final long serialVersionUID = 0L;
		
		public ContainsKey() {
	    }

	    public ContainsKey(Object key) {
	    	super(key);
	    }
	    
	    public ContainsKey(Object key, ConsistencyLevel consistency) {
	    	super(key, consistency);
	    }
	}

	public static class ContainsValue extends MapQuery<Boolean> {
		private static final long serialVersionUID = 0L;
		protected Object value;

	    public ContainsValue() {
	    }

	    public ContainsValue(Object value) {
	    	this.value = Assert.notNull(value, "value");
	    }
	    
	    public ContainsValue(Object value, ConsistencyLevel consistency) {
	    	super(consistency);
	    	this.value = Assert.notNull(value, "value");
	    }

	    public Object value() {
	    	return value;
	    }

	    @Override
	    public void writeObject(BufferOutput<?> buffer, Serializer serializer) {
	    	super.writeObject(buffer, serializer);
	    	serializer.writeObject(value, buffer);
	    }

	    @Override
	    public void readObject(BufferInput<?> buffer, Serializer serializer) {
	    	super.readObject(buffer, serializer);
	    	value = serializer.readObject(buffer);
	    }
	}

	public static abstract class KeyValueCommand<V> extends KeyCommand<V> {
		private static final long serialVersionUID = 0L;
		protected Object value;

	    public KeyValueCommand() {
	    }

	    public KeyValueCommand(Object key, Object value) {
	    	super(key);
	    	this.value = value;
	    }

	    public Object value() {
	    	return value;
	    }

	    @Override
	    public void writeObject(BufferOutput<?> buffer, Serializer serializer) {
	    	super.writeObject(buffer, serializer);
	    	serializer.writeObject(value, buffer);
	    }

	    @Override
	    public void readObject(BufferInput<?> buffer, Serializer serializer) {
	    	super.readObject(buffer, serializer);
	    	value = serializer.readObject(buffer);
	    }
	}

	public static class Put extends KeyValueCommand<Object> {
		private static final long serialVersionUID = 0L;
	    
		public Put() {
	    }

	    public Put(Object key, Object value) {
	    	super(key, value);
	    }
	}

	public static class PutIfAbsent extends KeyValueCommand<Object> {
		private static final long serialVersionUID = 0L;
	    
		public PutIfAbsent() {
	    }

	    public PutIfAbsent(Object key, Object value) {
	      super(key, value);
	    }
	}

	public static class Get extends KeyQuery<Object> {
		private static final long serialVersionUID = 0L;
	    
		public Get() {
	    }

	    public Get(Object key) {
	    	super(key);
	    }
	    
	    public Get(Object key, ConsistencyLevel consistency) {
	    	super(key, consistency);
	    }
	}

	public static class GetOrDefault extends KeyQuery<Object> {
		private static final long serialVersionUID = 0L;
		private Object defaultValue;

	    public GetOrDefault() {
	    }

	    public GetOrDefault(Object key, Object defaultValue) {
	    	super(key);
	    	this.defaultValue = defaultValue;
	    }
	    
	    public GetOrDefault(Object key, Object defaultValue, ConsistencyLevel consistency) {
	    	super(key, consistency);
	    	this.defaultValue = defaultValue;
	    }

	    public Object defaultValue() {
	    	return defaultValue;
	    }

	    @Override
	    public void readObject(BufferInput<?> buffer, Serializer serializer) {
	    	super.readObject(buffer, serializer);
	    	defaultValue = serializer.readObject(buffer);
	    }

	    @Override
	    public void writeObject(BufferOutput<?> buffer, Serializer serializer) {
	    	super.writeObject(buffer, serializer);
	    	serializer.writeObject(defaultValue, buffer);
	    }
	}

	public static class Remove extends KeyCommand<Object> {
		private static final long serialVersionUID = 0L;

	    public Remove() {
	    }

	    public Remove(Object key) {
	    	super(key);
	    }
	    
	    @Override
	    public CompactionMode compaction() {
	      return CompactionMode.SEQUENTIAL;
	    }
	}

	public static class RemoveIfPresent extends KeyValueCommand<Boolean> {
		private static final long serialVersionUID = 0L;

		public RemoveIfPresent() {
	    }

	    public RemoveIfPresent(Object key, Object value) {
	    	super(key, value);
	    }
	    
	    @Override
	    public CompactionMode compaction() {
	    	return CompactionMode.SEQUENTIAL;
	    }
	}

	public static class Replace extends KeyValueCommand<Object> {
		private static final long serialVersionUID = 0L;
		
		public Replace() {
	    }

	    public Replace(Object key, Object value) {
	    	super(key, value);
	    }
	}

	public static class ReplaceIfPresent extends KeyValueCommand<Boolean> {
		private static final long serialVersionUID = 0L;
		private Object replace;

	    public ReplaceIfPresent() {
	    }

	    public ReplaceIfPresent(Object key, Object replace, Object value) {
	    	super(key, value);
	    	this.replace = replace;
	    }

	    public Object replace() {
	    	return replace;
	    }

	    @Override
	    public void writeObject(BufferOutput<?> buffer, Serializer serializer) {
	    	super.writeObject(buffer, serializer);
	    	serializer.writeObject(replace, buffer);
	    }

	    @Override
	    public void readObject(BufferInput<?> buffer, Serializer serializer) {
	    	super.readObject(buffer, serializer);
	    	replace = serializer.readObject(buffer);
	    }
	}

	public static class IsEmpty extends MapQuery<Boolean> {
		private static final long serialVersionUID = 0L;

		public IsEmpty() {
	    }
		
		public IsEmpty(ConsistencyLevel consistency) {
			super(consistency);
		}
	}

	public static class Size extends MapQuery<Integer> {
		private static final long serialVersionUID = 0L;
	    
		public Size() {
	    }
		
	    public Size(ConsistencyLevel consistency) {
	        super(consistency);
	    }
	}

	public static class Values extends MapQuery<Collection<?>> {
		private static final long serialVersionUID = 0L;
	    
		public Values() {
	    }
		
		public Values(ConsistencyLevel consistency) {
			super(consistency);
	    }
	}

	public static class KeySet extends MapQuery<Set<?>> {
		private static final long serialVersionUID = 0L;
		
	    public KeySet() {
	    }
	    
	    public KeySet(ConsistencyLevel consistency) {
	    	super(consistency);
	    }
	}

	public static class EntrySet extends MapQuery<Set<?>> {
		private static final long serialVersionUID = 0L;
		
		public EntrySet() {
	    }
		
		public EntrySet(ConsistencyLevel consistency) {
			super(consistency);
		}
	}

	public static class FilterValue extends MapQuery<List<Object>> {
		private static final long serialVersionUID = 0L;
		private Function<Object,Boolean> function;
		
		public FilterValue() {
	    }
		
		public FilterValue(Function<Object,Boolean> function) {
			this.function=function;
	    }
		
		public FilterValue(Function<Object,Boolean> function,ConsistencyLevel consistency) {
			super(consistency);
			this.function=function;
		}
		
		public Function<Object,Boolean> getFunction(){
			return function;
		}
		
		@Override
	    public void writeObject(BufferOutput<?> buffer, Serializer serializer) {
	    	super.writeObject(buffer, serializer);
	    	serializer.writeObject(function, buffer);
	    }

	    @Override
	    public void readObject(BufferInput<?> buffer, Serializer serializer) {
	    	super.readObject(buffer, serializer);
	    	function =serializer.readObject(buffer);
	    }
	}
	
	public static class Clear extends MapCommand<Void> {
		private static final long serialVersionUID = 0L;

		public Clear(){
		}
		
		@Override
	    public CompactionMode compaction() {
			return CompactionMode.SEQUENTIAL;
	    }
	}

	public static class TypeResolver implements SerializableTypeResolver {
		
		public TypeResolver(){
		}
		
		@Override
	    public void resolve(SerializerRegistry registry) {
			registry.register(ContainsKey.class, 1930);
	      	registry.register(ContainsValue.class, 1931);
	      	registry.register(Put.class, 1932);
	      	registry.register(PutIfAbsent.class, 1933);
	      	registry.register(Get.class, 1934);
	      	registry.register(GetOrDefault.class, 1935);
	      	registry.register(Remove.class,1936);
	      	registry.register(RemoveIfPresent.class, 1937);
	      	registry.register(Replace.class, 1938);
	      	registry.register(ReplaceIfPresent.class, 1939);
	      	registry.register(Values.class, 1940);
	      	registry.register(KeySet.class, 1941);
	      	registry.register(EntrySet.class, 1942);
	      	registry.register(IsEmpty.class, 1943);
	      	registry.register(Size.class, 1944);
	      	registry.register(Clear.class, 1945);
	      	registry.register(FilterValue.class, 1946);
	      	registry.register(AddListener.class, 1947);
	      	registry.register(RemoveListener.class, 1948);
	    }
	}
}
