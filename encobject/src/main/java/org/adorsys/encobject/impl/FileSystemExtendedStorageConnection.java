package org.adorsys.encobject.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.ContentInfoEntry;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.PageSet;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.exceptions.StorageConnectionException;
import org.adorsys.encobject.exceptions.filesystemstorage.CreateFolderException;
import org.adorsys.encobject.exceptions.filesystemstorage.DeleteFileException;
import org.adorsys.encobject.exceptions.filesystemstorage.FileIsFolderException;
import org.adorsys.encobject.exceptions.filesystemstorage.FileNotFoundException;
import org.adorsys.encobject.exceptions.filesystemstorage.FolderDeleteException;
import org.adorsys.encobject.exceptions.filesystemstorage.FolderIsAFileException;
import org.adorsys.encobject.exceptions.filesystemstorage.WriteBlobException;
import org.adorsys.encobject.service.ContainerExistsException;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.service.ObjectNotFoundException;
import org.adorsys.encobject.service.UnknownContainerException;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.EmptyFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Created by peter on 06.02.18 at 12:40.
 */
public class FileSystemExtendedStorageConnection implements ExtendedStoreConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(FileSystemExtendedStorageConnection.class);
    private final static String DEFAULT_BASE = "target/filesystemstorage";
    private final BucketDirectory baseDir;

    public FileSystemExtendedStorageConnection() {
        baseDir = new BucketDirectory(DEFAULT_BASE);
        try {
            createContainer("");
        } catch (ContainerExistsException e) {
            throw new BaseException(e);
        }
    }

    @Override
    public void createContainer(String container) throws ContainerExistsException {
        File file = getAsFile(baseDir.append(container));
        if (file.isDirectory()) {
            LOGGER.debug("directory already exists:" + file);
            return;
        }
        boolean success = file.mkdirs();
        if (!success) {
            throw new CreateFolderException("Can not create directory " + file);
        }
        LOGGER.info("created folder " + file);
    }

    @Override
    public boolean containerExists(String container) {
        return containerExists(new BucketDirectory(container));
    }

    public boolean containerExists(BucketDirectory bucketDirectory) {
        File file = getAsFile(baseDir.append(bucketDirectory));
        if (file.isDirectory()) {
            LOGGER.debug("directory exists:" + file);
            return true;
        }
        if (file.isFile()) {
            throw new FolderIsAFileException("folder is a file " + file);
        }
        LOGGER.debug("directory does not exists" + file);
        return false;
    }


    @Override
    public void deleteContainer(String container) throws UnknownContainerException {
        deleteContainer(new BucketDirectory(container));
    }

    public void deleteContainer(BucketDirectory container) throws UnknownContainerException {
        File file = getAsFile(baseDir.append(container.getObjectHandle().getContainer()));
        if (!containerExists(container)) {
            LOGGER.debug("directory does not exist. so nothing to delete:" + file);
            return;
        }
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            throw new FolderDeleteException("can not delete " + file, e);
        }
    }

    @Override
    public void putBlob(ObjectHandle handle, byte[] bytes) throws UnknownContainerException {
        File file = getAsFile(baseDir.append(asBucketPath(handle)));
        LOGGER.debug("putBlob " + file);
        try {
            FileUtils.writeByteArrayToFile(file, bytes);
        } catch (IOException e) {
            throw new WriteBlobException("can not write " + file, e);
        }
    }

    @Override
    public byte[] getBlob(ObjectHandle handle) throws UnknownContainerException, ObjectNotFoundException {
        File file = getAsFile(baseDir.append(asBucketPath(handle)));
        LOGGER.debug("getBlob " + file);
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            throw new FileNotFoundException("can not read file " + file, e);
        }
    }

    @Override
    public boolean blobExists(ObjectHandle location) {
        File file = getAsFile(baseDir.append(asBucketPath(location)));
        LOGGER.debug("blobExists " + file);
        if (file.isDirectory()) {
            throw new FileIsFolderException("file " + file);
        }
        if (file.isFile()) {
            LOGGER.debug("file does exists" + file);
            return true;
        }
        LOGGER.debug("file does not exists" + file);
        return false;
    }

    @Override
    public PageSet<? extends StorageMetadata> list(BucketPath bucketPath, ListRecursiveFlag listRecursiveFlag) {
        return list(new BucketDirectory(bucketPath), listRecursiveFlag);
    }


    public PageSet<? extends StorageMetadata> list(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        File file = getAsFile(new BucketDirectory(baseDir.append(bucketDirectory)));
        LOGGER.debug("List directory " + file);
        try {
            FileSystemPageSet<FileSystemMetaData> set = new FileSystemPageSet<>();
            if (file.isFile()) {
                return set;
            }
            if (!file.isDirectory()) {
                return set;
            }
            String container = bucketDirectory.getObjectHandle().getContainer();
            File containerPrefix = getAsFile(new BucketDirectory(baseDir.append(container)));
            String knownPrefix = containerPrefix.getAbsolutePath() + BucketPath.BUCKET_SEPARATOR;
            if (listRecursiveFlag.equals(ListRecursiveFlag.FALSE)) {
                Collection<File> files = FileUtils.listFiles(file, null, listRecursiveFlag.equals(ListRecursiveFlag.TRUE));
                for (File f : files) {
                    String filename = f.getAbsolutePath();
                    if (!filename.startsWith(knownPrefix)) {
                        throw new BaseException("Programming Error. expected " + filename + " to start with " + knownPrefix);
                    }
                    filename = filename.substring(knownPrefix.length());
                    set.add(new FileSystemMetaData(filename));
                }
                String[] list = file.list(new DirectoryFilenameFilter());
                file.listFiles();
                for (String filename : list) {
                    LOGGER.debug("gefundenes directory ist " + filename);
                    filename = bucketDirectory.getObjectHandle().getName() + BucketPath.BUCKET_SEPARATOR + filename;
                    set.add(new FileSystemMetaData(filename + BucketPath.BUCKET_SEPARATOR));
                }
            } else {
                Collection<File> files = FileUtils.listFiles(file, null, listRecursiveFlag.equals(ListRecursiveFlag.TRUE));
                for (File f : files) {
                    String filename = f.getAbsolutePath();
                    if (!filename.startsWith(knownPrefix)) {
                        throw new BaseException("Programming Error. expected " + filename + " to start with " + knownPrefix);
                    }
                    filename = filename.substring(knownPrefix.length());
                    set.add(new FileSystemMetaData(filename));
                }
            }
            return set;
        } catch (Exception e) {
            throw new StorageConnectionException("" + file, e);
        }
    }

    @Override
    public String putBlob(BucketPath bucketPath, Payload payload) {
        throw new BaseException("NYI");
    }

    @Override
    public Map<String, ContentInfoEntry> blobMetadata(BucketPath bucketPath) {
        throw new BaseException("NYI");
    }

    @Override
    public Payload getBlob(BucketPath bucketPath) {
        throw new BaseException("NYI");
    }

    @Override
    public void removeBlob(BucketPath bucketPath) {
        File file = getAsFile(baseDir.append(bucketPath));
        if (! file.exists()) {
            return;
        }
        try {
            FileUtils.forceDelete(file);
        } catch (IOException e) {
            throw new DeleteFileException("can not delete " + file, e);
        }
    }

    @Override
    public void removeBlobs(Iterable<BucketPath> bucketPaths) {
        for (BucketPath bp : bucketPaths) {
            removeBlob(bp);
        }
    }

    @Override
    public long countBlobs(BucketPath bucketPath, ListRecursiveFlag recursive) {
        File file = getAsFile(baseDir.append(bucketPath));
        FileSystemPageSet<FileSystemMetaData> set = new FileSystemPageSet<>();

        return FileUtils.listFiles(file, null, recursive.equals(ListRecursiveFlag.TRUE)).size();
    }


    private File getAsFile(BucketPath bucketPath) {
        ObjectHandle objectHandle = bucketPath.getObjectHandle();
        String container = objectHandle.getContainer();
        String name = objectHandle.getName();
        String fullpath = container + BucketPath.BUCKET_SEPARATOR + name;
        return new File(fullpath);
    }

    private BucketPath asBucketPath(ObjectHandle objectHandle) {
        return new BucketPath(objectHandle.getContainer(), objectHandle.getName());
    }

    private final static class DirectoryFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            LOGGER.debug("have to accept " + dir + ", " + name);
            try {
                return new File(dir.getCanonicalPath() + BucketPath.BUCKET_SEPARATOR + name).isDirectory();
            } catch (IOException e) {
                throw BaseExceptionHandler.handle(e);
            }
        }
    }
}
