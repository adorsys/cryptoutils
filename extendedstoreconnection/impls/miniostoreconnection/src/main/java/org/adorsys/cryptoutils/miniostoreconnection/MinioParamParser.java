package org.adorsys.cryptoutils.miniostoreconnection;

import org.adorsys.encobject.exceptions.ParamParserException;
import org.adorsys.encobject.types.connection.MinioAccessKey;
import org.adorsys.encobject.types.connection.MinioRootBucketName;
import org.adorsys.encobject.types.connection.MinioSecretKey;
import org.adorsys.encobject.types.properties.MinioConnectionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.StringTokenizer;

/**
 * Created by peter on 19.03.18 at 18:56.
 */
public class MinioParamParser {
    private final static Logger LOGGER = LoggerFactory.getLogger(MinioParamParser.class);
    private final static String DELIMITER = ",";
    private final static String EXPECTED_PARAMS = "<url>,<accesskey>,<secretkey>[,<rootbucket>]";

    public static MinioConnectionProperties getProperties(String params) {
        LOGGER.debug("parse:" + params);
        try {
            MinioConnectionPropertiesImpl properties = new MinioConnectionPropertiesImpl();

            StringTokenizer st = new StringTokenizer(params, DELIMITER);
            properties.setUrl(new URL(st.nextToken()));
            properties.setMinioAccessKey(new MinioAccessKey(st.nextToken()));
            properties.setMinioSecretKey(new MinioSecretKey(st.nextToken()));
            if (st.hasMoreElements()) {
                properties.setMinioRootBucketName(new MinioRootBucketName(st.nextToken()));
            }
            return properties;
        } catch (Exception e) {
            throw new ParamParserException(params, DELIMITER, EXPECTED_PARAMS);
        }
    }
}
