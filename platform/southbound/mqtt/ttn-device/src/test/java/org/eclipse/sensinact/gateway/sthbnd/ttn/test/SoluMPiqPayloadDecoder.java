package org.eclipse.sensinact.gateway.sthbnd.ttn.test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.sthbnd.ttn.packet.PayloadDecoder;

/**
 * Byte order: big endian
 *
 * -------------------------------------------------------------------------------------------------------------------
 * Transaction ID            | 1 byte         | LoRa Packet Sequence Number (Range: 1->255)
 * -------------------------------------------------------------------------------------------------------------------
 * Payload Index             | 1 byte         | Payload Sequence Number
 *                           |                | 0x00: First Payload (Total payload is 1)
 *                           |                | 0x01: First Payload (Total payload is 2)
 *                           |                | 0x01: First Payload (Total payload is 2)
 * -------------------------------------------------------------------------------------------------------------------
 * Packet Type               | 1 byte         | 0 - motion_mode_1
 *                           |                | 1 - motion_mode_2
 * -------------------------------------------------------------------------------------------------------------------
 * Latitude                  | 4 bytes        | IEEE 754 format (N,E : +, S,W : -), degree, float
 * -------------------------------------------------------------------------------------------------------------------
 * Longitude                 | 4 bytes        | IEEE 754 format (N,E : +, S,W : -), degree, float
 * -------------------------------------------------------------------------------------------------------------------
 * Motion 1 or motion 2      | 15 or 13 bytes | See Payload Index and decodeMotion1() or decodeMotion2() for
 *                           |                | further details
 * -------------------------------------------------------------------------------------------------------------------
 * descent_height            | 4 bytes        | Height of descent
 * -------------------------------------------------------------------------------------------------------------------
 *
 * Total -------------------> 30 or 28 bytes
 */
public class SoluMPiqPayloadDecoder implements PayloadDecoder {

    private enum PacketType {
        MODE_0, MODE_1
    }

    private enum RotationLabel {
        ANGLE_0(0, "Pas de figure"),
        ANGLE_180(1, "180°"),
        ANGLE_360(2, "360°"),
        ANGLE_540(3, "540°"),
        ANGLE_720(4, "720°"),
        ANGLE_900(5, "900°"),
        ANGLE_1080(6, "1080°"),
        BACK_FLIP(7, "Back Flip"),
        FRONT_FLIP(8, "Front Flip"),
        MC_TWIST(9, "Mc Twist"),
        FLAIR(10, "Flair"),
        RODEO_FLIP(11, "Rodeo Flip"),
        MISTY_FLIP(12, "Misty Flip"),
        OFF_AXIS(13, "Off Axis"),
        FLAT_SPIN(14, "Flat Spin"),
        D_SPIN(15, "D Spin"),
        LINCOLN(16, "Lincoln"),
        UNKNOWN(17, "Figure inconnue");

        private final int value;
        private final String label;

        RotationLabel(final int value, final String label) {
            this.value = value;
            this.label = label;
        }

        public int getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }

    @Override
    public Map<String, Object> decodeRawPayload(byte[] payload) {
        Map<String, Object> solumPayload = new HashMap<>();

        int latitude = (payload[3]<<24)&0xff000000|
                (payload[4]<<16)&0x00ff0000|
                (payload[5]<< 8)&0x0000ff00|
                (payload[6])&0x000000ff;

        BigDecimal latitudeAsDecimal = BigDecimal.valueOf(Float.intBitsToFloat(latitude));

        int longitude = (payload[7]<<24)&0xff000000|
                (payload[8]<<16)&0x00ff0000|
                (payload[9]<< 8)&0x0000ff00|
                (payload[10])&0x000000ff;

        BigDecimal longitudeAsDecimal = BigDecimal.valueOf(Float.intBitsToFloat(longitude));

        solumPayload.put("position", latitudeAsDecimal.toString() + ":" + longitudeAsDecimal.toString());

        if (payload[2] == 0x00) {
            solumPayload.putAll(decodeMotion1(payload));
        } else {
            solumPayload.putAll(decodeMotion2(payload));
        }

        solumPayload.put("packet_type", payload[2] == 0x00?PacketType.MODE_0:PacketType.MODE_1);

        int height = (payload[payload.length-1]<<24)&0xff000000|
                (payload[payload.length-2]<<16)&0x00ff0000|
                (payload[payload.length-3]<< 8)&0x0000ff00|
                (payload[payload.length-4])&0x000000ff;

        BigDecimal heightAsDecimal = BigDecimal.valueOf(Float.intBitsToFloat(height));

        solumPayload.put("descent_height", heightAsDecimal);

        return solumPayload;
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------
     * turn_type                 | 1 byte  | 0 - left_turn
     *                           |         | 1 - right_turn
     * ------------------------------------------------------------------------------------------------------------------
     * max_angle_ski             | 2 bytes | The maximum angle of the skier from the vertical
     * ------------------------------------------------------------------------------------------------------------------
     * max_velocity              | 2 bytes | Characterizes the speed of entry into the turn of the skier
     * ------------------------------------------------------------------------------------------------------------------
     * max_lateral_force         | 2 bytes | Characterizes the skier's overload at the entrance into the turn
     * ------------------------------------------------------------------------------------------------------------------
     * air_time                  | 2 bytes | Time in air
     * ------------------------------------------------------------------------------------------------------------------
     * smoothness_landing        | 4 bytes | Acceleration with which the skier landed
     * ------------------------------------------------------------------------------------------------------------------
     * rotation_label            | 1 byte  | Type of jump
     * ------------------------------------------------------------------------------------------------------------------
     * score_rotation            | 1 byte  | Dimensionless value that characterizes the complexity of the jump
     * ------------------------------------------------------------------------------------------------------------------
     *
     * Total -------------------> 15 bytes
     *
     * @return the decoded payload
     */
    private static Map<String, Object> decodeMotion1(byte[] payload) {
        Map<String, Object> motion1 = new HashMap<>();

        motion1.put("turn_type", payload[11]==0x00?"left":"right");
        motion1.put("max_angle_ski", ((payload[13]<<8)&0xff00|(payload[12])&0x00ff) / 100.0);
        motion1.put("max_velocity", ((payload[15]<<8)&0xff00|(payload[14])&0x00ff) / 10.0);
        motion1.put("max_lateral_force", ((payload[17]<<8)&0xff00|(payload[16])&0x00ff) / 1000.0);
        motion1.put("air_time", (payload[19]<<8)&0xff00|(payload[18])&0x00ff);

        int landing = (payload[23]<<24)&0xff000000|
                (payload[22]<<16)&0x00ff0000|
                (payload[21]<< 8)&0x0000ff00|
                (payload[20])&0x000000ff;

        BigDecimal landingAsDecimal = BigDecimal.valueOf(Float.intBitsToFloat(landing));

        motion1.put("smoothness_landing", landingAsDecimal);

        String rotationLabel;

        switch(new Byte(payload[24]).intValue()) {
            case 0:
                rotationLabel = RotationLabel.ANGLE_0.getLabel();
                break;
            case 1:
                rotationLabel = RotationLabel.ANGLE_180.getLabel();
                break;
            case 2:
                rotationLabel = RotationLabel.ANGLE_360.getLabel();
                break;
            case 3:
                rotationLabel = RotationLabel.ANGLE_540.getLabel();
                break;
            case 4:
                rotationLabel = RotationLabel.ANGLE_720.getLabel();
                break;
            case 5:
                rotationLabel = RotationLabel.ANGLE_900.getLabel();
                break;
            case 6:
                rotationLabel = RotationLabel.ANGLE_1080.getLabel();
                break;
            case 7:
                rotationLabel = RotationLabel.BACK_FLIP.getLabel();
                break;
            case 8:
                rotationLabel = RotationLabel.FRONT_FLIP.getLabel();
                break;
            case 9:
                rotationLabel = RotationLabel.MC_TWIST.getLabel();
                break;
            case 10:
                rotationLabel = RotationLabel.FLAIR.getLabel();
                break;
            case 11:
                rotationLabel = RotationLabel.RODEO_FLIP.getLabel();
                break;
            case 12:
                rotationLabel = RotationLabel.MISTY_FLIP.getLabel();
                break;
            case 13:
                rotationLabel = RotationLabel.OFF_AXIS.getLabel();
                break;
            case 14:
                rotationLabel = RotationLabel.FLAT_SPIN.getLabel();
                break;
            case 15:
                rotationLabel = RotationLabel.D_SPIN.getLabel();
                break;
            case 16:
                rotationLabel = RotationLabel.LINCOLN.getLabel();
                break;
            default:
                rotationLabel = RotationLabel.UNKNOWN.getLabel();
                break;
        }

        motion1.put("rotation_label", rotationLabel);
        motion1.put("score_rotation", payload[25]);

        return motion1;
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------
     * max_turn_angle_ski_number       | 2 byte  | Number of turns with maximum angle ski
     * ------------------------------------------------------------------------------------------------------------------
     * max_turn_angle_ski_value        | 2 bytes | The maximum angle of the skier from the vertical
     * ------------------------------------------------------------------------------------------------------------------
     * max_turn_velocity_number        | 2 bytes | Number of turns with maximum velocity
     * ------------------------------------------------------------------------------------------------------------------
     * max_turn_velocity_value         | 2 bytes | Characterizes the skier's speed of entry into the turn of the skier
     * ------------------------------------------------------------------------------------------------------------------
     * max_jump_air_time_number        | 1 byte  | Number of jumps with maximum air time
     * ------------------------------------------------------------------------------------------------------------------
     * max_jump_air_time               | 2 bytes | Maximum time in air
     * ------------------------------------------------------------------------------------------------------------------
     * best_jump_score_rotation_number | 1 byte  | Number of jumps with best score rotation
     * ------------------------------------------------------------------------------------------------------------------
     * best_jump_score_rotation        | 1 byte  | Dimensionless value that characterizes the complexity of the jump
     * ------------------------------------------------------------------------------------------------------------------
     *
     * Total -------------------> 13 bytes
     *
     * @return the decoded payload
     */
    private static Map<String, Object> decodeMotion2(byte[] payload) {
        Map<String, Object> motion2 = new HashMap<>();

        motion2.put("max_turn_angle_ski_number", (payload[12]<<8)&0xff00|(payload[11])&0x00ff);
        motion2.put("max_turn_angle_ski_value", ((payload[14]<<8)&0xff00|(payload[13])&0x00ff) / 100.0);
        motion2.put("max_turn_velocity_number", (payload[16]<<8)&0xff00|(payload[15])&0x00ff);
        motion2.put("max_turn_velocity_value", ((payload[18]<<8)&0xff00|(payload[17])&0x00ff) / 10.0);
        motion2.put("max_jump_air_time_number", (payload[19]));
        motion2.put("max_jump_air_time", (payload[21]<<8)&0xff00|(payload[20])&0x00ff);
        motion2.put("best_jump_score_rotation_number", (payload[22]));
        motion2.put("best_jump_score_rotation", (payload[23]));

        return motion2;
    }
}
