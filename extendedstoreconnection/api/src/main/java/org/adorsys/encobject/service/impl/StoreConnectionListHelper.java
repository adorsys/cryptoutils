package org.adorsys.encobject.service.impl;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by peter on 19.03.18 at 14:10.
 */
public class StoreConnectionListHelper {
    public static Set<BucketDirectory> findAllSubDirs(List<BucketPath> bucketPaths) {
        Set<BucketDirectory> allDirs = new HashSet<>();
        bucketPaths.forEach(bucketPath -> {
            allDirs.add(bucketPath.getBucketDirectory());
        });
        return allDirs;
    }

}
