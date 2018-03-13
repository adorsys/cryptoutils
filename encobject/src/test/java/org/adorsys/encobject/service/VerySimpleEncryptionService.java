package org.adorsys.encobject.service;

import org.adorsys.encobject.service.api.EncryptionStreamService;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.types.KeyID;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by peter on 05.03.18 at 16:37.
 */
public class VerySimpleEncryptionService implements EncryptionStreamService {
    @Override
    public InputStream getEncryptedInputStream(InputStream inputStream, KeySource keySource, KeyID keyID, Boolean compress) {
        return new VerySimpleEncryptionStream(inputStream);
    }

    @Override
    public InputStream getDecryptedInputStream(InputStream inputStream, KeySource keySource, KeyID keyID) {
        return new VerySimpleEncryptionStream(inputStream);
    }


    private static class VerySimpleEncryptionStream extends InputStream {
        private InputStream source;
        public VerySimpleEncryptionStream(InputStream source) {
            this.source = source;
        }

        @Override
        public int read() throws IOException{
            int value = source.read();
            if (value == -1) {
                return value;
            }
            int newValue = (byte) (255 - (byte)value);
            return newValue & 0xFF;
        }

    }
}
