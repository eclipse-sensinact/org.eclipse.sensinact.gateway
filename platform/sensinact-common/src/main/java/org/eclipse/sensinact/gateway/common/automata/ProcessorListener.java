/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.common.automata;

/**
 * Signature of a service listening for {@link Processor}'s frame
 * instantiation events
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface ProcessorListener extends FrameModelProvider {
    /**
     * The listener is informed about the instantiation of
     * a new {@link Frame}
     *
     * @param frame        the instantiated {@link Frame}
     * @param delimitation the delimitation type
     * @param delimiters   the array of delimiters
     */
    void push(Frame frame, int delimitation, byte[] delimiters);

    /**
     * Returns the {@link FrameFactory} used to instantiate
     * new {@link Frame}s
     *
     * @return the {@link FrameFactory} used to instantiate
     * new {@link Frame}s
     */
    FrameFactory getFrameFactory();
}
