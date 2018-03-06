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
    public static final int MINI_CHUNK = 4096;

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
        if (decryptedBytes != null) {
            if (decryptedBytesIndex == decryptedBytes.length) {
                decryptedBytes = null;
                return -1;
            }
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
        LOGGER.info("ableToDecryptRestBytes " + eof);
        if (delimiterFound) {
            return true;
        }
        if (eof) {
            return true;
        }
        return false;
    }

    private void findMoreRestBytes() {
        LOGGER.info("findMoreRestBytes " + sum + " eof " + eof);
        try {
            byte[] newBytes = new byte[MINI_CHUNK];
            int newByteIndex = 0;
            int value;
            while (!(eof || delimiterFound)) {
                value = source.read();
                sum++;
                eof = value == -1;
                if (!eof) {
                    newBytes[newByteIndex++] = (byte) value;
                    delimiterFound = value == DELIMITER;
                }
                if (newByteIndex == MINI_CHUNK) {
                    restBytes = add(restBytes, newBytes);
                    newBytes = new byte[MINI_CHUNK];
                    newByteIndex = 0;
                }
            }
            byte[] bytesToAdd = copy(newBytes,0, newByteIndex);
            restBytes = add(restBytes, bytesToAdd);
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

