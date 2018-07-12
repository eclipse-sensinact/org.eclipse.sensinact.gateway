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
package org.eclipse.sensinact.gateway.common.execution;

public interface ExecutionErrorHandler {
    public static final int CONTINUE = 0x00001;
    public static final int STOP = 0x00010;
    public static final int ROLLBACK = 0x00100;
    public static final int IGNORE = 0x01000;
    public static final int LOG = 0x10000;
    public static final int DEFAULT_POLICY = ExecutionErrorHandler.CONTINUE | ExecutionErrorHandler.LOG;

    boolean registerException(Exception e);
}
