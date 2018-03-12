package org.adorsys.cryptoutils.mongodbstorageconnection;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import static com.mongodb.client.model.Filters.regex;

/**
 * Created by peter on 12.03.18 at 12:32.
 */
public class connectToMongoDBTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(connectToMongoDBTest.class);

    @Test
    public void a() {
        LOGGER.info("test");

        MongoClient mongoClient = new MongoClient();

        MongoDatabase database = mongoClient.getDatabase("test");
        MongoIterable<String> strings = database.listCollectionNames();
        for (String name : strings) {
            LOGGER.info("name:" + name);
        }
        MongoCollection<Document> collection = database.getCollection("animals");
        collection.drop();
        for (int i = 0; i < 10; i++) {
            Document d = new Document();
            d.append("name", "affe");
            d.append("id", i);
            collection.insertOne(d);
        }

        collection = database.getCollection("animals");
        LOGGER.debug("size of collections: " + collection.count());
        MongoCursor<Document> iterator = collection.find().iterator();
        while (iterator.hasNext()) {
            LOGGER.info("element:" + iterator.next());
        }
    }

    @Test
    public void b() {
        String filename = "file1";
        streamDocument(filename);
    }

    @Test
    public void c() {
        for (int i = 0; i<3; i++) {
            for (int j = 0; j<3; j++) {
                for (int k = 0; k < 3; k++) {
                    String filename = "folder/" + i + "/" + j + "/file" + k;
                    streamDocument(filename);
                }
            }
        }

        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("testgrid");
        GridFSBucket bucket = GridFSBuckets.create(database, "bucket");

        String pattern = "folder/1/.*";
        GridFSFindIterable gridFSFiles = bucket.find(regex("filename", pattern, "i"));
        MongoCursor<GridFSFile> iterator = gridFSFiles.iterator();
        while (iterator.hasNext()) {
            LOGGER.info("element:" + iterator.next());
        }


    }



    private void streamDocument(String filename) {
        try {
            String content = "Ein Affe ist ein Affe und das bleibt auch so";
            InputStream is = new ByteArrayInputStream(content.getBytes());
            MongoClient mongoClient = new MongoClient();
            MongoDatabase database = mongoClient.getDatabase("testgrid");
            GridFSBucket bucket = GridFSBuckets.create(database, "bucket");
            bucket.uploadFromStream(filename, is);

            GridFSDownloadStream file1Stream = bucket.openDownloadStream(filename);
            byte[] bytes = IOUtils.toByteArray(file1Stream);
            Assert.assertTrue(Arrays.equals(content.getBytes(), bytes));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


}
