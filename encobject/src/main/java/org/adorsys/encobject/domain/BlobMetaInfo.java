package org.adorsys.encobject.domain;

import org.adorsys.cryptoutils.exceptions.BaseException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by peter on 08.02.18 at 10:26.
 */
public class BlobMetaInfo {
    private Map<String, String> map;
    public BlobMetaInfo() {
        map = new HashMap<>();
    }

    /**
     * Wirft eine Exception, wenn der Wert nicht gefunden werden kann
     */
    public String get(String key) {
        String value = map.get(key);
        if (value == null) {
            throw new BaseException("Key " + key + " not found in BlobMetaInfo. Known Keys are " + map.keySet());
        }
        return value;
    }

    /**
     * Gibt null zurück, wenn der Wert nicht gefunden wird
     */
    public String find(String key) {
        return map.get(key);
    }

    public void put(String key, String value) {
        map.put(key, value);
    }

    public Set<String> keySet() {
        return map.keySet();
    }
}
