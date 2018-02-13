package org.adorsys.encobject.utils;

import org.adorsys.encobject.filesystem.FileSystemExtendedStorageConnection;

import java.io.File;

public class TestFileSystemExtendedStorageConnection extends FileSystemExtendedStorageConnection {
	
	public boolean existsOnFs(String container, String name){
		File file = getAsFile(baseDir.append(container).append(name));
		return file.exists();
	}
}
