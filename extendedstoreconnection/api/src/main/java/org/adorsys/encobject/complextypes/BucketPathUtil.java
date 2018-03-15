package org.adorsys.encobject.complextypes;

import org.adorsys.cryptoutils.exceptions.BaseException;
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

    public static void checkContainerName(String name) {
        if (name.indexOf(BucketPath.BUCKET_SEPARATOR) != -1) {
            throw new BaseException(name + " is not a valid container name. Must not contain " + BucketPath.BUCKET_SEPARATOR);
        }
    }
}
