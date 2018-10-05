package org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption;

import org.adorsys.cryptoutils.utils.Frame;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.BucketPathUtil;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.BucketPathEncryptionPassword;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 26.09.18.
 */
public class BucketPathEncryptingExtendedStoreConnection implements ExtendedStoreConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(BucketPathEncryptingExtendedStoreConnection.class);
    protected ExtendedStoreConnection extendedStoreConnection;
    BucketPathEncryption bucketPathEncryption;
    BucketPathEncryptionPassword bucketPathEncryptionPassword;
    boolean active = bucketPathEncryptionPassword != null;

    public BucketPathEncryptingExtendedStoreConnection(ExtendedStoreConnection extendedStoreConnection,
                                                       BucketPathEncryptionPassword bucketPathEncryptionPassword) {
        this.extendedStoreConnection = extendedStoreConnection;
        this.bucketPathEncryption = new BucketPathEncryption();
        this.bucketPathEncryptionPassword = bucketPathEncryptionPassword;
        this.active = bucketPathEncryptionPassword != null;
        Frame frame = new Frame();
        if (active) {
            frame.add(bucketPathEncryptionPassword.toString());
        } else {
            frame.add("Filenames will not be encrypted");
        }
        LOGGER.info(frame.toString());

    }

    @Override
    public void putBlob(BucketPath bucketPath, Payload payload) {
        extendedStoreConnection.putBlob(e(bucketPath), payload);
    }

    @Override
    public Payload getBlob(BucketPath bucketPath) {
        return d(extendedStoreConnection.getBlob(e(bucketPath)));
    }

    @Override
    public Payload getBlob(BucketPath bucketPath, StorageMetadata storageMetadata) {
        return d(extendedStoreConnection.getBlob(e(bucketPath), e(storageMetadata)));
    }

    @Override
    public void putBlobStream(BucketPath bucketPath, PayloadStream payloadStream) {
        extendedStoreConnection.putBlobStream(e(bucketPath), payloadStream);
    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath) {
        return d(extendedStoreConnection.getBlobStream(e(bucketPath)));
    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath, StorageMetadata storageMetadata) {
        return d(extendedStoreConnection.getBlobStream(e(bucketPath), e(storageMetadata)));
    }

    @Override
    public void putBlob(BucketPath bucketPath, byte[] bytes) {
        extendedStoreConnection.putBlob(e(bucketPath), bytes);
    }

    @Override
    public StorageMetadata getStorageMetadata(BucketPath bucketPath) {
        return d(extendedStoreConnection.getStorageMetadata(e(bucketPath)));
    }

    @Override
    public boolean blobExists(BucketPath bucketPath) {
        return extendedStoreConnection.blobExists(e(bucketPath));
    }

    @Override
    public void removeBlob(BucketPath bucketPath) {
        extendedStoreConnection.removeBlob(e(bucketPath));
    }

    @Override
    public void removeBlobFolder(BucketDirectory bucketDirectory) {
        extendedStoreConnection.removeBlobFolder(e(bucketDirectory));
    }

    @Override
    public void createContainer(BucketDirectory bucketDirectory) {
        extendedStoreConnection.createContainer(e(bucketDirectory));
    }

    @Override
    public boolean containerExists(BucketDirectory bucketDirectory) {
        return extendedStoreConnection.containerExists(e(bucketDirectory));
    }

    @Override
    public void deleteContainer(BucketDirectory bucketDirectory) {
        extendedStoreConnection.deleteContainer(e(bucketDirectory));
    }

    @Override
    public List<StorageMetadata> list(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        return d(extendedStoreConnection.list(e(bucketDirectory), listRecursiveFlag));
    }

    @Override
    public List<BucketDirectory> listAllBuckets() {
        return de(extendedStoreConnection.listAllBuckets());
    }

    private List<BucketDirectory> de(List<BucketDirectory> bucketDirectories) {
        List<BucketDirectory> newBucketDirectoryList = new ArrayList<>();
        bucketDirectories.forEach(bucketDirectory -> newBucketDirectoryList.add(d(bucketDirectory)));
        return newBucketDirectoryList;
    }

    private BucketPath e(BucketPath bucketPath) {
        if (!active) {
            return bucketPath;
        }
        return bucketPathEncryption.encrypt(bucketPathEncryptionPassword, bucketPath);
    }

    private BucketDirectory e(BucketDirectory bucketDirectory) {
        if (! active) {
            return bucketDirectory;
        }
        return bucketPathEncryption.encrypt(bucketPathEncryptionPassword, bucketDirectory);
    }

    private BucketDirectory d(BucketDirectory bucketDirectory) {
        if (! active) {
            return bucketDirectory;
        }
        return bucketPathEncryption.decrypt(bucketPathEncryptionPassword, bucketDirectory);
    }

    private Payload d(Payload payload) {
        if (! active) {
            return payload;
        }
        return new SimplePayloadImpl(d(payload.getStorageMetadata()), payload.getData());
    }

    private PayloadStream d(PayloadStream payloadStream) {
        if (! active) {
            return payloadStream;
        }
        return new SimplePayloadStreamImpl(d(payloadStream.getStorageMetadata()), payloadStream.openStream());
    }

    private StorageMetadata d(StorageMetadata storageMetadata) {
        if (! active) {
            return storageMetadata;
        }
        String encryptedName = storageMetadata.getName();
        BucketPath encryptedBucketPath = new BucketPath(encryptedName);
        BucketPath decryptedBucketPath = BucketPathEncryption.decrypt(bucketPathEncryptionPassword, encryptedBucketPath);
        String decryptedName = BucketPathUtil.getAsString(decryptedBucketPath);

        SimpleStorageMetadataImpl newStorageMetadata = new SimpleStorageMetadataImpl(storageMetadata);
        newStorageMetadata.setName(decryptedName);
        return newStorageMetadata;
    }

    private StorageMetadata e(StorageMetadata storageMetadata) {
        if (! active) {
            return storageMetadata;
        }
        if (storageMetadata == null) {
            return storageMetadata;
        }
        String plainName = storageMetadata.getName();
        BucketPath plainBucketPath = new BucketPath(plainName);
        BucketPath encryptedBucketPath = BucketPathEncryption.encrypt(bucketPathEncryptionPassword, plainBucketPath);
        String encryptedName = BucketPathUtil.getAsString(encryptedBucketPath);

        SimpleStorageMetadataImpl newStorageMetadata = new SimpleStorageMetadataImpl(storageMetadata);
        newStorageMetadata.setName(encryptedName);
        return newStorageMetadata;
    }

    private List<StorageMetadata> d(List<StorageMetadata> list) {
        if (! active) {
            return list;
        }
        List<StorageMetadata> newStorageMetadataList = new ArrayList<>();
        list.forEach(storageMetadata -> newStorageMetadataList.add(d(storageMetadata)));
        return newStorageMetadataList;
    }


}
