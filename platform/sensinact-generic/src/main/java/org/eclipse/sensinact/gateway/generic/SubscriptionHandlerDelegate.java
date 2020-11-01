/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.generic;

/**
 * A SubscriptionHandlerDelegate is in charge of providing the appropriate and extended {@link 
 * AbstractSubscribeTaskWrapper} and {@link AbstractUnsubscribeTaskWrapper} types to wrap respectively 
 * subscribe and unsubscribe {@link Task}s
 */
public interface SubscriptionHandlerDelegate {

    /**
     * Returns the {@link UnsubscribeTaskWrapper} type to be used to wrapped unsubscribe 
     * {@link Task}
     * 
     * @return the {@link UnsubscribeTaskWrapper} type to be used
     */
    Class<? extends UnsubscribeTaskWrapper> getUnsubscribeTaskWrapperType();
    
    /**
     * Returns the {@link SubscribeTaskWrapper} type to be used to wrapped subscribe 
     * {@link Task}
     * 
     * @return the {@link SubscribeTaskWrapper} type to be used
     */
    Class<? extends SubscribeTaskWrapper> getSubscribeTaskWrapperType();
    
}
