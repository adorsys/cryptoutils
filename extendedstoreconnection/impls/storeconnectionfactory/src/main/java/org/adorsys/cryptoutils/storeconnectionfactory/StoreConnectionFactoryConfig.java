package org.adorsys.cryptoutils.storeconnectionfactory;

import org.adorsys.cryptoutils.extendendstoreconnection.impl.amazons3.AmazonS3ParamParser;
import org.adorsys.cryptoutils.miniostoreconnection.MinioParamParser;
import org.adorsys.cryptoutils.mongodbstoreconnection.MongoParamParser;
import org.adorsys.encobject.filesystem.FileSystemParamParser;
import org.adorsys.encobject.types.BucketPathEncryptionPassword;

/**
 * Created by peter on 27.03.18 at 20:22.
 */
public class StoreConnectionFactoryConfig {

    public ConnectionType connectionType = ConnectionType.FILE_SYSTEM;
    public MinioParamParser minioParams = null;
    public AmazonS3ParamParser amazonS3Params = null;
    public MongoParamParser mongoParams = null;
    public FileSystemParamParser fileSystemParamParser = new FileSystemParamParser("");
    public BucketPathEncryptionPassword bucketPathEncryptionPassword = new BucketPathEncryptionPassword("2837/(&dfja34j39,yiesdkfhasdfkljh");

    public static enum ConnectionType {
        FILE_SYSTEM,
        MONGO,
        MINIO,
        AMAZONS3
    }
}
