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
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.apache.commons.io.IOUtils;
import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.regex;

/**
 * Created by peter on 12.03.18 at 12:32.
 */
public class connectToMongoDBTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(connectToMongoDBTest.class);

    @Test
    public void createSimpleCollection() {
        LOGGER.info("test");

        int number = 10;
        MongoClient mongoClient = new MongoClient();

        MongoDatabase database = mongoClient.getDatabase("test");
        MongoIterable<String> strings = database.listCollectionNames();
        for (String name : strings) {
            LOGGER.info("name:" + name);
        }
        MongoCollection<Document> collection = database.getCollection("animals");
        collection.drop();
        for (int i = 0; i < number; i++) {
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

        Assert.assertEquals(number, collection.count());
    }

    @Test
    public void createSimpleDocumentWithStream() {
        GridFSBucket bucket = createBucket("bucket");
        String filename = "file1";
        streamDocument(bucket, filename);
    }

    @Test
    public void listDirectoryStucture() {
        int dir1 = 3;
        int dir2 = 3;
        int files = 3;
        GridFSBucket bucket = createBucket("bucket");
        for (int i = 0; i < dir1; i++) {
            for (int j = 0; j < dir2; j++) {
                for (int k = 0; k < files; k++) {
                    String filename = "folder/" + i + "/" + j + "/file" + k;
                    streamDocument(bucket, filename);
                }
            }
        }
        for (int k = 0; k < files; k++) {
            String filename = "folder/file" + k;
            streamDocument(bucket, filename);
        }
        for (int i = 0; i < dir1; i++) {
            for (int k = 0; k < files; k++) {
                String filename = "folder/" + i + "/file" + k;
                streamDocument(bucket, filename);
            }
        }

        String pattern1 = "folder/1/.*";
        GridFSFindIterable gridFSFiles = bucket.find(regex("filename", pattern1, "i"));
        MongoCursor<GridFSFile> iterator = gridFSFiles.iterator();
        while (iterator.hasNext()) {
            GridFSFile file = iterator.next();
            LOGGER.info("element:" + file);
        }
        final List<Integer> list = new ArrayList<>();
        gridFSFiles.forEach((Consumer<GridFSFile>) file -> list.add(new Integer(1)));
        LOGGER.debug(pattern1 + " -> " + list.size());
        Assert.assertEquals(dir2 * files + files, list.size());

        list.clear();
        String pattern2 = "folder/.*";
        gridFSFiles = bucket.find(regex("filename", pattern2, "i"));
        gridFSFiles.forEach((Consumer<GridFSFile>) file -> list.add(new Integer(1)));
        LOGGER.debug(pattern2 + " -> " + list.size());
        Assert.assertEquals(dir1 * dir2 * files  + dir1*files + files, list.size());
    }

    @Test
    public void testStreamVersions() {
        try {
            {
                String filename = "affe";
                GridFSBucket bucket = createBucket("bucket");
                GridFSUploadOptions uploadOptions = new GridFSUploadOptions();
                uploadOptions.metadata(new Document());
                uploadOptions.getMetadata().put("KEY", new Date().toString());
                String content = "Ein Affe ist ein Affe und das bleibt auch so";
                InputStream is = new ByteArrayInputStream(content.getBytes());
                bucket.uploadFromStream(filename, is, uploadOptions);

                GridFSDownloadStream file1Stream = bucket.openDownloadStream(filename);
                byte[] bytes = IOUtils.toByteArray(file1Stream);
                Assert.assertTrue(Arrays.equals(content.getBytes(), bytes));

                String content2 = "Ein anderer Affe ist ein anderer Affe und das bleibt auch so";
                is = new ByteArrayInputStream(content2.getBytes());
                bucket.uploadFromStream(filename, is, uploadOptions);

                file1Stream = bucket.openDownloadStream(filename);
                bytes = IOUtils.toByteArray(file1Stream);
                Assert.assertTrue(Arrays.equals(content2.getBytes(), bytes));

                GridFSFindIterable files = bucket.find(regex("filename", filename));
                MongoCursor<GridFSFile> iterator = files.iterator();
                int counter = 0;
                while (iterator.hasNext()) {
                    GridFSFile file = iterator.next();
                    LOGGER.info("element:" + file);
                    counter++;
                }
                Assert.assertEquals(2, counter);
            }
            {
                String filename = "affe2";
                GridFSBucket bucket = createBucket("bucket");
                List<ObjectId> idsToDelete = new ArrayList<>();
                {
                    String pattern2 = filename;
                    GridFSFindIterable gridFSFiles = bucket.find(Filters.eq("filename", pattern2));
                    gridFSFiles.forEach((Consumer<GridFSFile>) file -> idsToDelete.add(file.getObjectId()));
                    if (idsToDelete.size() > 1) {
                        throw new BaseException("das darf nicht sein, ist aber so....");
                    }
                    LOGGER.debug("ids to delete:" + idsToDelete.size());

                }
                GridFSUploadOptions uploadOptions = new GridFSUploadOptions();
                uploadOptions.metadata(new Document());
                uploadOptions.getMetadata().put("KEY", new Date().toString());
                String content = "Ein Affe ist ein Affe und das bleibt auch so";
                InputStream is = new ByteArrayInputStream(content.getBytes());

                bucket.uploadFromStream(filename, is, uploadOptions);
                idsToDelete.forEach(id -> bucket.delete(id));
                idsToDelete.clear();

                GridFSDownloadStream file1Stream = bucket.openDownloadStream(filename);
                byte[] bytes = IOUtils.toByteArray(file1Stream);
                Assert.assertTrue(Arrays.equals(content.getBytes(), bytes));

                String content2 = "Ein anderer Affe ist ein anderer Affe und das bleibt auch so";
                is = new ByteArrayInputStream(content2.getBytes());
                {
                    String pattern2 = filename;
                    GridFSFindIterable gridFSFiles = bucket.find(Filters.eq("filename", pattern2));
                    gridFSFiles.forEach((Consumer<GridFSFile>) file -> idsToDelete.add(file.getObjectId()));
                    if (idsToDelete.size() > 1) {
                        throw new BaseException("das darf nicht sein, ist aber so....");
                    }
                    LOGGER.debug("ids to delete:" + idsToDelete.size());

                }
                bucket.uploadFromStream(filename, is, uploadOptions);
                idsToDelete.forEach(id -> bucket.delete(id));
                idsToDelete.clear();

                file1Stream = bucket.openDownloadStream(filename);
                bytes = IOUtils.toByteArray(file1Stream);
                Assert.assertTrue(Arrays.equals(content2.getBytes(), bytes));

                GridFSFindIterable files = bucket.find(regex("filename", filename));
                MongoCursor<GridFSFile> iterator = files.iterator();
                int counter = 0;
                while (iterator.hasNext()) {
                    GridFSFile file = iterator.next();
                    LOGGER.info("element:" + file);
                    counter++;
                }
                Assert.assertEquals(1, counter);
            }


        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private void streamDocument(GridFSBucket bucket, String filename) {
        try {
            GridFSUploadOptions uploadOptions = new GridFSUploadOptions();
            uploadOptions.metadata(new Document());
            uploadOptions.getMetadata().put("KEY", new Date().toString());
            String content = "Ein Affe ist ein Affe und das bleibt auch so";
            InputStream is = new ByteArrayInputStream(content.getBytes());
            bucket.uploadFromStream(filename, is, uploadOptions);

            GridFSDownloadStream file1Stream = bucket.openDownloadStream(filename);
            byte[] bytes = IOUtils.toByteArray(file1Stream);
            Assert.assertTrue(Arrays.equals(content.getBytes(), bytes));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private GridFSBucket createBucket(String bucketName) {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("testgrid");
        GridFSBucket bucket = GridFSBuckets.create(database, bucketName);
        bucket.drop();
        return GridFSBuckets.create(database, bucketName);
    }

}
