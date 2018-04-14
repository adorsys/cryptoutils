package org.adorsys.cryptoutils.storeconnectionfactory;

import org.adorsys.cryptoutils.miniostoreconnection.MinioParamParser;
import org.adorsys.cryptoutils.mongodbstoreconnection.MongoParamParser;
import org.adorsys.encobject.filesystem.FileSystemParamParser;

/**
 * Created by peter on 27.03.18 at 20:22.
 */
public class StoreConnectionFactoryConfig {

    public ConnectionType connectionType = ConnectionType.FILE_SYSTEM;
    public MinioParamParser minioParams = null;
    public MongoParamParser mongoParams = null;
    public FileSystemParamParser fileSystemParamParser = new FileSystemParamParser("");

    public static enum ConnectionType {
        FILE_SYSTEM,
        MONGO,
        MINIO
    }
}
