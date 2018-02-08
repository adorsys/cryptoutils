package org.adorsys.encobject.filesystem;

import org.adorsys.encobject.domain.ContentInfoEntry;
import org.adorsys.encobject.domain.DocumentMetaInfo;

/**
 * Created by peter on 08.02.18 at 12:10.
 */
public class FileSystemDocumentMetaInfo extends DocumentMetaInfo {
    public FileSystemDocumentMetaInfo() {
        super();
    }

    public void putString(String key, String value) {
        super.put(key, new ContentInfoEntry("String", "1", value));
    }

    public String getString(String key) {
        return super.get(key).getValue();
    }
}
