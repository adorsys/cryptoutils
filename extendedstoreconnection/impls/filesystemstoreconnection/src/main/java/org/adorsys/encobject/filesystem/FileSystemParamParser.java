package org.adorsys.encobject.filesystem;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;

/**
 * Created by peter on 13.04.18 at 19:19.
 */
public class FileSystemParamParser {
    private final static Logger LOGGER = LoggerFactory.getLogger(FileSystemParamParser.class);
    private String filesystembase = "target/filesystemstorage";

    public FileSystemParamParser(String params) {
        LOGGER.debug("parse:" + params);
        if (params.length() > 0) {
            filesystembase = params;
        }
    }

    public String getFilesystembase() {
        return filesystembase;
    }
}
