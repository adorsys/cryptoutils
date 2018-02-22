package org.adorsys.encobject.utils;

import org.adorsys.encobject.filesystem.BucketPathFileHelper;
import org.adorsys.encobject.filesystem.FileSystemExtendedStorageConnection;
import org.adorsys.encobject.filesystem.ZipFileHelper;

import java.io.File;

public class TestFileSystemExtendedStorageConnection extends FileSystemExtendedStorageConnection {

	public boolean existsOnFs(String container, String name){
		File file = BucketPathFileHelper.getAsFile(baseDir.appendDirectory(container).appendName(name).add(ZipFileHelper.ZIP_SUFFIX));
		return file.exists();
	}
}
