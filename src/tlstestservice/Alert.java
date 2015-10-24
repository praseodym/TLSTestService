package tlstestservice;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class Alert {
	private byte level;
	private byte description;
	
	public Alert(InputStream msg) throws IOException {
		level = (byte)(0xFF & msg.read());
		description = (byte)(0xFF & msg.read());		
	}
	
	public Alert(byte level, byte description) {
		this.level = level;
		this.description = description;
	}
	
	public byte[] getBytes() {
		return new byte[] {level, description};
	}
	
	public byte getLevel() {
		return level;
	}
	
	public byte getDescription() {
		return description;
	}
}
