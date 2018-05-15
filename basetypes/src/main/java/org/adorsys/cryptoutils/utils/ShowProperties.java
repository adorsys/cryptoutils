package org.adorsys.cryptoutils.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by peter on 19.04.18 at 11:17.
 */
public class ShowProperties {
    private final static Logger LOGGER = LoggerFactory.getLogger(ShowProperties.class);
    public static void log() {
        List<String> keys = new ArrayList<>();
        int max = 0;
        for (Object keyo : System.getProperties().keySet()) {
            String key = keyo.toString();
            keys.add(key);
            if (key.length() > max)
                max = key.length();
        }
        Collections.sort(keys);
        for (String key : keys) {
            String value = System.getProperty(key);
            if (key.endsWith(".path") || key.endsWith("java.ext.dirs")) {
                String ps = System.getProperty("path.separator");
                StringTokenizer st = new StringTokenizer(value, ps);
                if (st.hasMoreElements()) {
                    LOGGER.debug(fill(key, ".", max) + " : " + st.nextToken());
                }
                while (st.hasMoreElements()) {
                    LOGGER.debug(fill(" ", " ", max) + " : " + st.nextToken());
                }
            } else {
                LOGGER.debug(fill(key, ".", max) + " : " + value);
            }
        };
    }

    private static String fill(String s, String achar, int max) {
        while (s.length() < max) {
            s = s + achar;
        }
        return s;
    }

}
