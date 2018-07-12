package org.eclipse.sensinact.gateway.core.method;

import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.core.ModelElement;

import java.util.Set;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface AccessMethodDescriptor<E extends ElementsProxy<?>> {
    /**
     * Defines on which Type this AccessMethodDescriptor applies
     *
     * @return the Type on which this AccessMethodDescriptor applies
     */
    Class<E> getTargetType();

    /**
     * Returns the Set of {@link Signature}s wrapped
     * by this AccessMethodDescriptor
     *
     * @return this AccessMethodDescriptor's Set of
     * {@link Signature}
     */
    Set<Signature> getSignatures();

    /**
     * Defines whether this AccessMethodDescriptor is valid for
     * the {@link ModelElement} passed as parameter, depending
     * on the specific constraints applying on this AccessMethodDescriptor
     *
     * @param element
     * @return
     */
    boolean valid(ModelElement<?, ?, ?, ?, E> element);
}
