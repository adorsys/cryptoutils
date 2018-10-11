package org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;

import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Created by peter on 11.10.18 15:07.
 */
public class DeflaterCompression implements StringCompression{
    public byte[] compress(String inputString) {
        try {
            byte[] input = inputString.getBytes("UTF-8");

            // Compress the bytes
            byte[] output = new byte[200];
            Deflater compresser = new Deflater();
            compresser.setInput(input);
            compresser.finish();
            int compressedDataLength = compresser.deflate(output);
            byte[] b = Arrays.copyOf(output, compressedDataLength);
            return b;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
    public String decompress(byte[] output) {
        try {
            // Decompress the bytes
            Inflater decompresser = new Inflater();
            decompresser.setInput(output, 0, output.length);
            byte[] result = new byte[100];
            int resultLength = decompresser.inflate(result);
            decompresser.end();

            // Decode the bytes into a String
            String outputString = new String(result, 0, resultLength, "UTF-8");
            return outputString;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}