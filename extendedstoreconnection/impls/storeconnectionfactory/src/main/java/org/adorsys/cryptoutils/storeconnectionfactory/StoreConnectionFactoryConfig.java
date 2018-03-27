package org.adorsys.cryptoutils.storeconnectionfactory;

import org.adorsys.cryptoutils.miniostoreconnection.MinioParamParser;

/**
 * Created by peter on 27.03.18 at 20:22.
 */
public class StoreConnectionFactoryConfig {

    public ConnectionType connectionType = ConnectionType.FILE_SYSTEM;
    public MinioParamParser minioParams = null;

    public static enum ConnectionType {
        FILE_SYSTEM,
        MONGO,
        MINIO
    }
}
