package org.adorsys.cryptoutils.extendedstoreconnection.impls.pathencryption;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.exceptions.PathDecryptionException;
import org.adorsys.encobject.types.BucketPathEncryptionPassword;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 25.09.18.
 */
public class BucketPathEncryptionTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(BucketPathEncryption.class);

    @Test
    public void testContainerOnly() {
        BucketPath bucketPath = new BucketPath("peter");
        BucketPathEncryptionPassword bucketPathEncryptionPassword = new BucketPathEncryptionPassword("affe");
        doTest(bucketPath, bucketPathEncryptionPassword);
    }

    @Test
    public void testSimplePath() {
        BucketPath bucketPath = new BucketPath("peter/folder1");
        BucketPathEncryptionPassword bucketPathEncryptionPassword = new BucketPathEncryptionPassword("affe");
        doTest(bucketPath, bucketPathEncryptionPassword);
    }
    @Test
    public void testDeepPathPath() {
        BucketPath bucketPath = new BucketPath("peter/folder1/1/2/3");
        BucketPathEncryptionPassword bucketPathEncryptionPassword = new BucketPathEncryptionPassword("affe");
        doTest(bucketPath, bucketPathEncryptionPassword);
    }

    @Test (expected = PathDecryptionException.class)
    public void testWrongPassword() {
        BucketPath bucketPath = new BucketPath("peter/folder1/1/2/3");
        BucketPathEncryptionPassword bucketPathEncryptionPassword1 = new BucketPathEncryptionPassword("affe1");
        BucketPathEncryptionPassword bucketPathEncryptionPassword2 = new BucketPathEncryptionPassword("affe2");
        BucketPath encryptedBucketPath = BucketPathEncryption.encrypt(bucketPathEncryptionPassword1, bucketPath);
        BucketPath decryptedBucketPath = BucketPathEncryption.decrypt(bucketPathEncryptionPassword2, encryptedBucketPath);
    }

    @Test
    public void lasttest() {
        int NUMBER = 1000;
        BucketPath bucketPath = new BucketPath("peter/folder1/1/2/3");
        BucketPathEncryptionPassword bucketPathEncryptionPassword = new BucketPathEncryptionPassword("asdfasdfasdf45dda");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i<NUMBER; i++) {
            BucketPath encryptedBucketPath = BucketPathEncryption.encrypt(bucketPathEncryptionPassword, bucketPath);
            BucketPath decryptedBucketPath = BucketPathEncryption.decrypt(bucketPathEncryptionPassword, encryptedBucketPath);
        }
        stopWatch.stop();
        LOGGER.info("time for " + NUMBER + " en- and decryptions took " + stopWatch.toString());

    }

    private void doTest(BucketPath bucketPath, BucketPathEncryptionPassword bucketPathEncryptionPassword) {
        BucketPath encryptedBucketPath = BucketPathEncryption.encrypt(bucketPathEncryptionPassword, bucketPath);
        BucketPath decryptedBucketPath = BucketPathEncryption.decrypt(bucketPathEncryptionPassword, encryptedBucketPath);
        LOGGER.debug("    plain bucket path:" + bucketPath);
        LOGGER.debug("encrypted bucket path:" + encryptedBucketPath);
        LOGGER.debug("decrypted bucket path:" + decryptedBucketPath);
        Assert.assertEquals(bucketPath, decryptedBucketPath);
        if (BucketPathEncryption.encryptContainer) {
            Assert.assertNotEquals(bucketPath.getObjectHandle().getContainer(), encryptedBucketPath.getObjectHandle().getContainer());
        } else {
            Assert.assertEquals(bucketPath.getObjectHandle().getContainer(), encryptedBucketPath.getObjectHandle().getContainer());
        }
    }

    @Test
    public void dtestContainerOnly() {
        BucketDirectory bucketDirectory = new BucketDirectory("peter");
        BucketPathEncryptionPassword bucketPathEncryptionPassword = new BucketPathEncryptionPassword("affe");
        doTest(bucketDirectory, bucketPathEncryptionPassword);
    }

    @Test
    public void dtestSimplePath() {
        BucketDirectory bucketDirectory = new BucketDirectory("peter/folder1");
        BucketPathEncryptionPassword bucketPathEncryptionPassword = new BucketPathEncryptionPassword("affe");
        doTest(bucketDirectory, bucketPathEncryptionPassword);
    }
    @Test
    public void dtestDeepPathPath() {
        BucketDirectory bucketDirectory = new BucketDirectory("peter/folder1/1/2/3");
        BucketPathEncryptionPassword bucketPathEncryptionPassword = new BucketPathEncryptionPassword("affe");
        doTest(bucketDirectory, bucketPathEncryptionPassword);
    }

    @Test (expected = PathDecryptionException.class)
    public void dtestWrongPassword() {
        BucketDirectory BucketDirectory = new BucketDirectory("peter/folder1/1/2/3");
        BucketPathEncryptionPassword bucketPathEncryptionPassword1 = new BucketPathEncryptionPassword("affe1");
        BucketPathEncryptionPassword bucketPathEncryptionPassword2 = new BucketPathEncryptionPassword("affe2");
        org.adorsys.encobject.complextypes.BucketDirectory encryptedBucketDirectory = BucketPathEncryption.encrypt(bucketPathEncryptionPassword1, BucketDirectory);
        org.adorsys.encobject.complextypes.BucketDirectory decryptedBucketDirectory = BucketPathEncryption.decrypt(bucketPathEncryptionPassword2, encryptedBucketDirectory);
    }
    private void doTest(BucketDirectory bucketDirectory, BucketPathEncryptionPassword bucketPathEncryptionPassword) {
        BucketDirectory encryptedBucketDirectory = BucketPathEncryption.encrypt(bucketPathEncryptionPassword, bucketDirectory);
        BucketDirectory decryptedBucketDirectory = BucketPathEncryption.decrypt(bucketPathEncryptionPassword, encryptedBucketDirectory);
        LOGGER.debug("    plain bucket path:" + bucketDirectory);
        LOGGER.debug("encrypted bucket path:" + encryptedBucketDirectory);
        LOGGER.debug("decrypted bucket path:" + decryptedBucketDirectory);
        Assert.assertEquals(bucketDirectory, decryptedBucketDirectory);
        if (BucketPathEncryption.encryptContainer) {
            Assert.assertNotEquals(bucketDirectory.getObjectHandle().getContainer(), encryptedBucketDirectory.getObjectHandle().getContainer());
        } else {
            Assert.assertEquals(bucketDirectory.getObjectHandle().getContainer(), encryptedBucketDirectory.getObjectHandle().getContainer());
        }
    }


}
