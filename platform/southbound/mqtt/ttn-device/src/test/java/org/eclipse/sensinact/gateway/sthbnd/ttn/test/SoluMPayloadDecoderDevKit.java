package org.eclipse.sensinact.gateway.sthbnd.ttn.test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.sthbnd.ttn.packet.PayloadDecoder;

/**
 * Byte order: big endian
 *
 * ------------------------------------------------------------------------------------------------------------------
 * Packet Header             |              |
 *    Company                | 2 bytes      | 0x0001 (default)
 *    Product                | 3 bytes      | 0x000000 (default)
 * ------------------------------------------------------------------------------------------------------------------
 * Version                   | 1 byte       | 0x01 (default)
 * ------------------------------------------------------------------------------------------------------------------
 * Major                     | 2 bytes      | iBeacon Major
 * ------------------------------------------------------------------------------------------------------------------
 * Minor                     | 2 bytes      | iBeacon Minor
 * ------------------------------------------------------------------------------------------------------------------
 * DeviceId                  | 4 bytes      | LoRa Device ID, Low 4 byte MAC address
 * ------------------------------------------------------------------------------------------------------------------
 * Timestamp                 | 4 bytes      | GMT Time by unix timestamp format, signed 32-bit integer
 * ------------------------------------------------------------------------------------------------------------------
 * Latitude                  | 4 bytes      | IEEE 754 format (N,E : +, S,W : -), degree, float
 * ------------------------------------------------------------------------------------------------------------------
 * Longitude                 | 4 bytes      | IEEE 754 format (N,E : +, S,W : -), degree, float
 * ------------------------------------------------------------------------------------------------------------------
 * Altitude                  | 2 bytes      | 0.1m resolution (I have some doubts about it...)
 * ------------------------------------------------------------------------------------------------------------------
 * Speed                     | 2 bytes      | 0.1km/h resolution
 * ------------------------------------------------------------------------------------------------------------------
 * HDOP                      | 3 bytes      | IEEE 754, Horizontal Dilution of Precision, float
 * ------------------------------------------------------------------------------------------------------------------
 * GPS Num                   | 1 byte       |
 * ------------------------------------------------------------------------------------------------------------------
 * HPE                       | 4 bytes      | IEEE 754, m, float
 * ------------------------------------------------------------------------------------------------------------------
 * Battery                   | 1 byte       | 0~100% value
 * ------------------------------------------------------------------------------------------------------------------
 * Status                    |              | Positioning mode for GPS, Function Key operation, GPS operation, Sensor
 *    -                      | 3 bits (!!)  | Empty bits (and not bytes!!)
 *    Positioning Mode       | 2 bits       | 00: no fix, 01: autonomous GPS fix, 10 differential GPS fix
 *    Function operation     | 1 bit        | Function key operation
 *    GPS operation          | 1 bit        |
 *    Sensor                 | 1 bit        |
 * ------------------------------------------------------------------------------------------------------------------
 * Firmware Version          |              | Device firmware version
 *    Firmware Version Major | 1 byte       |
 *    Firmware Version Minor | 1 byte       |
 *    Firmware Version Build | 1 byte       |
 * ------------------------------------------------------------------------------------------------------------------
 * Gyroscope sensor          |              |
 *    X                      | 1 byte       |
 *    Y                      | 1 byte       |
 *    Z                      | 1 byte       |
 * ------------------------------------------------------------------------------------------------------------------
 *
 * Total -------------------> 46 bytes
 */
public class SoluMPayloadDecoderDevKit implements PayloadDecoder {

    @Override
    public Map<String, Object> decodeRawPayload(byte[] payload) {
        Map<String, Object> solumPayload = new HashMap<>();

        byte[] deviceId = { payload[10], payload[11], payload[12], payload [13] };

        int latitude = (payload[18]<<24)&0xff000000|
                (payload[19]<<16)&0x00ff0000|
                (payload[20]<< 8)&0x0000ff00|
                (payload[21])&0x000000ff;

        BigDecimal latitudeAsDecimal = BigDecimal.valueOf(Float.intBitsToFloat(latitude));

                int longitude = (payload[22]<<24)&0xff000000|
                (payload[23]<<16)&0x00ff0000|
                (payload[24]<< 8)&0x0000ff00|
                (payload[25])&0x000000ff;

        BigDecimal longitudeAsDecimal = BigDecimal.valueOf(Float.intBitsToFloat(longitude));

        solumPayload.put("position", latitudeAsDecimal.toString() + ":" + longitudeAsDecimal.toString());

        int altitude = (payload[26]<<8)&0xff00| (payload[27])&0x00ff;

        solumPayload.put("altitude", altitude);

        double speed = ((payload[28]<<8)&0xff00 | (payload[29])&0x00ff) * 0.1;

        solumPayload.put("speed", speed);

        int battery = (payload[38])&0xff;

        solumPayload.put("battery", battery);

        int gyroX = (payload[43])&0xff;
        int gyroY = (payload[44])&0xff;
        int gyroZ = (payload[45])&0xff;

        solumPayload.put("gyroX", gyroX);
        solumPayload.put("gyroY", gyroY);
        solumPayload.put("gyroZ", gyroZ);

        return solumPayload;
    }
}
