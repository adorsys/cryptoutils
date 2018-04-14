package org.adorsys.encobject.filesystem;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;

import java.util.StringTokenizer;

/**
 * Created by peter on 13.04.18 at 19:19.
 */
public class FileSystemParamParser {
    private String filesystembase = "target/filesystemstorage";

    public FileSystemParamParser(String params) {
        if (params.length() > 0) {
            filesystembase = params;
        }
    }

    public String getFilesystembase() {
        return filesystembase;
    }
}