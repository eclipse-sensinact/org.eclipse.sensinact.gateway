package org.eclipse.sensinact.gateway.sthbnd.ttn.packet;

import java.util.Map;

public interface PayloadDecoder {

	Map<String, Object> decodeRawPayload(byte[] payload);

}
