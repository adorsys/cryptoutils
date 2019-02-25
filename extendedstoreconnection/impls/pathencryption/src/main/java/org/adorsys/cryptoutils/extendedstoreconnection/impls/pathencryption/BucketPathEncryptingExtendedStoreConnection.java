package org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.utils.Frame;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.BucketPathUtil;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.exceptions.BucketRestrictionException;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.BucketPathEncryptionPassword;
import org.adorsys.encobject.types.ExtendedStoreConnectionType;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.adorsys.encobject.types.properties.BucketPathEncryptionFilenameOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 26.09.18.
 */
public class BucketPathEncryptingExtendedStoreConnection implements ExtendedStoreConnection {
    // those two limits are not random. For Ceph and Mongo a path part longer than this
    // can break the database

    // for ceph nano total length must not exceed 212
    // Ceph, Mino, Amazon, Filesystem
    private final static MaxLengthInfo AMAZONS3_MAX_LENGTH = new MaxLengthInfo(175, -1);
    // Mongo
    private final static MaxLengthInfo MONGO__MAX_LENGTH = new MaxLengthInfo(88, -1);
    private MaxLengthInfo maxLengthInfo;

    private final static Logger LOGGER = LoggerFactory.getLogger(BucketPathEncryptingExtendedStoreConnection.class);
    protected ExtendedStoreConnection extendedStoreConnection;
    BucketPathEncryption bucketPathEncryption;
    BucketPathEncryptionPassword bucketPathEncryptionPassword;
    BucketPathEncryptionFilenameOnly bucketPathEncryptionFilenameOnly;
    boolean active = bucketPathEncryptionPassword != null;

    public BucketPathEncryptingExtendedStoreConnection(ExtendedStoreConnection extendedStoreConnection,
                                                       BucketPathEncryptionPassword bucketPathEncryptionPassword,
                                                       BucketPathEncryptionFilenameOnly bucketPathEncryptionFilenameOnly) {
        this.extendedStoreConnection = extendedStoreConnection;
        this.bucketPathEncryption = new BucketPathEncryption();
        this.bucketPathEncryptionPassword = bucketPathEncryptionPassword;
        this.active = bucketPathEncryptionPassword != null;
        this.maxLengthInfo = getMaxLengthTupel(this.extendedStoreConnection);
        this.bucketPathEncryptionFilenameOnly = bucketPathEncryptionFilenameOnly;
        Frame frame = new Frame();
        if (extendedStoreConnection.getType().equals(ExtendedStoreConnectionType.MONGO)) {
            if (active) {
                frame.add("WARNING WARNING WARNING");
                frame.add("MONGO DB FILE NAMES CAN NOT BE ENCRYPTED DUE TO LENGTH RESTRICTION");
            }
            active = false;
            this.bucketPathEncryptionPassword = null;
        }
        if (active) {
            frame.add(bucketPathEncryptionPassword.toString());
            frame.add("BucketPathEncryptionFilenameOnly:" + bucketPathEncryptionFilenameOnly.toString());
        } else {
            frame.add("Filenames will not be encrypted");
        }
        LOGGER.info(frame.toString());

        if (LOGGER.isTraceEnabled()) {
            new BaseException("JUST A STACK, TO SEE WHERE THE CONNECTION IS CREATED");
        }
    }

    @Override
    public void putBlob(BucketPath bucketPath, Payload payload) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("putBlob(" + bucketPath + ", payload)");
        }
        extendedStoreConnection.putBlob(e(bucketPath), payload);
    }

    @Override
    public Payload getBlob(BucketPath bucketPath) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getBlob(" + bucketPath + ")");
        }
        return d(extendedStoreConnection.getBlob(e(bucketPath)));
    }

    @Override
    public Payload getBlob(BucketPath bucketPath, StorageMetadata storageMetadata) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getBlob(" + bucketPath + " ,storageMetadata)");
        }
        return d(extendedStoreConnection.getBlob(e(bucketPath), e(storageMetadata)));
    }

    @Override
    public void putBlobStream(BucketPath bucketPath, PayloadStream payloadStream) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("putBlobStream(" + bucketPath + " ,payloadStream)");
        }
        extendedStoreConnection.putBlobStream(e(bucketPath), payloadStream);
    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getBlobStream(" + bucketPath + ")");
        }
        return d(extendedStoreConnection.getBlobStream(e(bucketPath)));
    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath, StorageMetadata storageMetadata) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getBlobStream(" + bucketPath + " ,storageMetadata)");
        }
        return d(extendedStoreConnection.getBlobStream(e(bucketPath), e(storageMetadata)));
    }

    @Override
    public void putBlob(BucketPath bucketPath, byte[] bytes) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("putBlob(" + bucketPath + " ,byte[])");
        }
        extendedStoreConnection.putBlob(e(bucketPath), bytes);
    }

    @Override
    public StorageMetadata getStorageMetadata(BucketPath bucketPath) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getStorageMetadata(" + bucketPath + ")");
        }
        return d(extendedStoreConnection.getStorageMetadata(e(bucketPath)));
    }

    @Override
    public boolean blobExists(BucketPath bucketPath) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("blobExists(" + bucketPath + ")");
        }
        return extendedStoreConnection.blobExists(e(bucketPath));
    }

    @Override
    public void removeBlob(BucketPath bucketPath) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("removeBlob(" + bucketPath + ")");
        }
        extendedStoreConnection.removeBlob(e(bucketPath));
    }

    @Override
    public void removeBlobFolder(BucketDirectory bucketDirectory) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("removeBlobFolder(" + bucketDirectory + ")");
        }
        extendedStoreConnection.removeBlobFolder(e(bucketDirectory));
    }

    @Override
    public void createContainer(BucketDirectory bucketDirectory) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("createContainer(" + bucketDirectory + ")");
        }
        extendedStoreConnection.createContainer(e(bucketDirectory));
    }

    @Override
    public boolean containerExists(BucketDirectory bucketDirectory) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("containerExists(" + bucketDirectory + ")");
        }
        return extendedStoreConnection.containerExists(e(bucketDirectory));
    }

    @Override
    public void deleteContainer(BucketDirectory bucketDirectory) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("deleteContainer(" + bucketDirectory + ")");
        }
        extendedStoreConnection.deleteContainer(e(bucketDirectory));
    }

    @Override
    public List<StorageMetadata> list(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("list(" + bucketDirectory + " ," + listRecursiveFlag + ")");
        }
        return d(extendedStoreConnection.list(e(bucketDirectory), listRecursiveFlag));
    }

    @Override
    public List<BucketDirectory> listAllBuckets() {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("listAllBuckets()");
        }
        return de(extendedStoreConnection.listAllBuckets());
    }

    @Override
    public ExtendedStoreConnectionType getType() {
        return extendedStoreConnection.getType();
    }

    public MaxLengthInfo getMaxLengthInfo() {
        return maxLengthInfo;
    }

    private List<BucketDirectory> de(List<BucketDirectory> bucketDirectories) {
        List<BucketDirectory> newBucketDirectoryList = new ArrayList<>();
        bucketDirectories.forEach(bucketDirectory -> newBucketDirectoryList.add(d(bucketDirectory)));
        return newBucketDirectoryList;
    }

    private BucketPath e(BucketPath bucketPath) {
        if (!active) {
            return checkLength(maxLengthInfo, bucketPath, null);
        }
        return checkLength(maxLengthInfo, bucketPathEncryption.encrypt(bucketPathEncryptionPassword, bucketPathEncryptionFilenameOnly, bucketPath), bucketPath);
    }

    private BucketDirectory e(BucketDirectory bucketDirectory) {
        if (!active) {
            return checkLength(maxLengthInfo, bucketDirectory, null);
        }
        return checkLength(maxLengthInfo, bucketPathEncryption.encrypt(bucketPathEncryptionPassword, bucketPathEncryptionFilenameOnly, bucketDirectory), bucketDirectory);
    }

    private BucketDirectory d(BucketDirectory bucketDirectory) {
        if (!active) {
            return bucketDirectory;
        }
        return bucketPathEncryption.decrypt(bucketPathEncryptionPassword, bucketPathEncryptionFilenameOnly, bucketDirectory);
    }

    private Payload d(Payload payload) {
        if (!active) {
            return payload;
        }
        return new SimplePayloadImpl(d(payload.getStorageMetadata()), payload.getData());
    }

    private PayloadStream d(PayloadStream payloadStream) {
        if (!active) {
            return payloadStream;
        }
        return new SimplePayloadStreamImpl(d(payloadStream.getStorageMetadata()), payloadStream.openStream());
    }

    private StorageMetadata d(StorageMetadata storageMetadata) {
        if (!active) {
            return storageMetadata;
        }
        String encryptedName = storageMetadata.getName();
        String decryptedName = null;
        if (storageMetadata.getType().equals(StorageType.BLOB)) {
            BucketPath encryptedBucketPath = new BucketPath(encryptedName);
            BucketPath decryptedBucketPath = BucketPathEncryption.decrypt(bucketPathEncryptionPassword,
                    bucketPathEncryptionFilenameOnly,
                    encryptedBucketPath);
            decryptedName = BucketPathUtil.getAsString(decryptedBucketPath);
        }
        if (storageMetadata.getType().equals(StorageType.FOLDER)) {
            BucketDirectory encryptedBucketDirectory = new BucketDirectory(encryptedName);
            BucketDirectory decryptedBucketDirectory = BucketPathEncryption.decrypt(bucketPathEncryptionPassword,
                    bucketPathEncryptionFilenameOnly,
                    encryptedBucketDirectory);
            decryptedName = BucketPathUtil.getAsString(decryptedBucketDirectory);
        }
        SimpleStorageMetadataImpl newStorageMetadata = new SimpleStorageMetadataImpl(storageMetadata);
        newStorageMetadata.setName(decryptedName);
        return newStorageMetadata;
    }

    private StorageMetadata e(StorageMetadata storageMetadata) {
        if (!active) {
            return storageMetadata;
        }
        if (storageMetadata == null) {
            return storageMetadata;
        }
        String plainName = storageMetadata.getName();
        BucketPath plainBucketPath = new BucketPath(plainName);
        BucketPath encryptedBucketPath = BucketPathEncryption.encrypt(bucketPathEncryptionPassword,
                bucketPathEncryptionFilenameOnly,
                plainBucketPath);
        String encryptedName = BucketPathUtil.getAsString(encryptedBucketPath);

        SimpleStorageMetadataImpl newStorageMetadata = new SimpleStorageMetadataImpl(storageMetadata);
        newStorageMetadata.setName(encryptedName);
        return newStorageMetadata;
    }

    private List<StorageMetadata> d(List<StorageMetadata> list) {
        if (!active) {
            return list;
        }
        List<StorageMetadata> newStorageMetadataList = new ArrayList<>();
        list.forEach(storageMetadata -> newStorageMetadataList.add(d(storageMetadata)));
        return newStorageMetadataList;
    }


    public static class MaxLengthInfo {
        int partMaxLength;
        int totalMaxLength;

        public MaxLengthInfo(int partMaxLength, int totalMaxLength) {
            this.partMaxLength = partMaxLength;
            this.totalMaxLength = totalMaxLength;
        }

        public int getPartMaxLength() {
            return partMaxLength;
        }

        public int getTotalMaxLength() {
            return totalMaxLength;
        }
    }

    private static MaxLengthInfo getMaxLengthTupel(ExtendedStoreConnection extendedStoreConnection) {
        switch (extendedStoreConnection.getType()) {
            case FILESYSTEM:
            case AMAZONS3:
                return AMAZONS3_MAX_LENGTH;
            case MONGO:
                return MONGO__MAX_LENGTH;
            default:
                throw new BaseException("missing switch for " + extendedStoreConnection.getType());
        }
    }

    private BucketDirectory checkLength(MaxLengthInfo maxLengthInfo, BucketDirectory realPath, BucketDirectory plainPath) {
        String realPathAsString = BucketPathUtil.getAsString(realPath);

        for (String s : BucketPathUtil.split(realPathAsString)) {
            if (s.length() > maxLengthInfo.getPartMaxLength()) {
                if (plainPath == null) {
                    throw new BucketRestrictionException(realPathAsString + " has part " + s + " with length " + s.length() + " must not exceed part length " + maxLengthInfo.getPartMaxLength());
                }

                throw new BucketRestrictionException(BucketPathUtil.getAsString(plainPath) + " -> " + realPathAsString + " has part " + s + " with length " + s.length() + " must not exceed part length " + maxLengthInfo.getPartMaxLength());
            }
        }
        if (maxLengthInfo.getTotalMaxLength() > -1) {
            if (realPathAsString.length() > maxLengthInfo.getTotalMaxLength()) {
                if (plainPath == null) {
                    throw new BucketRestrictionException(realPathAsString + " with length " + realPathAsString.length() + " must not exceed part length " + maxLengthInfo.getTotalMaxLength());
                }

                throw new BucketRestrictionException(BucketPathUtil.getAsString(plainPath) + " -> " + realPathAsString + " with length " + realPathAsString.length() + " must not exceed part length " + maxLengthInfo.getTotalMaxLength());
            }
        }
        return realPath;
    }

    private BucketPath checkLength(MaxLengthInfo maxLengthInfo, BucketPath realPath, BucketPath plainPath) {
        String realPathAsString = BucketPathUtil.getAsString(realPath);

        for (String s : BucketPathUtil.split(realPathAsString)) {
            if (s.length() > maxLengthInfo.getPartMaxLength()) {
                if (plainPath == null) {
                    throw new BucketRestrictionException(realPathAsString + " has part " + s + " with length " + s.length() + " must not exceed part length " + maxLengthInfo.getPartMaxLength());
                }

                throw new BucketRestrictionException(BucketPathUtil.getAsString(plainPath) + " -> " + realPathAsString + " has part " + s + " with length " + s.length() + " must not exceed part length " + maxLengthInfo.getPartMaxLength());
            }
        }
        if (maxLengthInfo.getTotalMaxLength() > -1) {
            if (realPathAsString.length() > maxLengthInfo.getTotalMaxLength()) {
                if (plainPath == null) {
                    throw new BucketRestrictionException(realPathAsString + " with length " + realPathAsString.length() + " must not exceed part length " + maxLengthInfo.getTotalMaxLength());
                }

                throw new BucketRestrictionException(BucketPathUtil.getAsString(plainPath) + " -> " + realPathAsString + " with length " + realPathAsString.length() + " must not exceed part length " + maxLengthInfo.getTotalMaxLength());
            }
        }
        return realPath;
    }


}
