package org.adorsys.cryptoutils.storageconnection.testsuite;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.cryptoutils.storeconnectionfactory.ReadArguments;
import org.adorsys.cryptoutils.storeconnectionfactory.StoreConnectionFactoryConfig;
import org.junit.After;
import org.junit.Assert;
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
    String amazon;
    String filesys;

    @Before
    public void before() {
        minio = System.getProperty(ReadArguments.AMAZONS3);
        minio = System.getProperty(ReadArguments.MINIO);
        mongo = System.getProperty(ReadArguments.MONGO);
        filesys = System.getProperty(ReadArguments.FILESYSTEM);
        System.clearProperty(ReadArguments.AMAZONS3);
        System.clearProperty(ReadArguments.MINIO);
        System.clearProperty(ReadArguments.MONGO);
        System.clearProperty(ReadArguments.FILESYSTEM);
        System.clearProperty(ReadArguments.NO_ENCRYPTION_PASSWORD);
        System.clearProperty(ReadArguments.ENCRYPTION_PASSWORD);
    }

    @After
    public void after() {
        LOGGER.debug("----------------");
        System.clearProperty(ReadArguments.AMAZONS3);
        System.clearProperty(ReadArguments.MINIO);
        System.clearProperty(ReadArguments.MONGO);
        System.clearProperty(ReadArguments.FILESYSTEM);
        System.clearProperty(ReadArguments.NO_ENCRYPTION_PASSWORD);
        System.clearProperty(ReadArguments.ENCRYPTION_PASSWORD);
    }

    @Test
    public void testEnvMinio1() {
        System.setProperty(ReadArguments.MINIO, "http://localhost,accesskey,secretkey");
        StoreConnectionFactoryConfig config = new ReadArguments().readEnvironment();
        Assert.assertEquals(StoreConnectionFactoryConfig.ConnectionType.MINIO, config.connectionType);
        Assert.assertTrue(config.bucketPathEncryptionPassword != null);
    }

    @Test(expected = BaseException.class)
    public void testEnvMinioWrong() {
        System.setProperty(ReadArguments.MINIO, "http://localhost accesskey,secretkey");
        StoreConnectionFactoryConfig config = new ReadArguments().readEnvironment();
        Assert.assertEquals(StoreConnectionFactoryConfig.ConnectionType.MINIO, config.connectionType);
        Assert.assertTrue(config.bucketPathEncryptionPassword != null);
    }

    @Test
    public void testEnvMongo1() {
        System.setProperty(ReadArguments.MONGO, "localhost,123,mongdb");
        StoreConnectionFactoryConfig config = new ReadArguments().readEnvironment();
        Assert.assertEquals(StoreConnectionFactoryConfig.ConnectionType.MONGO, config.connectionType);
        Assert.assertTrue(config.bucketPathEncryptionPassword != null);
    }

    @Test(expected = BaseException.class)
    public void testEnvMongoWrong() {
        System.setProperty(ReadArguments.MONGO, "localhost,123|mongdb");
        StoreConnectionFactoryConfig config = new ReadArguments().readEnvironment();
        Assert.assertEquals(StoreConnectionFactoryConfig.ConnectionType.MONGO, config.connectionType);
        Assert.assertTrue(config.bucketPathEncryptionPassword != null);
    }

    @Test
    public void testEnvMongo2() {
        System.setProperty(ReadArguments.MONGO, "");
        StoreConnectionFactoryConfig config = new ReadArguments().readEnvironment();
        Assert.assertEquals(StoreConnectionFactoryConfig.ConnectionType.MONGO, config.connectionType);
        Assert.assertTrue(config.bucketPathEncryptionPassword != null);
    }

    @Test
    public void testEnvFilesystem1() {
        System.setProperty(ReadArguments.FILESYSTEM, "target/filesystem");
        StoreConnectionFactoryConfig config = new ReadArguments().readEnvironment();
        Assert.assertEquals(StoreConnectionFactoryConfig.ConnectionType.FILE_SYSTEM, config.connectionType);
        Assert.assertTrue(config.bucketPathEncryptionPassword != null);
    }

    @Test
    public void testEnvFilesystem2() {
        System.setProperty(ReadArguments.FILESYSTEM, "");
        StoreConnectionFactoryConfig config = new ReadArguments().readEnvironment();
        Assert.assertEquals(StoreConnectionFactoryConfig.ConnectionType.FILE_SYSTEM, config.connectionType);
        Assert.assertTrue(config.bucketPathEncryptionPassword != null);
    }

    @Test
    public void testArgMinio1() {
        String[] args = new String[1];
        args[0] = ReadArguments.MINIO_ARG+ "http://localhost,accesskey,secretkey";
        ReadArguments.ArgsAndConfig argsAndConfig = new ReadArguments().readArguments(args);
        Assert.assertEquals(0, argsAndConfig.remainingArgs.length);
        Assert.assertTrue(argsAndConfig.config.bucketPathEncryptionPassword != null);
    }

    @Test
    public void testArgMongo1() {
        String[] args = new String[1];
        args[0] = ReadArguments.MONGO_ARG + "localhost,123,mongdb";
        ReadArguments.ArgsAndConfig argsAndConfig = new ReadArguments().readArguments(args);
        Assert.assertEquals(0, argsAndConfig.remainingArgs.length);
        Assert.assertTrue(argsAndConfig.config.bucketPathEncryptionPassword != null);
    }

    @Test
    public void testArgMongo2() {
        String[] args = new String[1];
        args[0] = ReadArguments.MONGO_ARG;
        ReadArguments.ArgsAndConfig argsAndConfig = new ReadArguments().readArguments(args);
        Assert.assertEquals(0, argsAndConfig.remainingArgs.length);
        Assert.assertTrue(argsAndConfig.config.bucketPathEncryptionPassword != null);
    }

    @Test
    public void testArgFilesystem1() {
        String[] args = new String[1];
        args[0] = ReadArguments.FILESYSTEM_ARG + "target/filesystem";
        ReadArguments.ArgsAndConfig argsAndConfig = new ReadArguments().readArguments(args);
        Assert.assertEquals(0, argsAndConfig.remainingArgs.length);
        Assert.assertTrue(argsAndConfig.config.bucketPathEncryptionPassword != null);
    }

    @Test
    public void tesArgFilesystem2() {
        String[] args = new String[1];
        args[0] = ReadArguments.FILESYSTEM_ARG;
        ReadArguments.ArgsAndConfig argsAndConfig = new ReadArguments().readArguments(args);
        Assert.assertEquals(0, argsAndConfig.remainingArgs.length);
        Assert.assertTrue(argsAndConfig.config.bucketPathEncryptionPassword != null);
    }

    @Test
    public void testParam3Args() {
        String[] args = new String[3];
        args[0] = ReadArguments.AMAZONS3_ARG + "http:1,key,key";
        args[1] = ReadArguments.NO_ENCRYPTION_PASSWORD_ARG;
        args[2] = "anyParam";
        ReadArguments.ArgsAndConfig argsAndConfig = new ReadArguments().readArguments(args);
        Assert.assertEquals(1, argsAndConfig.remainingArgs.length);
        Assert.assertTrue(argsAndConfig.config.bucketPathEncryptionPassword == null);
    }


    @Test
    public void testEnv3Args() {
        System.setProperty(ReadArguments.AMAZONS3,"http:1,key,key");
        System.setProperty(ReadArguments.NO_ENCRYPTION_PASSWORD,"any");
        System.setProperty("any","any");
        StoreConnectionFactoryConfig config = new ReadArguments().readEnvironment();
        Assert.assertTrue(config.bucketPathEncryptionPassword == null);
    }

}
