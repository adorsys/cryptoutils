package org.adorsys.cryptoutils.utils;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by peter on 14.02.18 at 10:17.
 */
public class HexUtilTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(HexUtilTest.class);
    @Test
    public void test1() {
        String value="pfad/subdir/filename.txt";
        value="0123456789!\"§$%&/()=abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        byte[] bytesOfValue = value.getBytes(Charset.forName("UTF8"));
        String hexString = HexUtil.convertBytesToHexString(bytesOfValue);
        byte[] reconvertedBytes = HexUtil.convertHexStringToBytes(hexString);
        String reconertedString = new String(reconvertedBytes, Charset.forName("UTF8"));

        LOGGER.debug("value             :" + value);
        LOGGER.debug("hexString         :" + hexString);
        LOGGER.debug("reconverted value :" + reconertedString);

        Assert.assertTrue(Arrays.equals(bytesOfValue,reconvertedBytes));
        Assert.assertEquals(reconertedString, value);

    }
}
