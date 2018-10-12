package org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption;

import org.adorsys.cryptoutils.utils.HexUtil;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;

/**
 * Created by peter on 11.10.18 14:53.
 */
public class StringCompressionTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(StringCompressionTest.class);
    private final static Charset CHARSET = Charset.forName("UTF-8");


    // Macht also erst ab 80 Zeichen Sinn
    // @Test
    public void gzip() {
        test(new GzipCompression());
    }

    // Macht also erst ab 54 Zeichen Sinn
    // @Test
    public void deflater() {
        test(new DeflaterCompression());
    }

    // Kann nicht benutzt werden, da output eine Liste von Ints ist, die zwischen 0 und 4096 liegen, also 1,5 bytes.
    // @Test
    public void lzw() {
        test(new LZWCompression());
    }

    // @Test
    public void lzwr() {
        LZWCompression lzw = new LZWCompression();

        byte[] bytes = new byte[500];
        new Random().nextBytes(bytes);
        String s = HexUtil.convertBytesToHexString(bytes);

        for (int i = 1; i < s.length(); i++) {
            String testString = s.substring(0, i);

            int bytesBefore = testString.getBytes(CHARSET).length;
            List<Integer> integers = lzw.realCompress(testString);
            String decompressedString = lzw.realDecompress(integers);
            Assert.assertEquals(testString, decompressedString);

            LOGGER.info(String.format("%3d -> %3d -> %3f ", bytesBefore, integers.size(), integers.size() * 1.5));
        }

    }

    public void test(StringCompression stringCompression) {
        byte[] bytes = new byte[50];
        new Random().nextBytes(bytes);
        String s = HexUtil.convertBytesToHexString(bytes);

        for (int i = 1; i < s.length(); i++) {
            String testString = s.substring(0, i);

            int bytesBefore = testString.getBytes(CHARSET).length;
            byte[] compressedBytes = stringCompression.compress(testString);
            String decompressedString = stringCompression.decompress(compressedBytes);
            Assert.assertEquals(testString, decompressedString);
            int bytesAfter = compressedBytes.length;

            LOGGER.info(String.format("%3d -> %3d = %6d", bytesBefore, bytesAfter, (1.5*bytesAfter) ) + " " + (bytesAfter < bytesBefore ? "OK" : ""));
        }
    }


    // interne konvertierungstest

    // @Test
    public void int2byte() {
        for (int i = 0; i<10; i++) {
            logInt(i);
        }
        for (int i = 123; i<133; i++) {
            logInt(i);
        }
        for (int i = 246; i<256; i++) {
            logInt(i);
        }
    }

    private void logInt(int value) {
        byte b = convert(value);
        int i2 = convert(b);
        LOGGER.info(String.format("%3d", value)
                + " -> " + String.format("%3d", i2)
                + " BYTE " + byteToBinaryString(b));

    }

    String byteToBinaryString(byte b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 7; i>=0; i--) {
            sb.append((b & (1 << i)) > 0 ? "1" : "0");
        }
        return sb.toString();
    }

    public int convert(byte b) {
        return new Byte(b).intValue() & 0xff;
    }

    public byte convert(int i) {
        byte b = new Integer(i).byteValue();
        return (byte) ((int) b & (0xff));
    }


}
