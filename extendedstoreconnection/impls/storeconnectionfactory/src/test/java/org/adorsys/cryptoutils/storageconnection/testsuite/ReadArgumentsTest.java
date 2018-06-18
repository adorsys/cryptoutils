package org.adorsys.cryptoutils.storageconnection.testsuite;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.cryptoutils.storeconnectionfactory.ReadArguments;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 13.04.18 at 19:30.
 */
public class ReadArgumentsTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(ReadArgumentsTest.class);
    String minio;
    String mongo;
    String filesys;

    @Before
    public void before() {
        minio = System.getProperty("SC-MINIO");
        mongo = System.getProperty("SC-MONGO");
        filesys = System.getProperty("SC-FILESYSTEM");
        System.clearProperty("SC-MINIO");
        System.clearProperty("SC-MONGO");
        System.clearProperty("SC-FILESYSTEM");
    }

    @After
    public void after() {
        LOGGER.debug("----------------");
        System.clearProperty("SC-MINIO");
        System.clearProperty("SC-MONGO");
        System.clearProperty("SC-FILESYSTEM");
        if (minio != null) {
            System.setProperty("SC-MINIO", minio);
        }
        if (mongo != null) {
            System.setProperty("SC-MONGO", mongo);
        }
        if (filesys != null) {
            System.setProperty("SC-FILESYSTEM", filesys);
        }
    }

    @Test
    public void testEnvMinio1() {
        System.setProperty("SC-MINIO", "http://localhost,accesskey,secretkey");
        new ReadArguments().readEnvironment();
    }

    @Test(expected = BaseException.class)
    public void testEnvMinioWrong() {
        System.setProperty("SC-MINIO", "http://localhost accesskey,secretkey");
        new ReadArguments().readEnvironment();
    }

    @Test
    public void testEnvMongo1() {
        System.setProperty("SC-MONGO", "localhost,123,mongdb");
        new ReadArguments().readEnvironment();
    }

    @Test(expected = BaseException.class)
    public void testEnvMongoWrong() {
        System.setProperty("SC-MONGO", "localhost,123|mongdb");
        new ReadArguments().readEnvironment();
    }

    @Test
    public void testEnvMongo2() {
        System.setProperty("SC-MONGO", "");
        new ReadArguments().readEnvironment();
    }

    @Test
    public void testEnvFilesystem1() {
        System.setProperty("SC-FILESYSTEM", "target/filesystem");
        new ReadArguments().readEnvironment();
    }

    @Test
    public void testEnvFilesystem2() {
        System.setProperty("SC-FILESYSTEM", "");
        new ReadArguments().readEnvironment();
    }

    @Test
    public void testArgMinio1() {
        String[] args = new String[1];
        args[0] = "-DSC-MINIO=http://localhost,accesskey,secretkey";
        new ReadArguments().readArguments(args);
    }

    @Test
    public void testArgMongo1() {
        String[] args = new String[1];
        args[0] = "-DSC-MONGO=localhost,123,mongdb";
        new ReadArguments().readArguments(args);
    }

    @Test
    public void testArgMongo2() {
        String[] args = new String[1];
        args[0] = "-DSC-MONGO=";
        new ReadArguments().readArguments(args);
    }

    @Test
    public void testArgFilesystem1() {
        String[] args = new String[1];
        args[0] = "-DSC-FILESYSTEM=target/filesystem";
        new ReadArguments().readArguments(args);
    }

    @Test
    public void tesArgFilesystem2() {
        String[] args = new String[1];
        args[0] = "-DSC-FILESYSTEM=";
        new ReadArguments().readArguments(args);
    }
}
