package org.eclipse.sensinact.prototype.model;

import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A builder for programmatically registering models 
 */
public interface ResourceBuilder<T> {
	
	ResourceBuilder<T> exclusivelyOwned(boolean exclusive);
	
	ResourceBuilder<T> withAutoDeletion(boolean autoDelete);
	
	<R> ResourceBuilder<R> withType(Class<R> type);

	ResourceBuilder<T> withInitialValue(T initialValue);
	
	ResourceBuilder<T> withInitialValue(T initialValue, Instant timestamp);
	
	ResourceBuilder<T> withValueType(ValueType valueType);

	ResourceBuilder<T> withResourceType(ResourceType resourceType);
	
	ResourceBuilder<T> withAction(Function<Object[], T> action, Class<?>... arguments);

	ResourceBuilder<T> withGetter(Supplier<T> getter);

	ResourceBuilder<T> withSetter(Consumer<T> setter);
	
	Resource build();
}
