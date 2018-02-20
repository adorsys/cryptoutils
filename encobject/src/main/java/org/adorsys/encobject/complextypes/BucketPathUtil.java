package org.adorsys.encobject.complextypes;

import org.adorsys.encobject.domain.ObjectHandle;

/**
 * Created by peter on 20.02.18 at 08:37.
 */
public class BucketPathUtil {
    public static String getAsString(BucketDirectory bucketDirectory) {
        ObjectHandle objectHandle = bucketDirectory.getObjectHandle();
        return objectHandle.getContainer() + BucketPath.BUCKET_SEPARATOR + objectHandle.getName();
    }

    public static String getAsString(BucketPath bucketDirectory) {
        ObjectHandle objectHandle = bucketDirectory.getObjectHandle();
        return objectHandle.getContainer() + BucketPath.BUCKET_SEPARATOR + objectHandle.getName();
    }
}
