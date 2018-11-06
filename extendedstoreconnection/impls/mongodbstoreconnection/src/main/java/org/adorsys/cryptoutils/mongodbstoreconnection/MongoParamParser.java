package org.adorsys.cryptoutils.mongodbstoreconnection;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.exceptions.ParamParserException;
import org.adorsys.encobject.types.connection.*;
import org.adorsys.encobject.types.properties.MongoConnectionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;

/**
 * Created by peter on 13.04.18 at 16:19.
 */
public class MongoParamParser {
    private final static Logger LOGGER = LoggerFactory.getLogger(MongoParamParser.class);
    private final static String DELIMITER = ",";
    public final static String EXPECTED_PARAMS = "<mongoClientUri> (mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database.collection][?options]]) - see http://mongodb.github.io/mongo-java-driver/3.6/javadoc/com/mongodb/MongoClientURI.html";

    public static MongoConnectionProperties getProperties(String params) {
        LOGGER.debug("parse:" + params);
        try {
            MongoConnectionPropertiesImpl props = new MongoConnectionPropertiesImpl();

            if (params.length() > 0) {
                props.setMongoURI(new MongoURI(params));
            }
            return props;
        } catch (Exception e) {
            throw new ParamParserException(params, DELIMITER, EXPECTED_PARAMS);
        }
    }
}
