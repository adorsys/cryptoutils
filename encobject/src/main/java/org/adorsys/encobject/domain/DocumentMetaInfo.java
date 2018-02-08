package org.adorsys.encobject.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by peter on 08.02.18 at 10:26.
 */
public class DocumentMetaInfo {
    private Map<String, ContentInfoEntry> map;
    public DocumentMetaInfo() {
        map = new HashMap<>();
    }

    public ContentInfoEntry get(String key) {
        return map.get(key);
    }

    public void put(String key, ContentInfoEntry value) {
        map.put(key, value);
    }

    public Set<String> keySet() {
        return map.keySet();
    }
}
