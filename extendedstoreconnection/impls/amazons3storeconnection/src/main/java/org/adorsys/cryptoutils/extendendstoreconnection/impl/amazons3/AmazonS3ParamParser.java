package org.adorsys.cryptoutils.extendendstoreconnection.impl.amazons3;

import org.adorsys.encobject.exceptions.ParamParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.StringTokenizer;

/**
 * Created by peter on 18.09.18.
 */
public class AmazonS3ParamParser {
    private final static Logger LOGGER = LoggerFactory.getLogger(AmazonS3ParamParser.class);
    private URL url;
    private AmazonS3AccessKey amazonS3AccessKey;
    private AmazonS3SecretKey amazonS3SecretKey;
    private final static String DELIMITER = ",";
    private final static String EXPECTED_PARAMS = "<url>,<accesskey>,<secretkey>[,<region>][,<rootbucket>]";
    private AmazonS3Region amazonS3Region = AmazonS3ExtendedStoreConnection.DEFAULT_REGION;
    private AmazonS3RootBucket amazonS3RootBucket = AmazonS3ExtendedStoreConnection.DEFAULT_BUCKET;

    public AmazonS3ParamParser(String params) {
        LOGGER.debug("parse:" + params);
        try {
            StringTokenizer st = new StringTokenizer(params, DELIMITER);
            url = new URL(st.nextToken());
            amazonS3AccessKey = new AmazonS3AccessKey(st.nextToken());
            amazonS3SecretKey = new AmazonS3SecretKey(st.nextToken());
            if (st.hasMoreTokens()) {
                amazonS3Region = new AmazonS3Region(st.nextToken());
            }
            if (st.hasMoreTokens()) {
                amazonS3RootBucket = new AmazonS3RootBucket(st.nextToken());
            }
        } catch (Exception e) {
            throw new ParamParserException(params, DELIMITER, EXPECTED_PARAMS);
        }
    }

    public URL getUrl() {
        return url;
    }

    public AmazonS3AccessKey getAmazonS3AccessKey() {
        return amazonS3AccessKey;
    }

    public AmazonS3SecretKey getAmazonS3SecretKey() {
        return amazonS3SecretKey;
    }

    public AmazonS3Region getAmazonS3Region() {
        return amazonS3Region;
    }

    public AmazonS3RootBucket getAmazonS3RootBucket() {
        return amazonS3RootBucket;
    }
}
