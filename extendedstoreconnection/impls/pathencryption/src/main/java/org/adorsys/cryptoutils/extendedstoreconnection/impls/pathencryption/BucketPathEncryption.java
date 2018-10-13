package org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.BucketPathUtil;
import org.adorsys.encobject.exceptions.PathDecryptionException;
import org.adorsys.encobject.exceptions.PathEncryptionException;
import org.adorsys.encobject.types.BucketPathEncryptionPassword;
import org.adorsys.encobject.types.properties.BucketPathEncryptionFilenameOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by peter on 25.09.18.
 * Quellcode aus https://blog.axxg.de/java-aes-verschluesselung-mit-beispiel/
 */
public class BucketPathEncryption {
    private final static Logger LOGGER = LoggerFactory.getLogger(BucketPathEncryption.class);
    public static final String UTF_8 = "UTF-8";
    private final static Charset CHARSET = Charset.forName(UTF_8);

    public static BucketDirectory encrypt(BucketPathEncryptionPassword bucketPathEncryptionPassword,
                                          BucketPathEncryptionFilenameOnly bucketPathEncryptionFilenameOnly,
                                          BucketDirectory bucketDirectory) {
        return new BucketDirectory(encrypt(bucketPathEncryptionPassword,
                bucketPathEncryptionFilenameOnly,
                BucketPathUtil.getAsString(bucketDirectory),
                false));
    }

    public static BucketPath encrypt(BucketPathEncryptionPassword bucketPathEncryptionPassword,
                                     BucketPathEncryptionFilenameOnly bucketPathEncryptionFilenameOnly,
                                     BucketPath bucketPath) {
        return new BucketPath(encrypt(bucketPathEncryptionPassword,
                bucketPathEncryptionFilenameOnly,
                BucketPathUtil.getAsString(bucketPath),
                true));
    }

    public static BucketDirectory decrypt(BucketPathEncryptionPassword bucketPathEncryptionPassword,
                                          BucketPathEncryptionFilenameOnly bucketPathEncryptionFilenameOnly,
                                          BucketDirectory bucketDirectory) {
        return new BucketDirectory(decrypt(bucketPathEncryptionPassword,
                bucketPathEncryptionFilenameOnly,
                BucketPathUtil.getAsString(bucketDirectory),
                false));
    }

    public static BucketPath decrypt(BucketPathEncryptionPassword bucketPathEncryptionPassword,
                                     BucketPathEncryptionFilenameOnly bucketPathEncryptionFilenameOnly,
                                     BucketPath bucketPath) {
        return new BucketPath(decrypt(bucketPathEncryptionPassword,
                bucketPathEncryptionFilenameOnly,
                BucketPathUtil.getAsString(bucketPath),
                true));
    }

    private static String encrypt(BucketPathEncryptionPassword bucketPathEncryptionPassword,
                                  BucketPathEncryptionFilenameOnly bucketPathEncryptionFilenameOnly,
                                  String fullString,
                                  Boolean isPath
    ) {
        try {
            Cipher cipher = createCipher(bucketPathEncryptionPassword, Cipher.ENCRYPT_MODE);

            StringBuilder encryptedPath = new StringBuilder();
            StringTokenizer st = new StringTokenizer(fullString, BucketPath.BUCKET_SEPARATOR);
            while (st.hasMoreTokens()) {
                String plainString = st.nextToken();
                // LOGGER.debug("encrypt: plain string " + plainString);
                String encryptedBytesAsHexString = null;
                if (isPath && !st.hasMoreTokens()) {
                    // the last token, so it has to be encrypted anyway
                    byte[] compressedBytes = compress(plainString);
                    // LOGGER.debug("encrypt: plain bytes as hex string " + HexUtil.convertBytesToHexString(compressedBytes));
                    byte[] encryptedBytes = cipher.doFinal(compressedBytes);
                    encryptedBytesAsHexString = HexUtil.convertBytesToHexString(encryptedBytes).toLowerCase();
                } else {
                    // not the last token, so it has to be encrepted depending
                    if (bucketPathEncryptionFilenameOnly.equals(BucketPathEncryptionFilenameOnly.TRUE)) {
                        // true means, do not encrypt
                        encryptedBytesAsHexString = plainString;
                    } else {
                        byte[] compressedBytes = compress(plainString);
                        // LOGGER.debug("encrypt: plain bytes as hex string " + HexUtil.convertBytesToHexString(compressedBytes));
                        byte[] encryptedBytes = cipher.doFinal(compressedBytes);
                        encryptedBytesAsHexString = HexUtil.convertBytesToHexString(encryptedBytes).toLowerCase();
                    }
                }
                // LOGGER.debug("encrypt: ecnrypted bytes as hex string " + encryptedBytesAsHexString);
                encryptedPath.append(BucketPath.BUCKET_SEPARATOR + encryptedBytesAsHexString);
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("%3d -> %3d", fullString.length(), encryptedPath.toString().length()) + " encrypted (" + fullString + ") -> (" + encryptedPath.toString() + ")");
            }
            return encryptedPath.toString();
        } catch (Exception e) {
            throw new PathEncryptionException(fullString, e);
        }

    }

    public static String decrypt(BucketPathEncryptionPassword BucketPathEncryptionPassword,
                                 BucketPathEncryptionFilenameOnly bucketPathEncryptionFilenameOnly,
                                 String encryptedHexString,
                                 Boolean isPath) {
        try {
            Cipher cipher = createCipher(BucketPathEncryptionPassword, Cipher.DECRYPT_MODE);

            StringBuilder plainPath = new StringBuilder();
            StringTokenizer st = new StringTokenizer(encryptedHexString, BucketPath.BUCKET_SEPARATOR);
            while (st.hasMoreTokens()) {
                String encryptedBytesAsHexString = st.nextToken();
                String plainString = null;

                if (isPath && !st.hasMoreTokens()) {
                    // the last token, so it has to be decrypted anyway
                    // LOGGER.debug("decrypt: encrpyted bytes as hex string (orig)       :" + encryptedBytesAsHexString);
                    byte[] encryptedBytes = HexUtil.convertHexStringToBytes(encryptedBytesAsHexString.toUpperCase());
                    // LOGGER.debug("decrypt: encrpyted bytes as hex string (reconverted):" + HexUtil.convertBytesToHexString(encryptedBytes));
                    byte[] compressedBytes = cipher.doFinal(encryptedBytes);
                    // LOGGER.debug("decrypt: plain bytes as hex string:" + HexUtil.convertBytesToHexString(compressedBytes));
                    plainString = decompress(compressedBytes);
                } else {
                    // not the last token, so it has to be decrypted depending
                    if (bucketPathEncryptionFilenameOnly.equals(BucketPathEncryptionFilenameOnly.TRUE)) {
                        plainString = encryptedBytesAsHexString;
                    } else {
                        // LOGGER.debug("decrypt: encrpyted bytes as hex string (orig)       :" + encryptedBytesAsHexString);
                        byte[] encryptedBytes = HexUtil.convertHexStringToBytes(encryptedBytesAsHexString.toUpperCase());
                        // LOGGER.debug("decrypt: encrpyted bytes as hex string (reconverted):" + HexUtil.convertBytesToHexString(encryptedBytes));
                        byte[] compressedBytes = cipher.doFinal(encryptedBytes);
                        // LOGGER.debug("decrypt: plain bytes as hex string:" + HexUtil.convertBytesToHexString(compressedBytes));
                        plainString = decompress(compressedBytes);
                    }
                }
                // LOGGER.debug("decrypt: plain string " + plainString);
                plainPath.append(BucketPath.BUCKET_SEPARATOR + plainString);
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("%3d -> %3d", encryptedHexString.length(), plainPath.toString().length()) + " decrypted (" + encryptedHexString + ") -> (" + plainPath.toString() + ")");
            }
            return plainPath.toString();
        } catch (Exception e) {
            throw new PathDecryptionException(encryptedHexString, e);
        }
    }

    private static Cipher createCipher(BucketPathEncryptionPassword bucketPathEncryptionPassword, int cipherMode) {
        try {
            byte[] key = bucketPathEncryptionPassword.getValue().getBytes(UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            // nur die ersten 128 bit nutzen
            key = Arrays.copyOf(key, 16);
            // der fertige Schluessel
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, secretKeySpec);
            return cipher;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }

    private static byte[] compress(String data) {
        return data.getBytes(CHARSET);
    }

    private static String decompress(byte[] compressed) {
        return new String(compressed, CHARSET);
    }
}
