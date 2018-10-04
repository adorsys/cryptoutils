package org.adorsys.encobject.types.connection;

import org.adorsys.cryptoutils.basetypes.BaseTypeString;

/**
 * Created by peter on 25.09.18.
 * Entspricht einem pyhsischen Bucket, dieser ist für die Benutzer der Schnittstelle transaprent. Alle in
 * der Schnittstelle angelegten Buckets liegen unterhalb dieses Buckets
 */
public class AmazonS3RootBucketName extends BaseTypeString {
    public AmazonS3RootBucketName(String s) {
        super (s);
    }
}
