package org.adorsys.cryptoutils.amazons3cmd;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 05.06.18 at 16:34.
 */
public class S3CmdListBuckets extends S3cmdBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(S3CmdListBuckets.class);
    public static List<BucketDirectory> listBuckets() {
        List<BucketDirectory> returnList = new ArrayList<>();
       exec("ls").getOutput().forEach(line -> {
           returnList.add(new BucketDirectory(line.replaceFirst(".*s3://","")));
       });
        return returnList;

    }
}
