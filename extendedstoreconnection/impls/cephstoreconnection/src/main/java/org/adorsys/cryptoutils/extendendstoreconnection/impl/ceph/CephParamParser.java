package org.adorsys.cryptoutils.extendendstoreconnection.impl.ceph;

import org.adorsys.encobject.exceptions.ParamParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.StringTokenizer;

/**
 * Created by peter on 18.09.18.
 */
public class CephParamParser {
    private final static Logger LOGGER = LoggerFactory.getLogger(CephParamParser.class);
    private URL url;
    private AmazonS3AccessKey minioAccessKey;
    private AmazonS3SecretKey minioSecretKey;
    private final static String DELIMITER = ",";

    public CephParamParser(String params) {
        LOGGER.debug("parse:" + params);
        try {
            StringTokenizer st = new StringTokenizer(params, DELIMITER);
            String urlString = st.nextToken();
            String accessKey = st.nextToken();
            String secretKey = st.nextToken();
            url = new URL(urlString);
            minioAccessKey = new AmazonS3AccessKey(accessKey);
            minioSecretKey = new AmazonS3SecretKey(secretKey);
        } catch (Exception e) {
            throw new ParamParserException(params, DELIMITER);
        }
    }

    public URL getUrl() {
        return url;
    }

    public AmazonS3AccessKey getMinioAccessKey() {
        return minioAccessKey;
    }

    public AmazonS3SecretKey getMinioSecretKey() {
        return minioSecretKey;
    }
}
