package org.adorsys.encobject.filesystem;

import org.adorsys.encobject.types.connection.FilesystemBasedirectoryName;
import org.adorsys.encobject.types.properties.ConnectionPropertiesImpl;
import org.adorsys.encobject.types.properties.FilesystemConnectionProperties;

/**
 * Created by peter on 04.10.18.
 */
public class FilesystemConnectionPropertiesImpl extends ConnectionPropertiesImpl implements FilesystemConnectionProperties {
    private FilesystemBasedirectoryName filesystemBasedirectoryName = defaultBasedirectory;

    public FilesystemConnectionPropertiesImpl() {}

    public FilesystemConnectionPropertiesImpl(FilesystemConnectionProperties source) {
        super(source);
        filesystemBasedirectoryName = source.getFilesystemBasedirectoryName();
    }

    @Override
    public FilesystemBasedirectoryName getFilesystemBasedirectoryName() {
        return filesystemBasedirectoryName;
    }

    public void setFilesystemBasedirectoryName(FilesystemBasedirectoryName filesystemBasedirectoryName) {
        this.filesystemBasedirectoryName = filesystemBasedirectoryName;
    }
}
