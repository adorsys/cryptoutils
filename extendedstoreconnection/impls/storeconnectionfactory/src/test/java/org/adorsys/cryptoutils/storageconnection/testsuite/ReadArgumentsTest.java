package org.adorsys.cryptoutils.storageconnection.testsuite;

import org.adorsys.cryptoutils.storeconnectionfactory.ReadArguments;
import org.adorsys.encobject.types.properties.AmazonS3ConnectionProperties;
import org.adorsys.encobject.types.properties.ConnectionProperties;
import org.adorsys.encobject.types.properties.FilesystemConnectionProperties;
import org.adorsys.encobject.types.properties.MongoConnectionProperties;
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
        mongo = System.getProperty(ReadArguments.MONGO);
        filesys = System.getProperty(ReadArguments.FILESYSTEM);
        System.clearProperty(ReadArguments.AMAZONS3);
        System.clearProperty(ReadArguments.MONGO);
        System.clearProperty(ReadArguments.FILESYSTEM);
        System.clearProperty(ReadArguments.NO_ENCRYPTION_PASSWORD);
        System.clearProperty(ReadArguments.ENCRYPTION_PASSWORD);
    }

    @After
    public void after() {
        LOGGER.debug("----------------");
        System.clearProperty(ReadArguments.AMAZONS3);
        System.clearProperty(ReadArguments.MONGO);
        System.clearProperty(ReadArguments.FILESYSTEM);
        System.clearProperty(ReadArguments.NO_ENCRYPTION_PASSWORD);
        System.clearProperty(ReadArguments.ENCRYPTION_PASSWORD);
    }

    @Test
    public void testEnvMongo1() {
        System.setProperty(ReadArguments.MONGO, "localhost:27017/mongdb");
        ConnectionProperties properties = new ReadArguments().readEnvironment();
        Assert.assertTrue(properties instanceof  MongoConnectionProperties);
        Assert.assertTrue(properties.getBucketPathEncryptionPassword() != null);
        MongoConnectionProperties m = (MongoConnectionProperties) properties;
        Assert.assertTrue(m.getMongoURI() != null);
    }

    @Test
    public void testEnvMongo2() {
        System.setProperty(ReadArguments.MONGO, "");
        ConnectionProperties properties = new ReadArguments().readEnvironment();
        Assert.assertTrue(properties instanceof MongoConnectionProperties);
        Assert.assertTrue(properties.getBucketPathEncryptionPassword() != null);
    }

    @Test
    public void testEnvFilesystem1() {
        System.setProperty(ReadArguments.FILESYSTEM, "target/filesystem");
        ConnectionProperties properties = new ReadArguments().readEnvironment();
        Assert.assertTrue(properties instanceof  FilesystemConnectionProperties);
        Assert.assertTrue(properties.getBucketPathEncryptionPassword() != null);
    }

    @Test
    public void testEnvFilesystem2() {
        System.setProperty(ReadArguments.FILESYSTEM, "");
        ConnectionProperties properties = new ReadArguments().readEnvironment();
        Assert.assertTrue(properties instanceof  FilesystemConnectionProperties);
        Assert.assertTrue(properties.getBucketPathEncryptionPassword() != null);
    }

    @Test
    public void testArgMongo1() {
        String[] args = new String[1];
        args[0] = ReadArguments.MONGO_ARG + "localhost,123,mongdb";
        ReadArguments.ArgsAndProperties argsAndProperties = new ReadArguments().readArguments(args);
        Assert.assertEquals(0, argsAndProperties.remainingArgs.length);
        Assert.assertTrue(argsAndProperties.properties.getBucketPathEncryptionPassword() != null);
    }

    @Test
    public void testArgMongo2() {
        String[] args = new String[1];
        args[0] = ReadArguments.MONGO_ARG;
        ReadArguments.ArgsAndProperties argsAndProperties = new ReadArguments().readArguments(args);
        Assert.assertEquals(0, argsAndProperties.remainingArgs.length);
        Assert.assertTrue(argsAndProperties.properties.getBucketPathEncryptionPassword() != null);
    }

    @Test
    public void testArgFilesystem1() {
        String[] args = new String[1];
        args[0] = ReadArguments.FILESYSTEM_ARG + "target/filesystem";
        ReadArguments.ArgsAndProperties argsAndProperties = new ReadArguments().readArguments(args);
        Assert.assertEquals(0, argsAndProperties.remainingArgs.length);
        Assert.assertTrue(argsAndProperties.properties.getBucketPathEncryptionPassword() != null);
    }

    @Test
    public void tesArgFilesystem2() {
        String[] args = new String[1];
        args[0] = ReadArguments.FILESYSTEM_ARG;
        ReadArguments.ArgsAndProperties argsAndProperties = new ReadArguments().readArguments(args);
        Assert.assertEquals(0, argsAndProperties.remainingArgs.length);
        Assert.assertTrue(argsAndProperties.properties.getBucketPathEncryptionPassword() != null);
    }

    @Test
    public void testParam3Args() {
        String[] args = new String[3];
        args[0] = ReadArguments.AMAZONS3_ARG + "http:1,key,key";
        args[1] = ReadArguments.NO_ENCRYPTION_PASSWORD_ARG;
        args[2] = "anyParam";
        ReadArguments.ArgsAndProperties argsAndProperties = new ReadArguments().readArguments(args);
        Assert.assertEquals(1, argsAndProperties.remainingArgs.length);
        Assert.assertTrue(argsAndProperties.properties.getBucketPathEncryptionPassword() == null);
    }


    @Test
    public void testEnv3Args() {
        System.setProperty(ReadArguments.AMAZONS3,"http:1,key,key");
        System.setProperty(ReadArguments.NO_ENCRYPTION_PASSWORD,"any");
        System.setProperty("any","any");
        ConnectionProperties properties = new ReadArguments().readEnvironment();
        Assert.assertTrue(properties instanceof AmazonS3ConnectionProperties);
        Assert.assertTrue(properties.getBucketPathEncryptionPassword() == null);
    }

}
