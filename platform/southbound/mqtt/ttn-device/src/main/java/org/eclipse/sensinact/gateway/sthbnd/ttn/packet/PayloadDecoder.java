package org.eclipse.sensinact.gateway.sthbnd.ttn.packet;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public interface PayloadDecoder {
	public class HexDecoder {
		private static final byte[] encodingTable;
		static {
			String encodingString = "0123456789abcdef";
			encodingTable = new byte[encodingString.length()];
			for(int n=0;n<encodingString.length();n++)
				encodingTable[n]=(byte) encodingString.charAt(n);
		}

		public static String toHexString(byte[] data) {  
			int length = data.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();      
	        for (int i = 0; i <  length; i++) {
	            int    v = data[i] & 0xff;
	            out.write(encodingTable[(v >>> 4)]);
	            out.write(encodingTable[v & 0xf]);
	        }
	        byte[] bytes = out.toByteArray();
	        char[] chars = new char[bytes.length];

	        for (int i = 0; i != chars.length; i++)
	            chars[i] = (char)(bytes[i] & 0xff);
	        return new String(chars);
		}

	}
	
    public class Base64BinaryDecoder {
    	
	    private static final byte PADDING = 127;
	    private static final byte[] decodeMap = new byte[128]; 
	
	    static {
		    String RAW = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
		    byte[] map = new byte[128];
		    int i;
		    for (i = 0; i < 128; i++) {
		      	map[i] = (byte) RAW.indexOf(i);
		    }
		    map['='] = PADDING;
		    System.arraycopy(map,0,decodeMap,0,128);
		 }
	    
	    private static int length(String text) {
	        final int len = text.length();
	        int j = len - 1;
	        for (; j >= 0;) {
	            byte code = decodeMap[text.charAt(j)];
	            if (code == PADDING){
	            	j-=1;
	                continue;
	        	}
	            if (code == -1)
	                return text.length() / 4 * 3;
	            break;
	        }
	        j++;
	        int padSize = len - j;
	        if (padSize > 2)
	            return text.length() / 4 * 3;
	        return text.length() / 4 * 3 - padSize;
	    }
	    
	    public static byte[] parseBase64Binary(String text) {
	        final int buflen = length(text);
	        final byte[] out = new byte[buflen];
	        int o = 0;
	
	        final int len = text.length();
	        int i;
	
	        final byte[] quadruplet = new byte[4];
	        int q = 0;
	
	        // convert each quadruplet to three bytes.
	        for (i = 0; i < len; i++) {
	            char ch = text.charAt(i);
	            byte v = decodeMap[ch];
	
	            if (v != -1)            	
	                quadruplet[q++] = v;
	
	            if (q == 4) {
	                // quadruplet is now filled.
	                out[o++] = (byte) ((quadruplet[0] << 2) | (quadruplet[1] >> 4));
	                if (quadruplet[2] != PADDING) {
	                    out[o++] = (byte) ((quadruplet[1] << 4) | (quadruplet[2] >> 2));
	                }
	                if (quadruplet[3] != PADDING) {
	                    out[o++] = (byte) ((quadruplet[2] << 6) | (quadruplet[3]));
	                }
	                q = 0;
	            }
	        }
	        if (buflen == o)
	            return out;
	        byte[] nb = new byte[o];
	        System.arraycopy(out, 0, nb, 0, o);
	        return nb;
	    }
    }
    
	Map<String, Object> decodeRawPayload(String payload);

}
