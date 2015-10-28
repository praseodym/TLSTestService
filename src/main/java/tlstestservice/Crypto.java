package tlstestservice;

import java.security.*;
import java.util.Arrays;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class Crypto {
    public static final byte HASH_ALGORITHM_NONE = 0x00;
    public static final byte HASH_ALGORITHM_MD5 = 0x01;
    public static final byte HASH_ALGORITHM_SHA1 = 0x02;
    public static final byte HASH_ALGORITHM_SHA224 = 0x03;
    public static final byte HASH_ALGORITHM_SHA256 = 0x04;
    public static final byte HASH_ALGORITHM_SHA384 = 0x05;
    public static final byte HASH_ALGORITHM_SHA512 = 0x06;

    public static final byte SIGNATURE_ALGORITHM_ANONYMOUS = 0x00;
    public static final byte SIGNATURE_ALGORITHM_RSA = 0x01;
    public static final byte SIGNATURE_ALGORITHM_DSA = 0x02;
    public static final byte SIGNATURE_ALGORITHM_ECDSA = 0x03;

    public static final byte[] HASH_SIGNATURE_ALGORITHM_SHA1RSA = {HASH_ALGORITHM_SHA1, SIGNATURE_ALGORITHM_RSA};
    public static final byte[] HASH_SIGNATURE_ALGORITHM_SHA256RSA = {HASH_ALGORITHM_SHA256, SIGNATURE_ALGORITHM_RSA};

    public static byte[] MD5(byte[] message) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return md.digest(message);
    }

    public static byte[] SHA1(byte[] message) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        return md.digest(message);
    }

    public static byte[] SHA256(byte[] message) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(message);
    }

    public static byte[] HMAC(String algorithm, byte[] key, byte[] message) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        int BLOCKSIZE = 64;

        if (key.length > BLOCKSIZE) {
            key = md.digest(key);
        }
        if (key.length < BLOCKSIZE) {
            key = Arrays.copyOf(key, BLOCKSIZE);
        }

        byte[] ipad = new byte[BLOCKSIZE];
        Arrays.fill(ipad, (byte) 0x36);
        byte[] i_key_pad = Utils.xor(ipad, key);

        byte[] hash_i = md.digest(Utils.concat(i_key_pad, message));

        byte[] opad = new byte[BLOCKSIZE];
        Arrays.fill(opad, (byte) 0x5C);
        byte[] o_key_pad = Utils.xor(opad, key);

        return md.digest(Utils.concat(o_key_pad, hash_i));
    }

    public static byte[] HMAC_MD5(byte[] key, byte[] message) throws Exception {
        return HMAC("MD5", key, message);
    }

    public static byte[] HMAC_SHA1(byte[] key, byte[] message) throws Exception {
        return HMAC("SHA1", key, message);
    }

    public static byte[] HMAC_SHA256(byte[] key, byte[] message) throws Exception {
        return HMAC("SHA-256", key, message);
    }

    public static byte[] HMAC_SHA384(byte[] key, byte[] message) throws Exception {
        return HMAC("SHA-384", key, message);
    }

    public static byte[] HMAC_SHA512(byte[] key, byte[] message) throws Exception {
        return HMAC("SHA-512", key, message);
    }

    public static byte[] SIGN_RSA_SHA1(PrivateKey key, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature instance = Signature.getInstance("SHA1withRSA");
        instance.initSign(key);
        instance.update(data);
        return instance.sign();
    }

    public static byte[] SIGN_RSA_SHA256(PrivateKey key, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature instance = Signature.getInstance("SHA256withRSA");
        instance.initSign(key);
        instance.update(data);
        return instance.sign();
    }
}
