package org.adorsys.encobject.service.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
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

    private byte[] restBytes = null;
    private boolean delimiterFound = false;

    public SimpleChunkedDecryptionInputStream(InputStream inputStream, EncryptionService encryptionService, KeySource keySource) {
        this.source = inputStream;
        this.encryptionService = encryptionService;
        this.keySource = keySource;
    }

    @Override
    public int available() {
        if (decryptedBytes == null) {
            return 0;
        }
        return decryptedBytes.length - decryptedBytesIndex;
    }

    @Override
    public int read() throws IOException {
        if (eof && decryptedBytes != null && decryptedBytesIndex == decryptedBytes.length) {
            LOGGER.debug("No more decrypted bytes to return");
            return -1;
        }
        if (decryptedBytes != null && decryptedBytesIndex < decryptedBytes.length) {
            return decryptedBytes[decryptedBytesIndex++] & 0xFF;
        }

        if (eof) {
            return -1;
        }

        while (!ableToDecryptRestBytes()) {
            findMoreRestBytes();
        }
        decryptRestBytes();

        if (decryptedBytes == null) {
            return -1;
        }
        return decryptedBytes[decryptedBytesIndex++] & 0xFF;
    }

    private boolean ableToDecryptRestBytes() {
        LOGGER.info("ableToDecryptRestBytes");
        if (restBytes == null) {
            return false;
        }
        if (eof) {
            return true;
        }
        return delimiterFound;
    }

    private void findMoreRestBytes() {
        LOGGER.info("findMoreRestBytes");
        try {
            while (!(eof || delimiterFound)) {
                // LOGGER.info("eof " + eof + " delimiterFound " + delimiterFound);
                int available = 0;
                available = source.available();
                // LOGGER.info("available:" + available);
                byte[] newBytes;
                if (available <= 1) {
                    newBytes = new byte[1];
                    int value = source.read();
                    eof = value == -1;
                    if (!eof) {
                        newBytes[0] = (byte) value;
                    }
                } else {
                    newBytes = new byte[available];
                    int read = source.read(newBytes);
                    if (read != available) {
                        throw new BaseException("expected to read " + available + " but read " + read);
                    }
                }
                if (! eof) {
                    for (int i = 0; !(delimiterFound) && i < newBytes.length; i++) {
                        delimiterFound = newBytes[i] == DELIMITER;
                    }
                    sum += newBytes.length;
                    // LOGGER.info("delimter founde:" + delimiterFound + " SUM " + sum);
                    restBytes = add(restBytes, newBytes);
                }
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


    private void decryptRestBytes() {
        int delPos = -1;
        for (int i = 0; i < restBytes.length; i++) {
            if (restBytes[i] == DELIMITER) {
                delPos = i;
                break;
            }
        }
        if (delPos == -1) {
            if (! eof) {
                throw new BaseException("EOF expected");
            }
            if (restBytes == null || restBytes.length == 0) {
                decryptedBytes = null;
                decryptedBytesIndex = 0;
            } else {
                decryptedBytes = encryptionService.decrypt(restBytes, keySource);
                LOGGER.debug("1decrypted " + restBytes.length + " to " + decryptedBytes.length);
                decryptedBytesIndex = 0;
                delimiterFound = false;
            }
        } else {
            byte[] toBeDecrytped = copy(restBytes, 0, delPos);
            restBytes = copy(restBytes, delPos + 1, restBytes.length);
            decryptedBytes = encryptionService.decrypt(toBeDecrytped, keySource);
            LOGGER.debug("2decrypted " + toBeDecrytped.length + " to " + decryptedBytes.length);
            decryptedBytesIndex = 0;
            delimiterFound = false;
        }
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

    private static byte[] copy(byte[] src, int beginIndex, int endIndex) {
        int size = endIndex - beginIndex;
        byte[] result = new byte[size];
        for (int i = 0; i<size; i++) {
            result[i] = src[beginIndex + i];
        }
        return result;
    }


}

