package org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.AbstractExtraUseCase;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.CONSTRUCTOR, ElementType.TYPE })
public @interface DependsOnUseCases {
    Class<? extends AbstractExtraUseCase<?, ?>>[] value();
}
