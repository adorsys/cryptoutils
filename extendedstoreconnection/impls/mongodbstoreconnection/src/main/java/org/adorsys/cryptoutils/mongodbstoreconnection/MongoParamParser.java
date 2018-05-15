package org.adorsys.cryptoutils.mongodbstoreconnection;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;

/**
 * Created by peter on 13.04.18 at 16:19.
 */
public class MongoParamParser {
    private final static Logger LOGGER = LoggerFactory.getLogger(MongoParamParser.class);
    private String host="127.0.0.1";
    private Integer port=27017;
    private String databasename = "mongodb";

    public MongoParamParser(String params) {
        LOGGER.debug("parse:" + params);
        try {
            if (params.length() > 0) {
                StringTokenizer st = new StringTokenizer(params, "|");
                host = st.nextToken();
                String portString = st.nextToken();
                port = Integer.parseInt(portString);
                databasename = st.nextToken();
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getDatabasename() {
        return databasename;
    }
}
