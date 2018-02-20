package org.adorsys.encobject.filesystem;

import com.google.protobuf.ByteString;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.BucketPathUtil;
import org.adorsys.encobject.domain.Location;
import org.adorsys.encobject.domain.LocationScope;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.domain.document.FullDocumentData;
import org.adorsys.encobject.domain.document.LocationData;
import org.adorsys.encobject.domain.document.StorageMetadataData;
import org.adorsys.encobject.domain.document.UserMetaDataData;
import org.adorsys.encobject.exceptions.StorageConnectionException;
import org.adorsys.encobject.filesystem.exceptions.CreateFolderException;
import org.adorsys.encobject.filesystem.exceptions.DeleteFileException;
import org.adorsys.encobject.filesystem.exceptions.FileIsFolderException;
import org.adorsys.encobject.filesystem.exceptions.FileNotFoundException;
import org.adorsys.encobject.filesystem.exceptions.FolderDeleteException;
import org.adorsys.encobject.filesystem.exceptions.FolderIsAFileException;
import org.adorsys.encobject.filesystem.exceptions.WriteBlobException;
import org.adorsys.encobject.service.ExtendedStorageConnectionDirectoryContent;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.service.SimpleLocationImpl;
import org.adorsys.encobject.service.SimplePayloadImpl;
import org.adorsys.encobject.service.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
        Payload payload = new SimplePayloadImpl(bytes);
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
    public List<StorageMetadata> list(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        List<StorageMetadata> result = new ArrayList<>();
        File file = getAsFile(baseDir.append(bucketDirectory));
        if (!file.exists()) {
            return result;
        }
        if (!file.isDirectory()) {
            return result;
        }
        ExtendedStorageConnectionDirectoryContent content = listContent(bucketDirectory, listRecursiveFlag);
        addStorageMetaData(result, content);
        return result;
    }

    @Override
    public void putBlob(BucketPath bucketPath, Payload payload) {
        byte[] document = payload.getData();

        payload.getStorageMetadata().setType(StorageType.BLOB);
        payload.getStorageMetadata().setName(BucketPathUtil.getAsString(bucketPath));
        StorageMetadataData storageMetadataData = fromJavaToProtoBuf(payload.getStorageMetadata());
        FullDocumentData fullDocumentData = FullDocumentData.newBuilder().setDocument(ByteString.copyFrom(document)).setStorageMetadataData(storageMetadataData).build();

        BucketPath metaInfoBucketPath = bucketPath.add(META_INFORMATION_SUFFIX);
        writeBytes(bucketPath, fullDocumentData.toByteArray());
        writeBytes(metaInfoBucketPath, storageMetadataData.toByteArray());
    }

    @Override
    public StorageMetadata getStorageMetadata(BucketPath bucketPath) {
        try {
            BucketPath metaInfoBucketPath = bucketPath.add(META_INFORMATION_SUFFIX);
            byte[] bytes = readBytes(metaInfoBucketPath);
            StorageMetadataData storageMetadataData = StorageMetadataData.parseFrom(bytes);
            return fromProtoBufToJava(storageMetadataData);
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
            StorageMetadataData storageMetadataData = fullDocumentData.getStorageMetadataData();
            StorageMetadata storageMetadata = fromProtoBufToJava(storageMetadataData);
            return new SimplePayloadImpl(storageMetadata, document);
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
    public long countBlobs(BucketDirectory bucketDirectory, ListRecursiveFlag recursive) {
        return countBlobs(listContent(bucketDirectory, recursive), 0);
    }

    /* ===========================================================================================================
     */
    protected File getAsFile(BucketPath bucketPath) {
        return getAsFile(bucketPath.getObjectHandle());
    }

    protected File getAsFile(BucketDirectory bucketPath) {
        return getAsFile(bucketPath.getObjectHandle());
    }

    private int countBlobs(ExtendedStorageConnectionDirectoryContent content, int currentCounter) {
        currentCounter += content.getFiles().size();
        for (ExtendedStorageConnectionDirectoryContent subdir : content.getSubidrs()) {
            currentCounter += countBlobs(subdir, 0);
        }
        return currentCounter;
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
            try {
                return new File(dir.getCanonicalPath() + BucketPath.BUCKET_SEPARATOR + name).isDirectory();
            } catch (IOException e) {
                throw BaseExceptionHandler.handle(e);
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

    private StorageMetadataData fromJavaToProtoBuf(StorageMetadata javaMeta) {
        StorageMetadataData.Builder protoBufBuilder = StorageMetadataData.newBuilder();
        {
            String name = javaMeta.getName();
            if (name != null) {
                protoBufBuilder.setName(name);
            }
        }
        {
            StorageType storageType = javaMeta.getType();
            if (storageType != null) {
                protoBufBuilder.setType(storageType.toString());
            }
        }
        {
            String providerID = javaMeta.getProviderID();
            if (providerID != null) {
                protoBufBuilder.setProviderid(providerID);
            }
        }
        {
            Location location = javaMeta.getLocation();
            if (location != null) {
                protoBufBuilder.setLocationData(fromJavaToProtoBuf(location));
            }
        }
        {
            URI uri = javaMeta.getUri();
            if (uri != null) {
                protoBufBuilder.setUri(uri.toString());
            }
        }
        {
            Map<String, String> map = new HashMap<>();
            for (String key : javaMeta.getUserMetadata().keySet()) {
                map.put(key, javaMeta.getUserMetadata().get(key));
            }
            UserMetaDataData userMetadataData = UserMetaDataData.newBuilder().putAllMap(map).build();
            protoBufBuilder.setUserMetaDataData(userMetadataData);
        }
        {
            String etag = javaMeta.getETag();
            if (etag != null) {
                protoBufBuilder.setEtag(etag);
            }
        }
        {
            Date creationDate = javaMeta.getCreationDate();
            if (creationDate != null) {
                protoBufBuilder.setCreationDate(creationDate.getTime());
            }
        }
        {
            Date modifiedDate = javaMeta.getLastModified();
            if (modifiedDate != null) {
                protoBufBuilder.setModifiedDate(modifiedDate.getTime());
            }
        }
        protoBufBuilder.setSize(javaMeta.getSize());
        return protoBufBuilder.build();
    }

    private LocationData fromJavaToProtoBuf(Location javaLocation) {
        LocationData.Builder protoBufBuilder = LocationData.newBuilder();

        if (javaLocation.getScope() != null) {
            protoBufBuilder.setScope(javaLocation.getScope().toString());
        }
        if (javaLocation.getID() != null) {
            protoBufBuilder.setId(javaLocation.getID());
        }
        if (javaLocation.getDescription() != null) {
            protoBufBuilder.setDescription(javaLocation.getDescription());
        }
        {
            Iterator<String> iterator = javaLocation.getIso3166Codes().iterator();
            while (iterator.hasNext()) {
                protoBufBuilder.addIs3166Codes(iterator.next());
            }
        }
        Location parent = javaLocation.getParent();
        if (parent != null) {
            protoBufBuilder.setParent(fromJavaToProtoBuf(parent));
        }
        return protoBufBuilder.build();
    }

    private StorageMetadata fromProtoBufToJava(StorageMetadataData protoBufMeta) {
        SimpleStorageMetadataImpl javaMeta = new SimpleStorageMetadataImpl();
        javaMeta.setName(protoBufMeta.getName());
        {
            String typestring = protoBufMeta.getType();
            if (!StringUtils.isBlank(typestring)) {
                javaMeta.setType(StorageType.valueOf(typestring));
            }
        }
        javaMeta.setProviderID(protoBufMeta.getProviderid());
        {
            LocationData protoBufLocation = protoBufMeta.getLocationData();
            if (protoBufLocation != null) {
                Location javaLocation = fromProtoBufToJava(protoBufLocation);
                javaMeta.setLocation(javaLocation);
            }
        }
        {
            String uristring = protoBufMeta.getUri();
            if (!StringUtils.isBlank(uristring)) {
                javaMeta.setUri(URI.create(uristring));
            }
        }
        {
            for (String key : protoBufMeta.getUserMetaDataData().getMapMap().keySet()) {
                javaMeta.getUserMetadata().put(key, protoBufMeta.getUserMetaDataData().getMapMap().get(key));
            }
        }
        javaMeta.setETag(protoBufMeta.getEtag());
        {
            Long creationDateLong = protoBufMeta.getCreationDate();
            if (creationDateLong != null) {
                javaMeta.setCreationDate(new Date(creationDateLong));
            }
        }
        {
            Long modifiedDate = protoBufMeta.getModifiedDate();
            if (modifiedDate != null) {
                javaMeta.setLastModified(new Date(modifiedDate));
            }
        }
        javaMeta.setSize(protoBufMeta.getSize());
        return javaMeta;
    }

    private Location fromProtoBufToJava(LocationData protoBufLocation) {
        SimpleLocationImpl javaLocation = new SimpleLocationImpl();

        if (!StringUtils.isBlank(protoBufLocation.getScope())) {
            javaLocation.setScope(LocationScope.valueOf(protoBufLocation.getScope()));
        }
        if (!StringUtils.isBlank(protoBufLocation.getId())) {
            javaLocation.setId(protoBufLocation.getId());
        }
        if (!StringUtils.isBlank(protoBufLocation.getDescription())) {
            javaLocation.setDescription(protoBufLocation.getDescription());
        }
        {
            Iterator<String> protoBufIsoIterator = protoBufLocation.getIs3166CodesList().iterator();
            while (protoBufIsoIterator.hasNext()) {
                javaLocation.getIso3166Codes().add(protoBufIsoIterator.next());
            }
        }
        {
            if (protoBufLocation.hasParent()) {
                javaLocation.setParent(fromProtoBufToJava(protoBufLocation.getParent()));
            }
        }
        return javaLocation;
    }

    private void files2content(ExtendedStorageConnectionDirectoryContent content, BucketDirectory bucketDirectory, Collection<File> files) {
        String knownPrefix = getAsFile(baseDir.append(bucketDirectory)).getAbsolutePath();

        for (File f : files) {
            if (!f.getName().endsWith(META_INFORMATION_SUFFIX)) {
                content.getFiles().add(bucketDirectory.appendName(f.getName()));
            }
        }
    }

    private void dirs2content(ExtendedStorageConnectionDirectoryContent content, BucketDirectory bucketDirectory, String[] dirs) {
        String knownPrefix = getAsFile(baseDir.append(bucketDirectory)).getAbsolutePath();

        for (String dir : dirs) {
            content.getSubidrs().add(new ExtendedStorageConnectionDirectoryContent(bucketDirectory.appendDirectory(dir)));
        }
    }

    private void addFilesOnly(List<BucketPath> result, ExtendedStorageConnectionDirectoryContent content) {
        result.addAll(content.getFiles());
        for (ExtendedStorageConnectionDirectoryContent subContent : content.getSubidrs()) {
            addFilesOnly(result, subContent);
        }
    }

    private void listRecursive(ExtendedStorageConnectionDirectoryContent content) {
        ExtendedStorageConnectionDirectoryContent current = listContent(content.getDirectory(), ListRecursiveFlag.FALSE);

        List<ExtendedStorageConnectionDirectoryContent> newSubdirs = new ArrayList<>();
        for (ExtendedStorageConnectionDirectoryContent subdir : content.getSubidrs()) {
            ExtendedStorageConnectionDirectoryContent newSubdir = listContent(subdir.getDirectory(), ListRecursiveFlag.FALSE);
            listRecursive(newSubdir);
            newSubdirs.add(newSubdir);
        }
        content.getSubidrs().clear();
        content.getSubidrs().addAll(newSubdirs);
    }

    private ExtendedStorageConnectionDirectoryContent listContent(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        File file = getAsFile(baseDir.append(bucketDirectory));
        try {
            ExtendedStorageConnectionDirectoryContent content = new ExtendedStorageConnectionDirectoryContent(bucketDirectory);
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
        ExtendedStorageConnectionDirectoryContent content = listContent(bucketDirectory, listRecursiveFlag);
        addFilesOnly(result, content);
        return result;
    }

    private void addStorageMetaData(List<StorageMetadata> result, ExtendedStorageConnectionDirectoryContent content) {
        result.add(createStorageMetadataForDirectory(content));
        for (BucketPath file : content.getFiles()) {
            result.add(getStorageMetadata(file));
        }
        for (ExtendedStorageConnectionDirectoryContent dir : content.getSubidrs()) {
            addStorageMetaData(result, dir);
        }
    }

    private StorageMetadata createStorageMetadataForDirectory(ExtendedStorageConnectionDirectoryContent content) {
        StorageMetadata storageMetadata = new SimpleStorageMetadataImpl();
        storageMetadata.setType(StorageType.FOLDER);
        storageMetadata.setSize(new Long(content.getFiles().size() + content.getSubidrs().size()));

        storageMetadata.setName(BucketPathUtil.getAsString(content.getDirectory()));
        return storageMetadata;
    }


}
