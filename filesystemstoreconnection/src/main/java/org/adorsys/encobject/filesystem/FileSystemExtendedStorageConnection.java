package org.adorsys.encobject.filesystem;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.exceptions.NYIException;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.BucketPathUtil;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.exceptions.StorageConnectionException;
import org.adorsys.encobject.filesystem.exceptions.CreateFolderException;
import org.adorsys.encobject.filesystem.exceptions.DeleteFileException;
import org.adorsys.encobject.filesystem.exceptions.FileIsFolderException;
import org.adorsys.encobject.filesystem.exceptions.FolderDeleteException;
import org.adorsys.encobject.filesystem.exceptions.FolderIsAFileException;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by peter on 06.02.18 at 12:40.
 */
public class FileSystemExtendedStorageConnection implements ExtendedStoreConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(FileSystemExtendedStorageConnection.class);
    private final static String DEFAULT_BASE = "target/filesystemstorage";
    protected final BucketDirectory baseDir;
    private ZipFileHelper zipFileHelper;

    public FileSystemExtendedStorageConnection() {
        this(DEFAULT_BASE);
    }

    public FileSystemExtendedStorageConnection(String basedir) {
        this.baseDir = new BucketDirectory(basedir);
        this.zipFileHelper = new ZipFileHelper(this.baseDir);
    }

    @Override
    public void createContainer(String container) {
        File file = BucketPathFileHelper.getAsFile(baseDir.appendDirectory(container));
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
        File file = BucketPathFileHelper.getAsFile(baseDir.append(bucketDirectory));
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
        File file = BucketPathFileHelper.getAsFile(baseDir.appendDirectory(container.getObjectHandle().getContainer()));
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
        Payload payload = new SimplePayloadImpl(bytes);
        putBlob(bucketPath, payload);
    }

    @Override
    public boolean blobExists(BucketPath bucketPath) {
        File file = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath.add(ZipFileHelper.ZIP_SUFFIX)));
        if (file.isDirectory()) {
            throw new FileIsFolderException("file " + file);
        }
        if (file.isFile()) {
            LOGGER.debug("file does exist " + file);
            return true;
        }
        LOGGER.debug("file does not exist " + file);
        return false;
    }


    @Override
    public List<StorageMetadata> list(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        List<StorageMetadata> result = new ArrayList<>();
        File file = BucketPathFileHelper.getAsFile(baseDir.append(bucketDirectory));
        if (!file.exists()) {
            return result;
        }
        if (!file.isDirectory()) {
            return result;
        }
        DirectoryContent content = listContent(bucketDirectory, listRecursiveFlag);
        addStorageMetaData(result, content);
        return result;
    }

    @Override
    public void putBlob(BucketPath bucketPath, Payload payload) {
        zipFileHelper.writeZip(bucketPath, new SimplePayloadImpl(payload));
    }

    @Override
    public void putBlobStream(BucketPath bucketPath, PayloadStream payloadStream) {
        zipFileHelper.writeZip(bucketPath, new SimplePayloadStreamImpl(payloadStream));

    }

    @Override
    public StorageMetadata getStorageMetadata(BucketPath bucketPath) {
        return zipFileHelper.readZipMetadataOnly(bucketPath);
    }

    @Override
    public Payload getBlob(BucketPath bucketPath) {
        return zipFileHelper.readZip(bucketPath);
    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath) {
        throw new NYIException();
    }

    @Override
    public void removeBlob(BucketPath bucketPath) {
        File file = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath).add(ZipFileHelper.ZIP_SUFFIX));
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
    public long countBlobs(BucketDirectory bucketDirectory, ListRecursiveFlag recursive) {
        return countBlobs(listContent(bucketDirectory, recursive), 0);
    }

    /* ===========================================================================================================
     */

    private int countBlobs(DirectoryContent content, int currentCounter) {
        currentCounter += content.getFiles().size();
        for (DirectoryContent subdir : content.getSubidrs()) {
            currentCounter += countBlobs(subdir, 0);
        }
        return currentCounter;
    }

    private final static class DirectoryFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            try {
                return new File(dir.getCanonicalPath() + BucketPath.BUCKET_SEPARATOR + name).isDirectory();
            } catch (IOException e) {
                throw BaseExceptionHandler.handle(e);
            }
        }
    }


    
    private void files2content(DirectoryContent content, BucketDirectory bucketDirectory, Collection<File> files) {
        String knownPrefix = BucketPathFileHelper.getAsFile(baseDir.append(bucketDirectory)).getAbsolutePath();

        for (File f : files) {
            String name = f.getName();
            if (!name.endsWith(ZipFileHelper.ZIP_SUFFIX)) {
                LOGGER.debug("ignore file " + bucketDirectory.appendName(name));
            } else {
                String origName = name.substring(0, name.length() - ZipFileHelper.ZIP_SUFFIX.length());
                content.getFiles().add(bucketDirectory.appendName(origName));
            }
        }
    }


    private void dirs2content(DirectoryContent content, BucketDirectory bucketDirectory, String[] dirs) {
        String knownPrefix = BucketPathFileHelper.getAsFile(baseDir.append(bucketDirectory)).getAbsolutePath();

        for (String dir : dirs) {
            content.getSubidrs().add(new DirectoryContent(bucketDirectory.appendDirectory(dir)));
        }
    }

    private void addFilesOnly(List<BucketPath> result, DirectoryContent content) {
        result.addAll(content.getFiles());
        for (DirectoryContent subContent : content.getSubidrs()) {
            addFilesOnly(result, subContent);
        }
    }

    private void listRecursive(DirectoryContent content) {
        DirectoryContent current = listContent(content.getDirectory(), ListRecursiveFlag.FALSE);

        List<DirectoryContent> newSubdirs = new ArrayList<>();
        for (DirectoryContent subdir : content.getSubidrs()) {
            DirectoryContent newSubdir = listContent(subdir.getDirectory(), ListRecursiveFlag.FALSE);
            listRecursive(newSubdir);
            newSubdirs.add(newSubdir);
        }
        content.getSubidrs().clear();
        content.getSubidrs().addAll(newSubdirs);
    }

    private DirectoryContent listContent(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        File file = BucketPathFileHelper.getAsFile(baseDir.append(bucketDirectory));
        try {
            DirectoryContent content = new DirectoryContent(bucketDirectory);
            if (file.isFile()) {
                return content;
            }
            if (!file.isDirectory()) {
                return content;
            }
            if (listRecursiveFlag.equals(ListRecursiveFlag.FALSE)) {
                Collection<File> files = FileUtils.listFiles(file, null, listRecursiveFlag.equals(ListRecursiveFlag.TRUE));
                files2content(content, bucketDirectory, files);
                String[] list = file.list(new DirectoryFilenameFilter());
                dirs2content(content, bucketDirectory, list);
                return content;
            }
            content = listContent(bucketDirectory, ListRecursiveFlag.FALSE);
            listRecursive(content);
            return content;
        } catch (Exception e) {
            throw new StorageConnectionException("" + file, e);
        }
    }

    private List<BucketPath> listFlat(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        List<BucketPath> result = new ArrayList<>();
        DirectoryContent content = listContent(bucketDirectory, listRecursiveFlag);
        addFilesOnly(result, content);
        return result;
    }

    private void addStorageMetaData(List<StorageMetadata> result, DirectoryContent content) {
        result.add(createStorageMetadataForDirectory(content));
        for (BucketPath file : content.getFiles()) {
            result.add(getStorageMetadata(file));
        }
        for (DirectoryContent dir : content.getSubidrs()) {
            addStorageMetaData(result, dir);
        }
    }

    private StorageMetadata createStorageMetadataForDirectory(DirectoryContent content) {
        SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
        storageMetadata.setType(StorageType.FOLDER);
        storageMetadata.setSize(new Long(content.getFiles().size() + content.getSubidrs().size()));

        storageMetadata.setName(BucketPathUtil.getAsString(content.getDirectory()));
        return storageMetadata;
    }


}
