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
    private final static String EXPECTED_PARAMS = "<host>,<port>,<databasename>[,<user>,<password>]";

    public static MongoConnectionProperties getProperties(String params) {
        LOGGER.debug("parse:" + params);
        try {
            MongoConnectionPropertiesImpl props = new MongoConnectionPropertiesImpl();

            if (params.length() > 0) {
                StringTokenizer st = new StringTokenizer(params, DELIMITER);
                props.setMongoHost(new MongoHost(st.nextToken()));
                String portString = st.nextToken();
                props.setMongoPort(new MongoPort(Long.parseLong(portString)));
                props.setMongoDatabaseName(new MongoDatabaseName(st.nextToken()));
                if (st.hasMoreTokens()) {
                    props.setMongoUser(new MongoUser(st.nextToken()));
                    props.setMongoPassword(new MongoPassword(st.nextToken()));
                }
            }
            return props;
        } catch (Exception e) {
            throw new ParamParserException(params, DELIMITER, EXPECTED_PARAMS);
        }
    }
}
