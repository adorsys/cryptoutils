package org.adorsys.encobject.filesystem;

import org.adorsys.encobject.types.connection.FilesystemBasedirectoryName;
import org.adorsys.encobject.types.properties.FilesystemConnectionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 13.04.18 at 19:19.
 */
public class FileSystemParamParser {
    private final static Logger LOGGER = LoggerFactory.getLogger(FileSystemParamParser.class);

    public static FilesystemConnectionProperties getProperties(String params) {
        LOGGER.debug("parse:" + params);
        FilesystemConnectionPropertiesImpl properties = new FilesystemConnectionPropertiesImpl();
        if (params.length() > 0) {
            properties.setFilesystemBasedirectoryName(new FilesystemBasedirectoryName(params));
        }
        return properties;
    }
}
