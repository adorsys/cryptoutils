package org.adorsys.encobject.service;

import de.adorsys.common.exceptions.BaseException;
import de.adorsys.dfs.connection.api.domain.UserMetaData;
import org.adorsys.encobject.service.api.EncryptionStreamService;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.types.KeyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by peter on 05.03.18 at 16:37.
 */
public class VerySimpleEncryptionService implements EncryptionStreamService {
    private final static Logger LOGGER = LoggerFactory.getLogger(VerySimpleEncryptionService.class);
    @Override
    public InputStream getEncryptedInputStream(UserMetaData userMetaData, InputStream inputStream, KeySource keySource, KeyID keyID, Boolean compress) {
        new BaseException("just to see, where new EncryptionStream is created");
        return new VerySimpleEncryptionStream(inputStream);
    }

    @Override
    public InputStream getDecryptedInputStream(UserMetaData userMetaData, InputStream inputStream, KeySource keySource, KeyID keyID) {
        new BaseException("just to see, where new DecryptionStream is created");
        return new VerySimpleEncryptionStream(inputStream);
    }


    private static class VerySimpleEncryptionStream extends InputStream {
        private int counterBytesRead = 0;
        private final static Logger LOGGER = LoggerFactory.getLogger(VerySimpleEncryptionStream.class);
        private static int instanceCounter = 0;
        private int instanceId = 0;
        private InputStream source;
        public VerySimpleEncryptionStream(InputStream source) {
            instanceId = instanceCounter++;
            LOGGER.info("create new Stream " + instanceId);
            this.source = source;
        }

        @Override
        public int read() throws IOException{
//            LOGGER.info("read stream " + instanceId);
            int value = source.read();
            if (value == -1) {
                LOGGER.info("read stream " + instanceId + " returns -1 after " + counterBytesRead + " bytes. meaning stream has ended");
//                new BaseException("just to see, where stream is read with -1");
                return value;
            }
            counterBytesRead++;
            int newValue = (byte) (255 - (byte)value);
            return newValue & 0xFF;
        }

        @Override
        public void close() throws IOException{
            LOGGER.info("close stream " + instanceId + " after " + counterBytesRead + " bytes have been read");
            // new BaseException("just to see, where stream is closed");
            source.close();
        }

    }
}
