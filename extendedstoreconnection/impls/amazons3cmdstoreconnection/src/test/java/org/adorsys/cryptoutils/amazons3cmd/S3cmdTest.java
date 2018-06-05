package org.adorsys.cryptoutils.amazons3cmd;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 05.06.18 at 13:44.
 */
public class S3cmdTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(S3cmdTest.class);

    // @Test
    public void a() {
        S3CmdListBuckets.listBuckets().forEach(bucketDirectory -> LOGGER.info(bucketDirectory.toString()));
        S3CmdCreateBucket.createBucket(new BucketDirectory("affe1"));
        S3CmdCreateBucket.createBucket(new BucketDirectory("affe2"));
        S3CmdListBuckets.listBuckets().forEach(bucketDirectory -> LOGGER.info(bucketDirectory.toString()));
    }
}
