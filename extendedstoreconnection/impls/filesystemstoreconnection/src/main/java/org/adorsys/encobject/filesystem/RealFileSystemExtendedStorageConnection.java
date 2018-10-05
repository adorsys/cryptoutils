package org.adorsys.encobject.filesystem;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.utils.Frame;
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
import org.adorsys.encobject.types.BucketPathEncryptionPassword;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.adorsys.encobject.types.connection.FilesystemBasedirectoryName;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by peter on 06.02.18 at 12:40.
 */
class RealFileSystemExtendedStorageConnection implements ExtendedStoreConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(RealFileSystemExtendedStorageConnection.class);
    protected final BucketDirectory baseDir;
    private ZipFileHelper zipFileHelper;
    private boolean absolutePath = false;

    public RealFileSystemExtendedStorageConnection(FilesystemBasedirectoryName basedir) {
        try {
            this.baseDir = new BucketDirectory(basedir.getValue());
            this.absolutePath = (basedir.getValue().startsWith(BucketPath.BUCKET_SEPARATOR));
            Frame frame = new Frame();
            frame.add("USE FILE SYSTEM");
            if (!absolutePath) {
                String currentDir = new File(".").getCanonicalPath();
                String absoluteDirectory = basedir.getValue();
                absoluteDirectory = currentDir + absoluteDirectory;
                frame.add("basedir     : " + basedir);
                frame.add("absolutedir : " + absoluteDirectory);
            } else {
                frame.add("absolutedir : " + basedir);
            }
            LOGGER.info(frame.toString());

            this.zipFileHelper = new ZipFileHelper(this.baseDir, absolutePath);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


    @Override
    public void createContainer(BucketDirectory bucketDirectory) {
        String containerOnly = bucketDirectory.getObjectHandle().getContainer();

        File file = BucketPathFileHelper.getAsFile(baseDir.appendDirectory(containerOnly), absolutePath);
        if (file.isDirectory()) {
            LOGGER.debug("directory already exists:" + file);
            return;
        }
        boolean success = file.mkdirs();
        if (!success) {
            throw new CreateFolderException("Can not create directory " + file);
        }
        LOGGER.debug("created folder " + file);
    }

    @Override
    public boolean containerExists(BucketDirectory bucketDirectory) {
        File file = BucketPathFileHelper.getAsFile(baseDir.appendDirectory(bucketDirectory.getObjectHandle().getContainer()), absolutePath);
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
    public void deleteContainer(BucketDirectory container) {
        File file = BucketPathFileHelper.getAsFile(baseDir.appendDirectory(container.getObjectHandle().getContainer()), absolutePath);
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
        File file = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath.add(ZipFileHelper.ZIP_SUFFIX)), absolutePath);
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
        File file = BucketPathFileHelper.getAsFile(baseDir.append(bucketDirectory), absolutePath);
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
    public List<BucketDirectory> listAllBuckets() {
        try {
            List<BucketDirectory> list = new ArrayList<>();
            String[] dirs = BucketPathFileHelper.getAsFile(baseDir, absolutePath).list(new DirectoryFilenameFilter());
            if (dirs == null) {
                return list;
            }
            Arrays.stream(dirs).forEach(dir -> list.add(new BucketDirectory(dir)));
            return list;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void putBlob(BucketPath bucketPath, Payload payload) {
        checkContainerExists(bucketPath);
        zipFileHelper.writeZip(bucketPath, new SimplePayloadImpl(payload));
    }

    @Override
    public Payload getBlob(BucketPath bucketPath) {
        checkContainerExists(bucketPath);
        return zipFileHelper.readZip(bucketPath, null);
    }

    @Override
    public Payload getBlob(BucketPath bucketPath, StorageMetadata storageMetadata) {
        checkContainerExists(bucketPath);
        return zipFileHelper.readZip(bucketPath, storageMetadata);
    }

    @Override
    public void putBlobStream(BucketPath bucketPath, PayloadStream payloadStream) {
        checkContainerExists(bucketPath);
        zipFileHelper.writeZipStream(bucketPath, new SimplePayloadStreamImpl(payloadStream));

    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath) {
        checkContainerExists(bucketPath);
        return zipFileHelper.readZipStream(bucketPath, null);
    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath, StorageMetadata storageMetadata) {
        checkContainerExists(bucketPath);
        return zipFileHelper.readZipStream(bucketPath, storageMetadata);
    }

    @Override
    public StorageMetadata getStorageMetadata(BucketPath bucketPath) {
        checkContainerExists(bucketPath);
        return zipFileHelper.readZipMetadataOnly(bucketPath);
    }

    @Override
    public void removeBlob(BucketPath bucketPath) {
        checkContainerExists(bucketPath);
        File file = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath).add(ZipFileHelper.ZIP_SUFFIX), absolutePath);
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
    public void removeBlobFolder(BucketDirectory bucketDirectory) {
        checkContainerExists(bucketDirectory);
        if (bucketDirectory.getObjectHandle().getName() == null) {
            throw new StorageConnectionException("not a valid bucket directory " + bucketDirectory);
        }
        File directory = BucketPathFileHelper.getAsFile(baseDir.append(bucketDirectory), absolutePath);
        LOGGER.debug("remove directory " + directory.getAbsolutePath());
        if (!directory.exists()) {
            return;
        }
        try {
            FileUtils.forceDelete(directory);
        } catch (IOException e) {
            throw new DeleteFileException("can not delete " + directory, e);
        }

    }

    /* ===========================================================================================================
     */

    private void checkContainerExists(BucketPath bucketPath) {
        if (!containerExists(bucketPath.getBucketDirectory())) {
            throw new BaseException("Container " + bucketPath.getObjectHandle().getContainer() + " does not exist");
        }
    }

    private void checkContainerExists(BucketDirectory bucketDirectory) {
        if (!containerExists(bucketDirectory)) {
            throw new BaseException("Container " + bucketDirectory.getObjectHandle().getContainer() + " does not exist");
        }
    }

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
        String knownPrefix = BucketPathFileHelper.getAsFile(baseDir.append(bucketDirectory), absolutePath).getAbsolutePath();

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
        String knownPrefix = BucketPathFileHelper.getAsFile(baseDir.append(bucketDirectory), absolutePath).getAbsolutePath();

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
        File file = BucketPathFileHelper.getAsFile(baseDir.append(bucketDirectory), absolutePath);
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
