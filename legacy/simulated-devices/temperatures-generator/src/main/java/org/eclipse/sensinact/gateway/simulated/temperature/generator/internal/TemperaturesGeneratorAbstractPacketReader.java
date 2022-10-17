/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.simulated.temperature.generator.internal;

import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;

public abstract class TemperaturesGeneratorAbstractPacketReader extends SimplePacketReader<TemperaturesGeneratorAbstractPacket> {
    public TemperaturesGeneratorAbstractPacketReader() {
        super();
    }
}
