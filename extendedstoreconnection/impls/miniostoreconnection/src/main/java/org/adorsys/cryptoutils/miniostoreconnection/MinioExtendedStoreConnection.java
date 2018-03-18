package org.adorsys.cryptoutils.miniostoreconnection;

import io.minio.MinioClient;
import io.minio.ObjectStat;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.filesystem.StorageMetadataFlattenerGSON;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 18.03.18 at 19:59.
 */
public class MinioExtendedStoreConnection implements ExtendedStoreConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(MinioExtendedStoreConnection.class);
    private final MinioClient minioClient;
    private final StorageMetadataFlattenerGSON storageMetadataFlattenerGSON = new StorageMetadataFlattenerGSON();

    public MinioExtendedStoreConnection(URL url, MinioAccessKey minioAccessKey, MinioSecretKey minioSecretKey) {
        try {
            this.minioClient = new MinioClient(url, minioAccessKey.getValue(), minioSecretKey.getValue());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void putBlob(BucketPath bucketPath, Payload payload) {
        putBlobStream(bucketPath, new SimplePayloadStreamImpl(payload.getStorageMetadata(), new ByteArrayInputStream(payload.getData())));
    }

    @Override
    public Payload getBlob(BucketPath bucketPath) {
        try {
            PayloadStream payloadStream = getBlobStream(bucketPath);
            return new SimplePayloadImpl(payloadStream.getStorageMetadata(), IOUtils.toByteArray(payloadStream.openStream()));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void putBlobStream(BucketPath bucketPath, PayloadStream payloadStream) {
        try {
            String storageMetadataString = storageMetadataFlattenerGSON.toJson(payloadStream.getStorageMetadata());
            minioClient.putObject(bucketPath.getObjectHandle().getContainer(),
                    bucketPath.getObjectHandle().getName(),
                    payloadStream.openStream(),
                    0,
                    storageMetadataString);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public PayloadStream getBlobStream(BucketPath bucketPath) {
        try {
            StorageMetadata storageMetadata = getStorageMetadata(bucketPath);
            InputStream stream = minioClient.getObject(bucketPath.getObjectHandle().getContainer(), bucketPath.getObjectHandle().getName());
            PayloadStream payloadStream = new SimplePayloadStreamImpl(storageMetadata, stream);
            return payloadStream;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void putBlob(BucketPath bucketPath, byte[] bytes) {
        putBlob(bucketPath, new SimplePayloadImpl(new SimpleStorageMetadataImpl(), bytes));
    }

    @Override
    public StorageMetadata getStorageMetadata(BucketPath bucketPath) {
        try {
            ObjectStat objectStat = minioClient.statObject(bucketPath.getObjectHandle().getContainer(), bucketPath.getObjectHandle().getName());
            String storageMeagedataString = objectStat.contentType();
            StorageMetadata storageMetadata = storageMetadataFlattenerGSON.fromJson(storageMeagedataString);
            return storageMetadata;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public boolean blobExists(BucketPath bucketPath) {
        try {
            getStorageMetadata(bucketPath);
        } catch (Exception e) {
            LOGGER.warn("ganz mies, exception -> file exisitert nicht");
            return false;
        }
        return true;
    }

    @Override
    public void removeBlob(BucketPath bucketPath) {
        try {
            minioClient.removeObject(bucketPath.getObjectHandle().getContainer(), bucketPath.getObjectHandle().getName());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void removeBlobs(Iterable<BucketPath> iterable) {
        iterable.forEach(bucketPath -> removeBlob(bucketPath));

    }

    @Override
    public long countBlobs(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        return 0;
    }

    @Override
    public void createContainer(BucketDirectory bucketDirectory) {
        try {
            minioClient.makeBucket(bucketDirectory.getObjectHandle().getContainer());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public boolean containerExists(BucketDirectory bucketDirectory) {
        try {
            return minioClient.bucketExists(bucketDirectory.getObjectHandle().getContainer());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void deleteContainer(BucketDirectory bucketDirectory) {
        try {
            minioClient.removeBucket(bucketDirectory.getObjectHandle().getContainer());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public List<StorageMetadata> list(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        List<StorageMetadata> returnList = new ArrayList<>();
        return returnList;
    }
}
