package org.adorsys.cryptoutils.storageconnection.testsuite;

import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 13.04.18 at 19:30.
 */
public class ReadArgumentsTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(ReadArgumentsTest.class);

    @Before
    public void before() {
        LOGGER.info("----------------");
        System.clearProperty("SC-MINIO");
        System.clearProperty("SC-MONGO");
        System.clearProperty("SC-FILESYSTEM");
        ExtendedStoreConnectionFactory.reset();
    }

    @Test
    public void testEnvMinio1() {
        try {
            System.setProperty("SC-MINIO", "http://localhost,accesskey,secretkey");
            ExtendedStoreConnectionFactory.get();
        } catch (Exception e) {
        }
    }
    @Test
    public void testEnvMongo1() {
        try {
            System.setProperty("SC-MONGO", "localhost,123,mongdb");
            ExtendedStoreConnectionFactory.get();
        } catch (Exception e) {
        }
    }
    @Test
    public void testEnvMongo2() {
        try {
            System.setProperty("SC-MONGO", "");
            ExtendedStoreConnectionFactory.get();
        } catch (Exception e) {
        }
    }
    @Test
    public void testEnvFilesystem1() {
        try {
            System.setProperty("SC-FILESYSTEM", "target/filesystem");
            ExtendedStoreConnectionFactory.get();
        } catch (Exception e) {
        }
    }
    @Test
    public void testEnvFilesystem2() {
        try {
            System.setProperty("SC-FILESYSTEM", "");
            ExtendedStoreConnectionFactory.get();
        } catch (Exception e) {
        }
    }


    @Test
    public void testArgMinio1() {
        try {
            String[] args = new String[1];
            args[0]="-DSC-MINIO=http://localhost,accesskey,secretkey";
            ExtendedStoreConnectionFactory.readArguments(args);
            ExtendedStoreConnectionFactory.get();
        } catch (Exception e) {
        }
    }
    @Test
    public void testArgMongo1() {
        try {
            String[] args = new String[1];
            args[0]="-DSC-MONGO=localhost,123,mongdb";
            ExtendedStoreConnectionFactory.readArguments(args);
            ExtendedStoreConnectionFactory.get();
        } catch (Exception e) {
        }
    }
    @Test
    public void testArgMongo2() {
        try {
            String[] args = new String[1];
            args[0]="-DSC-MONGO=";
            ExtendedStoreConnectionFactory.readArguments(args);
            ExtendedStoreConnectionFactory.get();
        } catch (Exception e) {
        }
    }
    @Test
    public void testArgFilesystem1() {
        try {
            String[] args = new String[1];
            args[0]="-DSC-FILESYSTEM=target/filesystem";
            ExtendedStoreConnectionFactory.readArguments(args);
            ExtendedStoreConnectionFactory.get();
        } catch (Exception e) {
        }
    }
    @Test
    public void tesArgFilesystem2() {
        try {
            String[] args = new String[1];
            args[0]="-DSC-FILESYSTEM=";
            ExtendedStoreConnectionFactory.readArguments(args);
            ExtendedStoreConnectionFactory.get();
        } catch (Exception e) {
        }
    }
}
