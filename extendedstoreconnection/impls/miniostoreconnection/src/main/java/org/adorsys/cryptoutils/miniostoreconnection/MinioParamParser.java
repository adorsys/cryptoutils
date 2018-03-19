package org.adorsys.cryptoutils.miniostoreconnection;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;

import java.net.URL;
import java.util.StringTokenizer;

/**
 * Created by peter on 19.03.18 at 18:56.
 */
public class MinioParamParser {
    private URL url;
    private MinioAccessKey minioAccessKey;
    private MinioSecretKey minioSecretKey;

    public MinioParamParser(String params) {
        try {
            StringTokenizer st = new StringTokenizer(params, ",");
            String urlString = st.nextToken();
            String accessKey = st.nextToken();
            String secretKey = st.nextToken();
            url = new URL(urlString);
            minioAccessKey = new MinioAccessKey(accessKey);
            minioSecretKey = new MinioSecretKey(secretKey);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
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
}
