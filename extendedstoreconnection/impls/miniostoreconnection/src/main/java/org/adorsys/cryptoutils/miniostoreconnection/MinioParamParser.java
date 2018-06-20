package org.adorsys.cryptoutils.miniostoreconnection;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.exceptions.ParamParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.StringTokenizer;

/**
 * Created by peter on 19.03.18 at 18:56.
 */
public class MinioParamParser {
    private final static Logger LOGGER = LoggerFactory.getLogger(MinioParamParser.class);
    private URL url;
    private MinioAccessKey minioAccessKey;
    private MinioSecretKey minioSecretKey;
    private String rootBucketName;
    private final static String DELIMITER = ",";

    public MinioParamParser(String params) {
        LOGGER.debug("parse:" + params);
        try {
            StringTokenizer st = new StringTokenizer(params, DELIMITER);
            String urlString = st.nextToken();
            String accessKey = st.nextToken();
            String secretKey = st.nextToken();
            url = new URL(urlString);
            minioAccessKey = new MinioAccessKey(accessKey);
            minioSecretKey = new MinioSecretKey(secretKey);
            if (st.hasMoreElements()) {
                rootBucketName = st.nextToken();
            } else {
                rootBucketName = MinioExtendedStoreConnection.DEFAULT_ROOT_BUCKET_NAME;
            }
        } catch (Exception e) {
            throw new ParamParserException(params, DELIMITER);
        }
    }

    public URL getUrl() {
        return url;
    }

    public MinioAccessKey getMinioAccessKey() {
        return minioAccessKey;
    }

    public MinioSecretKey getMinioSecretKey() {
        return minioSecretKey;
    }

    public String getRootBucketName() {
        return rootBucketName;
    }
}
