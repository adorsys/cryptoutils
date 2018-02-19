package org.adorsys.encobject.filesystem;

import com.google.protobuf.ByteString;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.BlobMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.PageSet;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.document.DocumentMetaInfoData;
import org.adorsys.encobject.domain.document.FullDocumentData;
import org.adorsys.encobject.exceptions.StorageConnectionException;
import org.adorsys.encobject.filesystem.exceptions.CreateFolderException;
import org.adorsys.encobject.filesystem.exceptions.DeleteFileException;
import org.adorsys.encobject.filesystem.exceptions.FileIsFolderException;
import org.adorsys.encobject.filesystem.exceptions.FileNotFoundException;
import org.adorsys.encobject.filesystem.exceptions.FolderDeleteException;
import org.adorsys.encobject.filesystem.exceptions.FolderIsAFileException;
import org.adorsys.encobject.filesystem.exceptions.WriteBlobException;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 06.02.18 at 12:40.
 */
public class FileSystemExtendedStorageConnection implements ExtendedStoreConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(FileSystemExtendedStorageConnection.class);
    private final static String DEFAULT_BASE = "target/filesystemstorage";
    private final static String META_INFORMATION_SUFFIX = "._META-INFORMATION_";
    protected final BucketDirectory baseDir;

    public FileSystemExtendedStorageConnection() {
        this(DEFAULT_BASE);
    }

    public FileSystemExtendedStorageConnection(String basedir) {
        this.baseDir = new BucketDirectory(basedir);
//        createContainer("");
    }

    @Override
    public void createContainer(String container) {
        File file = getAsFile(baseDir.appendDirectory(container));
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
    public void deleteContainer(String container) {
        deleteContainer(new BucketDirectory(container));
    }

    public void deleteContainer(BucketDirectory container) {
        File file = getAsFile(baseDir.appendDirectory(container.getObjectHandle().getContainer()));
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
    public void putBlob(BucketPath bucketPath, byte[] bytes) {
        Payload payload = new FileSystemPayload(bytes, new FileSystemBlobMetaInfo());
        putBlob(bucketPath, payload);
    }

    @Override
    public boolean blobExists(BucketPath bucketPath) {
        File file = getAsFile(baseDir.append(bucketPath));
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
    public PageSet<? extends StorageMetadata> list(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        File file = getAsFile(baseDir.append(bucketDirectory));
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
            File containerPrefix = getAsFile(baseDir.appendDirectory(container));
            String knownPrefix = containerPrefix.getAbsolutePath() + BucketPath.BUCKET_SEPARATOR;
            if (listRecursiveFlag.equals(ListRecursiveFlag.FALSE)) {
                Collection<File> files = FileUtils.listFiles(file, null, listRecursiveFlag.equals(ListRecursiveFlag.TRUE));
                file2set(set, knownPrefix, files);
                String[] list = file.list(new DirectoryFilenameFilter());
                file.listFiles();
                for (String filename : list) {
                    LOGGER.debug("gefundenes directory ist " + filename);
                    filename = bucketDirectory.getObjectHandle().getName() + BucketPath.BUCKET_SEPARATOR + filename;
                    set.add(new FileSystemMetaData(filename + BucketPath.BUCKET_SEPARATOR));
                }
            } else {
                Collection<File> files = FileUtils.listFiles(file, null, listRecursiveFlag.equals(ListRecursiveFlag.TRUE));
                file2set(set, knownPrefix, files);
            }
            return set;
        } catch (Exception e) {
            throw new StorageConnectionException("" + file, e);
        }
    }


    @Override
    public void putBlob(BucketPath bucketPath, Payload payload) {
        byte[] document = payload.getData();

        BlobMetaInfo metaInfo = payload.getBlobMetaInfo();
        Map<String, String> map = new HashMap<>();
        for (String key : metaInfo.keySet()) {
            map.put(key, metaInfo.get(key));
        }
        LOGGER.debug("PUT metaInfo map size " + map.keySet().size());
        DocumentMetaInfoData.Builder metaInfoBuilder = DocumentMetaInfoData.newBuilder().putAllMap(map);
        FullDocumentData fullDocumentData = FullDocumentData.newBuilder().setDocument(ByteString.copyFrom(document)).setBlobMetaInfo(metaInfoBuilder).build();

        BucketPath metaInfoBucketPath = bucketPath.add(META_INFORMATION_SUFFIX);
        writeBytes(bucketPath, fullDocumentData.toByteArray());
        writeBytes(metaInfoBucketPath, metaInfoBuilder.build().toByteArray());
    }

    @Override
    public BlobMetaInfo getBlobMetaInfo(BucketPath bucketPath) {
        try {
            BucketPath metaInfoBucketPath = bucketPath.add(META_INFORMATION_SUFFIX);
            byte[] bytes = readBytes(metaInfoBucketPath);
            Map<String, String> metaInfo = DocumentMetaInfoData.parseFrom(bytes).getMapMap();
            LOGGER.debug("GET1 metaInfo map size " + metaInfo.keySet().size());
            BlobMetaInfo blobMetaInfo = new BlobMetaInfo();
            for (String key : metaInfo.keySet()) {
                blobMetaInfo.put(key, metaInfo.get(key));
            }
            return blobMetaInfo;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public Payload getBlob(BucketPath bucketPath) {
        try {
            byte[] bytes = readBytes(bucketPath);
            FullDocumentData fullDocumentData = FullDocumentData.parseFrom(bytes);
            byte[] document = fullDocumentData.getDocument().toByteArray();
            Map<String, String> metaInfo = fullDocumentData.getBlobMetaInfo().getMapMap();
            LOGGER.debug("GET2 metaInfo map size " + metaInfo.keySet().size());
            BlobMetaInfo blobMetaInfo = new BlobMetaInfo();
            for (String key : metaInfo.keySet()) {
                blobMetaInfo.put(key, metaInfo.get(key));
            }
            return new FileSystemPayload(document, blobMetaInfo);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void removeBlob(BucketPath bucketPath) {
        File file = getAsFile(baseDir.append(bucketPath));
        if (!file.exists()) {
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


    protected File getAsFile(BucketPath bucketPath) {
        return getAsFile(bucketPath.getObjectHandle());
    }

    protected File getAsFile(BucketDirectory bucketPath) {
        return getAsFile(bucketPath.getObjectHandle());
    }

    private File getAsFile(ObjectHandle objectHandle) {
        String container = objectHandle.getContainer();
        String name = objectHandle.getName();
        String fullpath = container + BucketPath.BUCKET_SEPARATOR + name;
        return new File(fullpath);
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

    private static void file2set(FileSystemPageSet<FileSystemMetaData> set, String knownPrefix, Collection<File> files) {
        for (File f : files) {
            String filename = f.getAbsolutePath();
            if (!filename.startsWith(knownPrefix)) {
                throw new BaseException("Programming Error. expected " + filename + " to start with " + knownPrefix);
            }
            if (!filename.endsWith(META_INFORMATION_SUFFIX)) {
                filename = filename.substring(knownPrefix.length());
                set.add(new FileSystemMetaData(filename));
            }
        }
    }

    private void writeBytes(BucketPath bucketPath, byte[] bytes) {
        File file = getAsFile(baseDir.append(bucketPath));
        try {
            FileUtils.writeByteArrayToFile(file, bytes);
        } catch (IOException e) {
            throw new WriteBlobException("can not write " + file, e);
        }

    }

    private byte[] readBytes(BucketPath bucketPath) {
        File file = getAsFile(baseDir.append(bucketPath));
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            throw new FileNotFoundException("can not read file " + file, e);
        }
    }
}
