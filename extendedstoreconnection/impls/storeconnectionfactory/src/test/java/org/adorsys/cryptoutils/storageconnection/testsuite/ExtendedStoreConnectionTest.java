package org.adorsys.cryptoutils.storageconnection.testsuite;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.extendendstoreconnection.impl.amazons3.AmazonS3ExtendedStoreConnection;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.exceptions.StorageConnectionException;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by peter on 06.02.18 at 16:45.
 */
public class ExtendedStoreConnectionTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(ExtendedStoreConnectionTest.class);
    private List<BucketDirectory> containers = new ArrayList<>();
    private ExtendedStoreConnection s = ExtendedStoreConnectionFactory.get();

    @Before
    public void before() {
        containers.clear();
    }

    @After
    public void after() {
        for (BucketDirectory c : containers) {
            try {
                LOGGER.debug("AFTER TEST DELETE CONTAINER " + c);
                s.deleteContainer(c);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Test
    public void cleanDB() {
        ExtendedStoreConnection c = ExtendedStoreConnectionFactory.get();
        c.listAllBuckets().forEach(el -> c.deleteContainer(el));
    }

    /*
    This test requrires manual access
     */
    // @Test
    public void testConnectionAvaiable() {
        cleanDB();
        BucketDirectory bd = new BucketDirectory("test-container-exists");
        containers.add(bd);

        Assert.assertFalse(s.containerExists(bd));
        s.createContainer(bd);
        try {
            LOGGER.info("you have 10 secs to kill the connection");
            Thread.currentThread().sleep(10000);
            LOGGER.info("continue");
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

        Assert.assertTrue(s.containerExists(bd));
        containers.add(bd);

        BucketPath file = bd.appendName("file.txt");
        Assert.assertFalse(s.blobExists(file));

        byte[] filecontent = "Inhalt".getBytes();
        s.putBlob(file, new SimplePayloadImpl(filecontent));

        try {
            LOGGER.info("you have 10 secs to kill the connection");
            Thread.currentThread().sleep(10000);
            LOGGER.info("continue");
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

        Assert.assertTrue(s.blobExists(file));

    }

    @Test
    public void testListSubfolderRecursive() {
        BucketDirectory bd = new BucketDirectory("test_list_subfolder");
        s.createContainer(bd);
        containers.add(bd);

        BucketPath file = bd.appendName("/empty1/empty2/file.txt");

        byte[] filecontent = "Inhalt".getBytes();
        s.putBlob(file, new SimplePayloadImpl(filecontent));

        {
            List<StorageMetadata> content = s.list(bd, ListRecursiveFlag.TRUE);
            LOGGER.debug(show(content));
            List<BucketPath> files = getFilesOnly(content);
            Assert.assertEquals(1, files.size());
            List<BucketDirectory> dirs = getDirectoresOnly(content);
            Assert.assertEquals(3, dirs.size());
            Assert.assertTrue(s.blobExists(file));
        }

        {
            List<StorageMetadata> content = s.list(bd.appendDirectory("empty1"), ListRecursiveFlag.TRUE);
            LOGGER.debug(show(content));
            List<BucketPath> files = getFilesOnly(content);
            Assert.assertEquals(1, files.size());
            List<BucketDirectory> dirs = getDirectoresOnly(content);
            Assert.assertEquals(2, dirs.size());
            Assert.assertTrue(s.blobExists(file));
        }

        {
            List<StorageMetadata> content = s.list(bd.appendDirectory("empty1/empty2"), ListRecursiveFlag.TRUE);
            LOGGER.debug(show(content));
            List<BucketPath> files = getFilesOnly(content);
            Assert.assertEquals(1, files.size());
            List<BucketDirectory> dirs = getDirectoresOnly(content);
            Assert.assertEquals(1, dirs.size());
            Assert.assertTrue(s.blobExists(file));
        }
    }

    @Test
    public void testListSubfolderNonRecursive() {
        BucketDirectory bd = new BucketDirectory("test_list_subfolder");
        s.createContainer(bd);
        containers.add(bd);

        BucketPath file = bd.appendName("/empty1/empty2/file.txt");

        byte[] filecontent = "Inhalt".getBytes();
        s.putBlob(file, new SimplePayloadImpl(filecontent));

        {
            List<StorageMetadata> content = s.list(bd, ListRecursiveFlag.FALSE);
            LOGGER.debug(show(content));
            List<BucketPath> files = getFilesOnly(content);
            Assert.assertEquals(0, files.size());
            List<BucketDirectory> dirs = getDirectoresOnly(content);
            Assert.assertEquals(2, dirs.size());
            Assert.assertTrue(s.blobExists(file));
        }
        {
            List<StorageMetadata> content = s.list(bd.appendDirectory("empty1"), ListRecursiveFlag.FALSE);
            LOGGER.debug(show(content));
            List<BucketPath> files = getFilesOnly(content);
            Assert.assertEquals(0, files.size());
            List<BucketDirectory> dirs = getDirectoresOnly(content);
            Assert.assertEquals(2, dirs.size());
            Assert.assertTrue(s.blobExists(file));
        }
        {
            List<StorageMetadata> content = s.list(bd.appendDirectory("empty1/empty2"), ListRecursiveFlag.FALSE);
            LOGGER.debug(show(content));
            List<BucketPath> files = getFilesOnly(content);
            Assert.assertEquals(1, files.size());
            List<BucketDirectory> dirs = getDirectoresOnly(content);
            Assert.assertEquals(1, dirs.size());
            Assert.assertTrue(s.blobExists(file));
        }
    }

    /**
     * Suche in einem nicht vorhandenem Bucket sollte einfach eine leere Liste zurückgeben
     */
    @Test
    public void testList1() {
        List<StorageMetadata> content = s.list(new BucketDirectory("abc"), ListRecursiveFlag.FALSE);
        List<BucketPath> files = getFilesOnly(content);
        Assert.assertEquals(0, files.size());
        List<BucketDirectory> dirs = getDirectoresOnly(content);
        Assert.assertEquals(0, dirs.size());
    }

    /**
     * Liste eines echten Containers sollte genau ein Directory zurückliefern
     */
    @Test
    public void testList2() {
        BucketDirectory bd = new BucketDirectory("affe2");
        s.createContainer(bd);
        containers.add(bd);

        List<StorageMetadata> content = s.list(bd, ListRecursiveFlag.FALSE);
        LOGGER.debug(show(content));
        List<BucketPath> files = getFilesOnly(content);
        Assert.assertEquals(0, files.size());
        List<BucketDirectory> dirs = getDirectoresOnly(content);
        dirs.forEach(dir -> LOGGER.debug(dir.toString()));
        Assert.assertEquals(1, dirs.size());
    }

    /**
     * Liste einer Datei sollte genau diese mit zurückliefern
     */
    @Test
    public void testList3() {
        int REPEATS = 10;
        int i = 0;

        while (i > 0) {
            LOGGER.info("wait for visualVM profiler " + i);
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception e) {
            }
            i--;
        }
        BucketDirectory bd = new BucketDirectory("affe3");
        s.createContainer(bd);
        containers.add(bd);
        for (int j = 0; j < REPEATS; j++) {
            BucketPath file = bd.appendName("file1");
            if (s.blobExists(file)) {
                s.removeBlob(file);
            }

            byte[] filecontent = "Inhalt".getBytes();
            s.putBlob(file, filecontent);

            List<StorageMetadata> content = s.list(bd, ListRecursiveFlag.FALSE);
            List<BucketPath> files = getFilesOnly(content);
            Assert.assertEquals(1, files.size());
            List<BucketDirectory> dirs = getDirectoresOnly(content);
            Assert.assertEquals(1, dirs.size());
            Assert.assertTrue(s.blobExists(file));

            Payload loadedPayload = s.getBlob(file);
            byte[] loadedFileContent = loadedPayload.getData();
            Assert.assertTrue(Arrays.equals(filecontent, loadedFileContent));
        }
    }

    /**
     * Kein Unterverzeichnis, nur der Bucket.
     * Ein nicht existentes Directory darf keinen Fehler verursachen
     * so ist es zumindes bei der jclouldFilesystem umsetzung
     */
    @Test
    public void testList4() {
        BucketDirectory bd = new BucketDirectory("affe4");

        List<StorageMetadata> content = s.list(bd, ListRecursiveFlag.FALSE);
        content.forEach(c -> LOGGER.debug(c.getName()));
        List<BucketPath> files = getFilesOnly(content);
        Assert.assertEquals(0, files.size());
        List<BucketDirectory> dirs = getDirectoresOnly(content);
        Assert.assertEquals(0, dirs.size());
    }

    /**
     * Wenn als Verzeichnis eine Datei angegeben wird, dann muss eine leere Liste
     * zurückkommen, so zuindest verhält sich jcloud
     */
    @Test
    public void testList5() {
        BucketDirectory bd = new BucketDirectory("affe5");
        s.createContainer(bd);
        containers.add(bd);

        BucketPath file = bd.appendName("file1");
        s.putBlob(file, "Inhalt".getBytes());
        BucketDirectory bdtrick = new BucketDirectory(file);
        List<StorageMetadata> content = s.list(bdtrick, ListRecursiveFlag.FALSE);
        List<BucketPath> files = getFilesOnly(content);
        Assert.assertEquals(0, files.size());
        List<BucketDirectory> dirs = getDirectoresOnly(content);
        Assert.assertEquals(0, dirs.size());
    }


    /**
     * bei recursiver Suche muss alles gefunden werden, bei nicht rekursiver nur das
     * aktuelle Verzeichnis
     */
    @Test
    public void testList6() {
        BucketDirectory bd = new BucketDirectory("affe6/1/2/3");
        s.createContainer(bd);
        containers.add(bd);

        s.putBlob(bd.append(new BucketPath("filea")), "Inhalt".getBytes());
        s.putBlob(bd.append(new BucketPath("fileb")), "Inhalt".getBytes());
        s.putBlob(bd.append(new BucketPath("subdir1/filec")), "Inhalt".getBytes());
        s.putBlob(bd.append(new BucketPath("subdir1/filed")), "Inhalt".getBytes());
        List<StorageMetadata> content = s.list(bd, ListRecursiveFlag.TRUE);
        LOGGER.debug("recursive " + show(content));
        Assert.assertEquals("Anzahl Einträge", 6, content.size());
        {
            List<BucketPath> files = getFilesOnly(content);
            Assert.assertEquals(4, files.size());
            List<BucketDirectory> dirs = getDirectoresOnly(content);
            Assert.assertEquals(2, dirs.size());
        }


        content = s.list(bd, ListRecursiveFlag.FALSE);
        LOGGER.debug("plain " + show(content));
        Assert.assertEquals("Anzahl Einträge", 4, content.size());
        {
            List<BucketPath> files = getFilesOnly(content);
            Assert.assertEquals(2, files.size());
            List<BucketDirectory> dirs = getDirectoresOnly(content);
            Assert.assertEquals(2, dirs.size());
        }
    }

    /**
     * Nun mit Prüfung, dass auch wirklich die vorhandenen Dateien gefunden werden
     */
    @Test
    public void testList7() {
        BucketDirectory bd = new BucketDirectory("affe7/1/2/3");
        s.createContainer(bd);
        containers.add(bd);

        s.putBlob(bd.append(new BucketPath("subdir1/filea")), "Inhalt".getBytes());
        List<StorageMetadata> list = s.list(bd, ListRecursiveFlag.TRUE);
        LOGGER.debug(show(list));
        List<BucketPath> files = getFilesOnly(list);
        List<BucketDirectory> dirs = getDirectoresOnly(list);
        Assert.assertTrue(files.contains(new BucketPath("affe7/1/2/3/subdir1/filea")));
        Assert.assertEquals(3, list.size());
        Assert.assertEquals(1, files.size());
        Assert.assertEquals(2, dirs.size());

        list = s.list(bd, ListRecursiveFlag.FALSE);
        LOGGER.debug(show(list));
        files = getFilesOnly(list);
        dirs = getDirectoresOnly(list);
        Assert.assertTrue(dirs.contains(new BucketDirectory("affe7/1/2/3")));
        Assert.assertTrue(dirs.contains(new BucketDirectory("affe7/1/2/3/subdir1")));
        Assert.assertEquals(2, dirs.size());
        Assert.assertEquals(0, files.size());
    }


    @Test
    public void deleteDatabase() {
        if (s instanceof AmazonS3ExtendedStoreConnection) {
            ((AmazonS3ExtendedStoreConnection) s).cleanDatabase();
        }
    }

    @Test
    public void testDeleteFolder() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketDirectory bd = new BucketDirectory("deletedeep");
        s.createContainer(bd);
        containers.add(bd);

        /**
         * Anlegen einer tieferen Verzeichnisstruktur
         */
        createFilesAndFoldersRecursivly(bd, 2, 2, 5, s);

        if (s instanceof AmazonS3ExtendedStoreConnection) {
            ((AmazonS3ExtendedStoreConnection) s).showDatabase();
        }

        List<StorageMetadata> listAll = s.list(bd, ListRecursiveFlag.TRUE);
        List<BucketPath> filesOnlyAll = getFilesOnly(listAll);
        LOGGER.debug("number of all files under " + bd + " is " + filesOnlyAll.size());

        BucketDirectory bd00 = bd.appendDirectory("subdir0/subdir0");
        List<StorageMetadata> list00 = s.list(bd00, ListRecursiveFlag.TRUE);
        List<BucketPath> filesOnly00 = getFilesOnly(list00);
        LOGGER.debug("number of files under " + bd00 + " is " + filesOnly00.size());

        s.removeBlobFolder(bd00);

        List<StorageMetadata> listAllNew = s.list(bd, ListRecursiveFlag.TRUE);
        List<BucketPath> filesOnlyAllNew = getFilesOnly(listAllNew);
        LOGGER.debug("number of all files under " + bd + " is " + filesOnlyAllNew.size());

        Assert.assertEquals(filesOnlyAllNew.size() + filesOnly00.size(), filesOnlyAll.size());

        if (s instanceof AmazonS3ExtendedStoreConnection) {
            ((AmazonS3ExtendedStoreConnection) s).cleanDatabase();
        }
    }

    /**
     * Anlegen einer tieferen Verzeichnisstruktur
     */
    @Test
    public void testList8() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());

        BucketDirectory bd = new BucketDirectory("bucket8");
        s.createContainer(bd);
        containers.add(bd);

        createFiles(s, bd, 3, 2);
        {
            LOGGER.debug("test8 start subtest 1");
            List<StorageMetadata> list = s.list(bd, ListRecursiveFlag.FALSE);
            LOGGER.debug("1 einfaches listing");
            LOGGER.debug(show(list));
            List<BucketPath> files = getFilesOnly(list);
            List<BucketDirectory> dirs = getDirectoresOnly(list);

            Assert.assertEquals(4, dirs.size());
            Assert.assertEquals(2, files.size());
        }
        {
            LOGGER.debug("test8 start subtest 2");
            List<StorageMetadata> list = s.list(bd, ListRecursiveFlag.TRUE);
            LOGGER.debug("2 recursives listing");
            LOGGER.debug(show(list));
            List<BucketPath> files = getFilesOnly(list);
            List<BucketDirectory> dirs = getDirectoresOnly(list);

            Assert.assertEquals(13, dirs.size());
            Assert.assertEquals(26, files.size());
        }

        {
            LOGGER.debug("test8 start subtest 3");
            BucketDirectory bp = bd.appendDirectory("subdir1");
            List<StorageMetadata> list = s.list(bp, ListRecursiveFlag.FALSE);
            LOGGER.debug("3 einfaches listing");
            LOGGER.debug(show(list));
            List<BucketPath> files = getFilesOnly(list);
            List<BucketDirectory> dirs = getDirectoresOnly(list);

            Assert.assertEquals(4, dirs.size());
            Assert.assertEquals(2, files.size());
        }

        {
            LOGGER.debug("test8 start subtest 4");
            BucketDirectory bp = bd.appendDirectory("subdir1");
            List<StorageMetadata> list = s.list(bp, ListRecursiveFlag.TRUE);
            LOGGER.debug("4 recursives listing");
            LOGGER.debug(show(list));
            List<BucketPath> files = getFilesOnly(list);
            List<BucketDirectory> dirs = getDirectoresOnly(list);

            Assert.assertEquals(4, dirs.size());
            Assert.assertEquals(8, files.size());
        }

        {
            LOGGER.debug("test8 start subtest 5");
            BucketDirectory subdirectory1 = bd.appendDirectory("subdir1");
            s.removeBlobFolder(subdirectory1);
            List<StorageMetadata> list = s.list(bd, ListRecursiveFlag.TRUE);
            LOGGER.debug("5 recursives listing");
            LOGGER.debug(show(list));
            List<BucketPath> files = getFilesOnly(list);
            List<BucketDirectory> dirs = getDirectoresOnly(list);

            Assert.assertEquals(9, dirs.size());
            Assert.assertEquals(18, files.size());
        }
        {
            LOGGER.debug("test8 start subtest 6");
            s.list(bd, ListRecursiveFlag.TRUE).forEach(el -> LOGGER.debug("found. " + el.getName() + " " + el.getType()));
            Assert.assertFalse(s.blobExists(bd.appendDirectory("subdir1").appendName("file1")));
            Assert.assertTrue(s.blobExists(bd.appendDirectory("subdir2").appendName("file1")));
            Assert.assertFalse(s.blobExists(bd.appendDirectory("subdir2").appendName("file9")));
        }

        {
            // Extra nicht mit expected Annotation, damit diese Exception niecht vorher schon zum
            // Abbruch anderern Tests führt
            boolean testOk = false;
            try {
                s.removeBlobFolder(bd);
            } catch (StorageConnectionException e) {
                testOk = true;
            }
            Assert.assertTrue(testOk);
        }

    }

    /**
     * Überschreiben einer Datei
     */
    @Test
    public void testOverwrite() {
        BucketDirectory bd = new BucketDirectory("bucketoverwrite/1/2/3");
        s.createContainer(bd);
        containers.add(bd);

        StorageMetadata storageMetadata = new SimpleStorageMetadataImpl();
        storageMetadata.getUserMetadata().put("myinfo", "first time");
        BucketPath filea = bd.append(new BucketPath("filea"));
        Payload origPayload = new SimplePayloadImpl(storageMetadata, "1".getBytes());
        s.putBlob(filea, origPayload);
        Payload payload = s.getBlob(filea);
        Assert.assertEquals("1", new String(payload.getData()));
        LOGGER.debug("ok, inhalt nach dem ersten Schreiben ok");
        Payload newPayload = new SimplePayloadImpl(storageMetadata, "2".getBytes());
        s.putBlob(filea, newPayload);
        Assert.assertEquals("2", new String(newPayload.getData()));
        LOGGER.debug("ok, inhalt nach dem zweiten Schreiben auch ok");
    }

    @Test
    public void testListAllBuckets() {
        cleanDB();

        List<BucketDirectory> mybuckets = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            BucketDirectory bd = new BucketDirectory("bucket" + i);
            mybuckets.add(bd);
            containers.add(bd);
            s.createContainer(bd);

            StorageMetadata storageMetadata = new SimpleStorageMetadataImpl();
            storageMetadata.getUserMetadata().put("myinfo", "first time");
            Payload origPayload = new SimplePayloadImpl(storageMetadata, "1".getBytes());
            BucketPath file1 = bd.append(new BucketPath("dir1/file1"));
            s.putBlob(file1, origPayload);
            BucketPath file2 = bd.append(new BucketPath("dir1/file2"));
            s.putBlob(file2, origPayload);

        }
        List<BucketDirectory> foundBuckets = s.listAllBuckets();
        mybuckets.forEach(b -> LOGGER.debug("created bucket " + b));
        foundBuckets.forEach(b -> LOGGER.debug("found bucket " + b));
        Assert.assertTrue(foundBuckets.containsAll(mybuckets));
        Assert.assertTrue(mybuckets.containsAll(foundBuckets));
    }

    @Test
    public void testFileExists() {
        BucketDirectory bd = new BucketDirectory("bucketfileexiststest");
        s.createContainer(bd);
        containers.add(bd);

        StorageMetadata storageMetadata = new SimpleStorageMetadataImpl();
        storageMetadata.getUserMetadata().put("simpleinfo", "any value");
        BucketPath filea = bd.append(new BucketPath("file1"));
        Assert.assertFalse(s.blobExists(filea));
        Payload origPayload = new SimplePayloadImpl(storageMetadata, "1".getBytes());
        s.putBlob(filea, origPayload);
        Assert.assertTrue(s.blobExists(filea));
        s.removeBlob(filea);
        Assert.assertFalse(s.blobExists(filea));
    }

    @Test
    public void createBucketWithDotAndTestFileForDir() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bucketPath = new BucketPath("user1/.hidden/Affenfile.txt");
        byte[] documentContent = "Affe".getBytes();
        s.createContainer(bucketPath.getBucketDirectory());
        s.putBlob(bucketPath, new SimplePayloadImpl(new SimpleStorageMetadataImpl(), documentContent));
        BucketDirectory bd = new BucketDirectory(bucketPath);
        LOGGER.debug("bucketPath " + bucketPath);
        LOGGER.debug("pathAsDir  " + bd);
        List<StorageMetadata> list = s.list(bd, ListRecursiveFlag.TRUE);
        list.forEach(el -> LOGGER.debug("found " + el.getName() + " " + el.getType()));
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void destroyBucketTwice() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bucketPath = new BucketPath("user1/.hidden/Affenfile.txt");
        byte[] documentContent = "Affe".getBytes();
        s.createContainer(bucketPath.getBucketDirectory());
        s.putBlob(bucketPath, new SimplePayloadImpl(new SimpleStorageMetadataImpl(), documentContent));
        s.deleteContainer(bucketPath.getBucketDirectory());
        s.deleteContainer(bucketPath.getBucketDirectory());
    }


    @Test
    public void createManyBuckets() {
        for (int i = 0; i < 200; i++) {
            BucketDirectory bd = new BucketDirectory("bucket" + i);
            containers.add(bd);
            s.createContainer(bd);
        }
    }


   /* =========================================================================================================== */

    private void createFiles(ExtendedStoreConnection extendedStoreConnection, BucketDirectory rootDirectory,
                             int subdirs, int subfiles) {
        createFilesAndFoldersRecursivly(rootDirectory, subdirs, subfiles, 3, extendedStoreConnection);
    }

    private void createFilesAndFoldersRecursivly(BucketDirectory rootDirectory, int subdirs, int subfiles,
                                                 int depth, ExtendedStoreConnection extendedStoreConnection) {
        if (depth == 0) {
            return;
        }

        for (int i = 0; i < subfiles; i++) {
            byte[] content = ("Affe of file " + i + "").getBytes();
            extendedStoreConnection.putBlob(rootDirectory.appendName("file" + i), content);
        }
        for (int i = 0; i < subdirs; i++) {
            createFilesAndFoldersRecursivly(rootDirectory.appendDirectory("subdir" + i), subdirs, subfiles, depth - 1, extendedStoreConnection);
        }
    }

    private String show(List<StorageMetadata> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("List (" + list.size() + ")");
        sb.append("\n");
        for (StorageMetadata m : list) {
            sb.append("( ");
            sb.append(m.getName());
            sb.append(" ");
            sb.append(m.getType());
            sb.append(" ");
            sb.append(m.getSize());
            sb.append(" ");
            sb.append(") ");
            sb.append("\n");
        }
        return sb.toString();
    }

    private String showBucketPath(List<BucketPath> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("List of BucketPath");
        sb.append("\n");
        for (BucketPath m : list) {
            sb.append(m.toString());
            sb.append(", ");
            sb.append("\n");
        }
        return sb.toString();
    }

    private List<BucketPath> getFilesOnly(List<StorageMetadata> a) {
        List<BucketPath> result = new ArrayList<>();
        for (StorageMetadata s : a) {
            if (s.getType().equals(StorageType.BLOB)) {
                result.add(new BucketPath(s.getName()));
            }
        }
        return result;
    }


    private List<BucketDirectory> getDirectoresOnly(List<StorageMetadata> a) {
        List<BucketDirectory> result = new ArrayList<>();
        for (StorageMetadata s : a) {
            if (s.getType().equals(StorageType.FOLDER)) {
                result.add(new BucketDirectory(s.getName()));
            }
        }
        return result;
    }

}
