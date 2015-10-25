package tlstestservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class Extension {
	byte[] type;
	byte[] payload;
	
	public Extension(byte[] type, byte[] payload) {
		this.type = type;
		this.payload = payload;
	}
	
	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(type);
		out.write(Utils.getbytes16(payload.length));
		out.write(payload);
		
		return out.toByteArray();
	}
}
