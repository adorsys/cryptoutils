package org.adorsys.encobject.service.impl;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.BucketPathUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by peter on 19.03.18 at 14:10.
 */
public class StoreConnectionListHelper {
    public static Set<BucketDirectory> findAllSubDirs(BucketDirectory rootdir, List<BucketPath> bucketPaths) {

        String prefix = BucketPathUtil.getAsString(rootdir);
        Set<BucketDirectory> allDirs = new HashSet<>();
        bucketPaths.forEach(bucketPath -> {
            String key = BucketPathUtil.getAsString(bucketPath);
            int fromIndex = prefix.length();
            while (fromIndex != -1) {
                fromIndex = key.indexOf(BucketPath.BUCKET_SEPARATOR, fromIndex+1);
                if (fromIndex != -1) {
                    String dir = key.substring(0, fromIndex);
                    if (dir.length() == 0) {
                        dir = BucketPath.BUCKET_SEPARATOR;
                    }
                    allDirs.add(new BucketDirectory(dir));
                }
            }

        });




        bucketPaths.forEach(bucketPath -> {
            allDirs.add(bucketPath.getBucketDirectory());
        });
        return allDirs;
    }

}
