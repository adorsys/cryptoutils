package org.adorsys.encobject.service.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.service.api.EncryptionService;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.types.KeyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by peter on 05.03.18 at 10:21.
 */
public class SimpleChunkedEncryptionInputStream extends InputStream {
    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleChunkedEncryptionInputStream.class);
    public final static String DELIMITER_STRING = " ";
    private final static int DELIMITER = DELIMITER_STRING.getBytes()[0];

    private InputStream source;
    private EncryptionService encryptionService;
    private int chunkSize;
    private KeySource keySource;
    private KeyID keyID;
    private Boolean shouldBeCompressed;

    private long sum = 0;
    private boolean eof;

    private byte[] encryptedBytes = null;
    private int encryptedBytesIndex = 0;


    public SimpleChunkedEncryptionInputStream(InputStream inputStream, EncryptionService encryptionService, int chunkSize, KeySource keySource, KeyID keyID, Boolean shouldBeCompressed) {
        this.source = inputStream;
        this.encryptionService = encryptionService;
        this.chunkSize = chunkSize;
        this.keySource = keySource;
        this.keyID = keyID;
        this.shouldBeCompressed = shouldBeCompressed;
    }

    public int read() throws IOException {
        if (eof && encryptedBytes != null && encryptedBytesIndex == encryptedBytes.length) {
            LOGGER.debug("No more encrypted bytes to return");
            return -1;
        }
        if (encryptedBytes != null && encryptedBytesIndex < encryptedBytes.length) {
            return encryptedBytes[encryptedBytesIndex++] & 0xFF;
        }
        if (encryptedBytes != null && encryptedBytesIndex == encryptedBytes.length) {
            encryptedBytesIndex++;
            return DELIMITER;
        }

        if (eof) {
            throw new BaseException("Expected stream not to be finished");
        }

        // Jetzt muss gelesen werden, bis wieder eine ganze ChunkSize voll ist, oder der Stream zu Ende
        LOGGER.debug("es müssen neue bytes zum lesen bereitgestellt werden");
        byte[] bytes = new byte[chunkSize];
        int bytesIndex = 0;

        // Jetzt lesen wir solange, bis ende oder chungsize erreicht
        while (! (eof || bytesIndex == chunkSize)) {
            // LOGGER.debug("eof " + eof + " bytesIndex " + bytesIndex);
            int available = source.available();
            if (available <= 1) {
                // be blocked, until unexpected EOF Exception or Data availabel of expected EOF
                int value = source.read();
                eof = value == -1;
                if (!eof) {
                    bytes[bytesIndex++] = (byte) value;
                    sum++;
                    // LOGGER.debug("READ 1 byte. total " + sum);
                }
            } else {
                int missing = chunkSize - bytesIndex;
                int readNow = Math.min(missing, available);
                // LOGGER.debug("READ " + readNow + " bytes");
                int read = source.read(bytes, bytesIndex, readNow);
                if (read != readNow) {
                    throw new BaseException("expected to read " + readNow + " bytes, but read " + read + " bytes");
                }
                bytesIndex += readNow;
                sum += readNow;
                // LOGGER.debug("read " + readNow + " bytes. total " + sum);
            }
        }
        // Es wurden bytes gelesen, entweder bis chunksize, oder eof

        if (eof) {
            if (bytesIndex == 0) {
                encryptedBytesIndex = encryptedBytes.length;
                return -1;
            }
            if (bytesIndex != chunkSize) {
                byte[] remainder = Arrays.copyOf(bytes, bytesIndex);
                encryptedBytes = encryptionService.encrypt(remainder, keySource, keyID, shouldBeCompressed);
                LOGGER.debug("encrypted last chunk of síze " + remainder.length + " total bytes read so far " + sum);
            } else {
                encryptedBytes = encryptionService.encrypt(bytes, keySource, keyID, shouldBeCompressed);
                LOGGER.debug("encrypted last chunk of síze " + bytes.length + " total bytes read so far " + sum);
            }
        } else {
            if (bytesIndex != chunkSize) {
                throw new BaseException("Programming Error. expected bytesIndex to be " + chunkSize + " but was " + bytesIndex);
            }
            encryptedBytes = encryptionService.encrypt(bytes, keySource, keyID, shouldBeCompressed);
            LOGGER.debug("encrypted next chunk of síze " + bytes.length + " total bytes read so far " + sum);
        }

        encryptedBytesIndex = 0;
        return encryptedBytes[encryptedBytesIndex++] & 0xFF;
    }
}
