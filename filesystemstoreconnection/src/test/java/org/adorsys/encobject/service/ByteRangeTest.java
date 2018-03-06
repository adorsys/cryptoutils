package org.adorsys.encobject.service;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 06.03.18 at 15:25.
 */
public class ByteRangeTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(ByteRangeTest.class);

    @Test
    public void byteRange() {
        byte b = -1;
        int b2 = b & 0xFF;
        int i = b;
        int i2 = (int) b;
        LOGGER.info("b:" +b) ;
        LOGGER.info("i:" +i) ;
        LOGGER.info("i2:" +i2) ;
        LOGGER.info("b2:" +b2) ;
        // Siehe getTrickContent in EncryptedPersistenceServiceTest
    }
}
