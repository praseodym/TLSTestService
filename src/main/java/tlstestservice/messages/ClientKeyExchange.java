package tlstestservice.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import tlstestservice.TLS;
import tlstestservice.Utils;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class ClientKeyExchange extends HandshakeMsg {
	byte[] exchangeKeys;
	
	public ClientKeyExchange(byte[] exchangeKeys) throws IOException {
		super(TLS.HANDSHAKE_MSG_TYPE_CLIENT_KEY_EXCHANGE, 0, new byte[] {});
		this.exchangeKeys = exchangeKeys;
		
		ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
		payloadStream.write(Utils.getbytes16(exchangeKeys.length));
		payloadStream.write(exchangeKeys);
		
		payload = payloadStream.toByteArray();
		length = payload.length;
	}
	
	public ClientKeyExchange(HandshakeMsg hs) {
		super(hs.getType(), hs.getLength(), hs.getPayload());
		
		int lenExchangeKeys = Utils.getuint16(payload[0], payload[1]);
		exchangeKeys = Arrays.copyOfRange(payload, 2, 2 + lenExchangeKeys);
	}
	
	public byte[] getExchangeKeys() {
		return exchangeKeys;
	}
}
