package tlstestservice;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class CipherSuite {
	public final static String ALG_RSA = "RSA";
	public final static String ALG_DHE_RSA = "DHE_RSA";

	public final static byte[] TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA = new byte[] {0x00, 0x16};
	public final static byte[] TLS_RSA_WITH_AES_128_CBC_SHA = new byte[] {0x00, 0x2F};
	public final static byte[] TLS_DHE_RSA_WITH_AES_128_CBC_SHA = new byte[] {0x00, 0x33};
	public final static byte[] TLS_RSA_WITH_3DES_EDE_CBC_SHA = new byte[] {0x00, 0x0A};
	
	Cipher keyExchangeCipher;
	String keyExchange;
	
	String encCipherAlg;
	String encCipherKeyAlg;
	int encCipherKeySize;
	int ivSize;
	
	Mac macCipher;
	int hashSize;
	String macCipherAlg;
	
	
	public CipherSuite(byte[] cipherSuite) throws Exception {
		if(cipherSuite.length != 2) throw new Exception("Invalid cipher suite length");
		
		if(cipherSuite[0] == TLS_RSA_WITH_AES_128_CBC_SHA[0] && cipherSuite[1] == TLS_RSA_WITH_AES_128_CBC_SHA[1]) {
			keyExchangeCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			keyExchange = ALG_RSA;
			
			encCipherAlg = "AES/CBC/NoPadding";
			encCipherKeyAlg = "AES";
			encCipherKeySize = 16;
			ivSize = 16;
			
			macCipherAlg = "HmacSHA1";
			hashSize = 20;
		}
		else if(cipherSuite[0] == TLS_DHE_RSA_WITH_AES_128_CBC_SHA[0] && cipherSuite[1] == TLS_DHE_RSA_WITH_AES_128_CBC_SHA[1]) {
			keyExchangeCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			keyExchange = ALG_DHE_RSA;
			
			encCipherAlg = "AES/CBC/NoPadding";
			encCipherKeyAlg = "AES";
			encCipherKeySize = 16;
			ivSize = 16;
			
			macCipherAlg = "HmacSHA1";
			hashSize = 20;			
		}
		else if(cipherSuite[0] == TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA[0] && cipherSuite[1] == TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA[1]) {
			keyExchangeCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			keyExchange = ALG_DHE_RSA;
			
			encCipherAlg = "DESede/CBC/NoPadding";
			encCipherKeyAlg = "DESede";
			encCipherKeySize = 24;
			ivSize = 8;
			
			macCipherAlg = "HmacSHA1";
			hashSize = 20;			
		}
		else if(cipherSuite[0] == TLS_RSA_WITH_3DES_EDE_CBC_SHA[0] && cipherSuite[1] == TLS_RSA_WITH_3DES_EDE_CBC_SHA[1]) {
			keyExchangeCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			keyExchange = ALG_RSA;
			
			encCipherAlg = "DESede/CBC/NoPadding";
			encCipherKeyAlg = "DESede";
			encCipherKeySize = 24;
			ivSize = 8;
			
			macCipherAlg = "HmacSHA1";
			hashSize = 20;
		}		
		else {
			System.out.println("Unknown cipher suite: " + cipherSuite[0] + "  "  + cipherSuite[1]);
		}
	}
	
	public Mac getMAC() throws NoSuchAlgorithmException {
		return Mac.getInstance(macCipherAlg);
	}
	
	public Cipher getEncCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
		return Cipher.getInstance(encCipherAlg);
	}
}
