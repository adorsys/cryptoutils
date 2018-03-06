package org.adorsys.encobject.service.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.service.api.EncryptionService;
import org.adorsys.encobject.service.api.KeySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by peter on 05.03.18 at 16:34.
 */
public class SimpleChunkedDecryptionInputStream extends InputStream {
    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleChunkedDecryptionInputStream.class);
    public final static String DELIMITER_STRING = " ";
    private final static int DELIMITER = DELIMITER_STRING.getBytes()[0];

    private InputStream source;
    private EncryptionService encryptionService;
    private KeySource keySource;

    private long sum = 0;
    private boolean eof;

    private byte[] decryptedBytes = null;
    private int decryptedBytesIndex = 0;


    public SimpleChunkedDecryptionInputStream(InputStream inputStream, EncryptionService encryptionService, KeySource keySource) {
        this.source = inputStream;
        this.encryptionService = encryptionService;
        this.keySource = keySource;
    }

    public int read() throws IOException {
        if (eof && decryptedBytes != null && decryptedBytesIndex == decryptedBytes.length) {
            LOGGER.debug("No more decrypted bytes to return");
            return -1;
        }
        if (decryptedBytes != null && decryptedBytesIndex < decryptedBytes.length) {
            return decryptedBytes[decryptedBytesIndex++];
        }

        if (eof) {
            throw new BaseException("Expected stream not to be finished");
        }

        // Jetzt muss gelesen werden, bis wieder ein Delimiter gefunden wurde, oder der Stream zu Ende
        LOGGER.debug("es müssen neue bytes zum lesen bereitgestellt werden");
        byte[] encryptedBytes = null;

        boolean delimiterFound = false;
        // Jetzt lesen wir solange, bis ende oder chungsize erreicht
        while (!(eof || delimiterFound)) {
            // LOGGER.debug("eof " + eof + " delimiterFound " + delimiterFound);
            // be blocked, until unexpected EOF Exception or Data availabel of expected EOF
            int value = source.read();
            eof = value == -1;
            delimiterFound = (value == DELIMITER);
            if (! (eof  || delimiterFound)) {
                byte[] newEncryptedBytes = new byte[1];
                newEncryptedBytes[0] = (byte) value;
                encryptedBytes = add(encryptedBytes, newEncryptedBytes);
                sum++;
                // LOGGER.debug("READ 1 byte. total " + sum);
            }
        }
        // Es wurden bis zum Ende oder delimiter gelesen

        if (eof) {
            if (encryptedBytes == null) {
                decryptedBytesIndex = decryptedBytes.length;
                return -1;
            }
            decryptedBytes = encryptionService.decrypt(encryptedBytes, keySource);
            LOGGER.debug("decrypted last chunk of síze " + encryptedBytes.length + " total bytes read so far " + sum);

        } else {
            if (encryptedBytes == null) {
                throw new BaseException("Programming Error. expected encrypted byets not to be null");
            }
            decryptedBytes = encryptionService.decrypt(encryptedBytes, keySource);
            LOGGER.debug("decrypted next chunk of síze " + encryptedBytes.length + " total bytes read so far " + sum);
        }

        decryptedBytesIndex = 0;
        return decryptedBytes[decryptedBytesIndex++];
    }

    public static byte[] add(byte[] byteArray1, byte[] byteArray2) {
        if (byteArray1 == null) {
            return byteArray2;
        }
        byte[] result = new byte[byteArray1.length + byteArray2.length];
        int i = 0;
        for (; i < byteArray1.length; i++) {
            result[i] = byteArray1[i];
        }
        for (int j = 0; j < byteArray2.length; j++) {
            result[i + j] = byteArray2[j];
        }
        return result;
    }
}

