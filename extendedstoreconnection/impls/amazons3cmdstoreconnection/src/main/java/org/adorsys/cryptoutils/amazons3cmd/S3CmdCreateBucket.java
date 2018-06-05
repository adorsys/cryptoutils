package org.adorsys.cryptoutils.amazons3cmd;

import org.adorsys.encobject.complextypes.BucketDirectory;

/**
 * Created by peter on 05.06.18 at 17:05.
 */
public class S3CmdCreateBucket extends S3cmdBase {
    public static void createBucket(BucketDirectory bd) {
        exec("mb", "s3://" + bd.getObjectHandle().getContainer());
    }
}
