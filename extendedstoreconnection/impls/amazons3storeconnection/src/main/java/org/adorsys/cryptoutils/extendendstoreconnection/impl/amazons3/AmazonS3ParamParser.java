package org.adorsys.cryptoutils.extendendstoreconnection.impl.amazons3;

import org.adorsys.encobject.exceptions.ParamParserException;
import org.adorsys.encobject.types.connection.AmazonS3AccessKey;
import org.adorsys.encobject.types.connection.AmazonS3Region;
import org.adorsys.encobject.types.connection.AmazonS3RootBucketName;
import org.adorsys.encobject.types.connection.AmazonS3SecretKey;
import org.adorsys.encobject.types.properties.AmazonS3ConnectionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.StringTokenizer;

/**
 * Created by peter on 18.09.18.
 */
public class AmazonS3ParamParser {
    private final static Logger LOGGER = LoggerFactory.getLogger(AmazonS3ParamParser.class);
    private final static String DELIMITER = ",";
    private final static String EXPECTED_PARAMS = "<url>,<accesskey>,<secretkey>[,<region>][,<rootbucket>]";

    public static AmazonS3ConnectionProperties getProperties(String params) {
        LOGGER.debug("parse:" + params);
        try {
            AmazonS3ConnectionProperitesImpl properites = new AmazonS3ConnectionProperitesImpl();

            StringTokenizer st = new StringTokenizer(params, DELIMITER);
            properites.setUrl(new URL(st.nextToken()));
            properites.setAmazonS3AccessKey(new AmazonS3AccessKey(st.nextToken()));
            properites.setAmazonS3SecretKey(new AmazonS3SecretKey(st.nextToken()));
            if (st.hasMoreTokens()) {
                properites.setAmazonS3Region(new AmazonS3Region(st.nextToken()));
            }
            if (st.hasMoreTokens()) {
                properites.setAmazonS3RootBucketName(new AmazonS3RootBucketName(st.nextToken()));
            }
            return properites;
        } catch (Exception e) {
            throw new ParamParserException(params, DELIMITER, EXPECTED_PARAMS);
        }
    }

}
