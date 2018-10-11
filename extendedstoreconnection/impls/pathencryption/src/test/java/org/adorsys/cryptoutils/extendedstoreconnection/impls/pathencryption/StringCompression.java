package org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption;

/**
 * Created by peter on 11.10.18 16:56.
 */
public interface StringCompression {
    byte[] compress(String s);
    String decompress(byte[] b);
}
