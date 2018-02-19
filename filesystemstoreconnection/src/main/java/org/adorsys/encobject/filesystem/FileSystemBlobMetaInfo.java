package org.adorsys.encobject.filesystem;

import org.adorsys.encobject.domain.BlobMetaInfo;

/**
 * Created by peter on 08.02.18 at 12:10.
 */
public class FileSystemBlobMetaInfo extends BlobMetaInfo {
    public FileSystemBlobMetaInfo() {
        super();
    }

    public void putString(String key, String value) {
        super.put(key,value);
    }

    public String get(String key) {
        return super.get(key);
    }
}
