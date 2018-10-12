package org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption;

import org.adorsys.cryptoutils.exceptions.BaseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Created by peter on 11.10.18 15:24.
 */
public class LZWCompression implements StringCompression {
    /** Compress a string to a list of output symbols. */

    public byte[] compress(String uncompressed) {
        List<Integer> ints = realCompress(uncompressed);
        byte[] bytes = new byte[ints.size()];
        for (int i = 0; i<ints.size(); i++) {
            bytes[i]=convert(ints.get(i));
        }
        return bytes;
    }

    public String decompress(byte[] bytes) {
        List<Integer> ints = new ArrayList();
        for (int i = 0; i<bytes.length; i++) {
            ints.add(convert(bytes[i]));
        }
        return realDecompress(ints);
    }

    public List<Integer> realCompress(String uncompressed) {
        // Build the dictionary.
        int dictSize = 256;
        Map<String,Integer> dictionary = new HashMap<String,Integer>();
        for (int i = 0; i < 256; i++)
            dictionary.put("" + (char)i, i);

        String w = "";
        List<Integer> result = new ArrayList<Integer>();
        for (char c : uncompressed.toCharArray()) {
            String wc = w + c;
            if (dictionary.containsKey(wc))
                w = wc;
            else {
                result.add(dictionary.get(w));
                // Add wc to the dictionary.
                dictionary.put(wc, dictSize++);
                w = "" + c;
            }
        }

        // Output the code for w.
        if (!w.equals(""))
            result.add(dictionary.get(w));
        return result;
    }

    /** Decompress a list of output ks to a string. */
    public String realDecompress(List<Integer> compressed) {
        // Build the dictionary.
        int dictSize = 256;
        Map<Integer,String> dictionary = new HashMap<Integer,String>();
        for (int i = 0; i < 256; i++)
            dictionary.put(i, "" + (char)i);

        String w = "" + (char)(int)compressed.remove(0);
        StringBuffer result = new StringBuffer(w);
        for (int k : compressed) {
            String entry;
            if (dictionary.containsKey(k))
                entry = dictionary.get(k);
            else if (k == dictSize)
                entry = w + w.charAt(0);
            else
                throw new IllegalArgumentException("Bad compressed k: " + k);

            result.append(entry);

            // Add w+entry[0] to the dictionary.
            dictionary.put(dictSize++, w + entry.charAt(0));

            w = entry;
        }
        return result.toString();
    }

    public int convert(byte b) {
        return new Byte(b).intValue() & 0xff;
    }

    public byte convert(int i) {
        if (i<0) {
            throw new BaseException("did not expect value < 0 " + i);
        }
        if (i>255) {
            throw new BaseException("did not expect value > 255 " + i);
        }
        byte b = new Integer(i).byteValue();
        return (byte) ((int) b & (0xff));
    }

}