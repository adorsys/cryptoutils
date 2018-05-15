package org.adorsys.cryptoutils.mongodbstoreconnection;

import com.mongodb.DB;
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
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.BucketPathUtil;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.regex;

/**
 * Created by peter on 12.03.18 at 12:32.
 */
public class ConnectToMongoTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(ConnectToMongoTest.class);
    public static final String DATABASE_NAME = "testgrid";

    // @Test
    public void createSimpleCollection() {
        LOGGER.debug("test");

        int number = 10;
        MongoClient mongoClient = new MongoClient();

        MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        MongoIterable<String> strings = database.listCollectionNames();
        for (String name : strings) {
            LOGGER.debug("name:" + name);
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
            LOGGER.debug("element:" + iterator.next());
        }

        Assert.assertEquals(number, collection.count());
    }

    // @Test
    public void createSimpleDocumentWithStream() {
        GridFSBucket bucket = createBucket("bucket");
        String filename = "file1";
        streamDocument(bucket, filename);
    }

    // @Test
    public void withoudAnyFiles() {
        GridFSBucket bucket = createBucket("bucket");
        String pattern1 = ".*";
        GridFSFindIterable gridFSFiles = bucket.find(regex("filename", pattern1, "i"));
        gridFSFiles.forEach((Consumer<GridFSFile>) file -> {
            LOGGER.debug("element " + pattern1 + ": " + file.getFilename());
        });
    }

    // @Test
    public void listDirectoryStucture() {
        int dir1 = 3;
        int dir2 = 4;
        int files = 5;
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

        {
            String pattern1 = "folder/1/*";
            GridFSFindIterable gridFSFiles = bucket.find(regex("filename", pattern1, "i"));
            List<GridFSFile> list = new ArrayList<>();
            gridFSFiles.forEach((Consumer<GridFSFile>) file -> {
                list.add(file);
                LOGGER.debug("element " + pattern1 + ": " + file.getFilename());
            });
            Assert.assertEquals(dir2 * files + files, list.size());
        }
        {
            String pattern1 = "folder/*";
            GridFSFindIterable gridFSFiles = bucket.find(regex("filename", pattern1, "i"));
            List<GridFSFile> list = new ArrayList<>();
            gridFSFiles.forEach((Consumer<GridFSFile>) file -> {
                list.add(file);
                LOGGER.debug("element " + pattern1 + ": " + file.getFilename());
            });
            Assert.assertEquals(dir1 * dir2 * files + dir1 * files + files, list.size());
        }
        {
            String pattern1 = "^folder/[^/]*$";
            GridFSFindIterable gridFSFiles = bucket.find(regex("filename", pattern1, "i"));
            List<GridFSFile> list = new ArrayList<>();
            gridFSFiles.forEach((Consumer<GridFSFile>) file -> {
                list.add(file);
                LOGGER.debug("element " + pattern1 + ": " + file.getFilename());
            });
            Assert.assertEquals(files, list.size());
        }
        {
            String pattern1 = "^[^/]*$";
            GridFSFindIterable gridFSFiles = bucket.find(regex("filename", pattern1, "i"));
            List<GridFSFile> list = new ArrayList<>();
            gridFSFiles.forEach((Consumer<GridFSFile>) file -> {
                list.add(file);
                LOGGER.debug("element- " + pattern1 + ": " + file.getFilename());
            });
            Assert.assertEquals(0, list.size());
        }
        {
            Set<String> folder = findSubdirs(new BucketDirectory("bucket/folder"));
            folder.forEach(file -> LOGGER.debug("folder -> " + file));
            Assert.assertEquals(dir1, folder.size());
        }
        {
            Set<String> folder = findSubdirs(new BucketDirectory("bucket/folder/1"));
            folder.forEach(file -> LOGGER.debug("folder -> " + file));
            Assert.assertEquals(dir2, folder.size());
        }
    }

    // @Test
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
                    LOGGER.debug("element:" + file);
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
                    LOGGER.debug("element:" + file);
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

            String value = getMetadata(DATABASE_NAME,
                    new BucketPath(bucket.getBucketName() + BucketPath.BUCKET_SEPARATOR + filename),
                    "KEY");
            LOGGER.debug("KEY ist " + value);

        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private String getMetadata(String database, BucketPath bucketPath, String key) {
        MongoClient mongoClient = new MongoClient();
        DB db = mongoClient.getDB(DATABASE_NAME);
        GridFS gridFS = new GridFS(db, bucketPath.getObjectHandle().getContainer());
        GridFSDBFile one = gridFS.findOne(bucketPath.getObjectHandle().getName());
        return (String) one.getMetaData().get(key);
    }

    private GridFSBucket createBucket(String bucketName) {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        GridFSBucket bucket = GridFSBuckets.create(database, bucketName);
        bucket.drop();
        return GridFSBuckets.create(database, bucketName);
    }

    private Set<String> findSubdirs(BucketDirectory bucketDirectory) {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        GridFSBucket bucket = GridFSBuckets.create(database, bucketDirectory.getObjectHandle().getContainer());

        String prefix = bucketDirectory.getObjectHandle().getName() + BucketPath.BUCKET_SEPARATOR;
        Set<String> dirsOnly = new HashSet<>();
        List<String> allFiles = new ArrayList<>();
        {
            // all files
            String pattern = prefix + "*";
            GridFSFindIterable gridFSFiles = bucket.find(regex("filename", pattern, "i"));
            gridFSFiles.forEach((Consumer<GridFSFile>) file -> allFiles.add(file.getFilename()));
        }
        allFiles.forEach(filename -> {
            String remainder = filename.substring(prefix.length());
            int pos = remainder.indexOf(BucketPath.BUCKET_SEPARATOR);
            if (pos != -1) {
                String dirname = remainder.substring(0, pos);
                dirsOnly.add(BucketPathUtil.getAsString(bucketDirectory.appendDirectory(dirname)));
            }
        });
        return dirsOnly;


    }
}
