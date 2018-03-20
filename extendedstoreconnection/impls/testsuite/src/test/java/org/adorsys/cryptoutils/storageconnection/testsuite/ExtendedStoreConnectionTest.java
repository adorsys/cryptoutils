package org.adorsys.cryptoutils.storageconnection.testsuite;

import junit.framework.Assert;
import org.adorsys.cryptoutils.miniostoreconnection.MinioExtendedStoreConnection;
import org.adorsys.cryptoutils.mongodbstoreconnection.MongoDBExtendedStoreConnection;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.exceptions.StorageConnectionException;
import org.adorsys.encobject.filesystem.FileSystemExtendedStorageConnection;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

    // @Test
    public void cleanMinioDB() {
        ExtendedStoreConnection storeConnection = ExtendedStoreConnectionFactory.get();
        if (storeConnection instanceof MinioExtendedStoreConnection) {
            MinioExtendedStoreConnection minio = (MinioExtendedStoreConnection) storeConnection;
            minio.deleteAllBuckets();
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
        BucketDirectory bd = new BucketDirectory("affe3");
        s.createContainer(bd);
        containers.add(bd);
        BucketPath file = bd.appendName("file1");

        s.putBlob(file, "Inhalt".getBytes());

        List<StorageMetadata> content = s.list(bd, ListRecursiveFlag.FALSE);
        List<BucketPath> files = getFilesOnly(content);
        Assert.assertEquals(1, files.size());
        List<BucketDirectory> dirs = getDirectoresOnly(content);
        Assert.assertEquals(1, dirs.size());
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
        LOGGER.debug("plain " + content.toString());
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
            List<StorageMetadata> list = s.list(bd, ListRecursiveFlag.FALSE);
            LOGGER.debug("1 einfaches listing");
            LOGGER.debug(show(list));
            List<BucketPath> files = getFilesOnly(list);
            List<BucketDirectory> dirs = getDirectoresOnly(list);

            Assert.assertEquals(4, dirs.size());
            Assert.assertEquals(2, files.size());
        }
        {
            List<StorageMetadata> list = s.list(bd, ListRecursiveFlag.TRUE);
            LOGGER.debug("2 recursives listing");
            LOGGER.debug(show(list));
            List<BucketPath> files = getFilesOnly(list);
            List<BucketDirectory> dirs = getDirectoresOnly(list);

            Assert.assertEquals(13, dirs.size());
            Assert.assertEquals(26, files.size());
        }

        {
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
        LOGGER.info("ok, inhalt nach dem ersten Schreiben ok");
        Payload newPayload = new SimplePayloadImpl(storageMetadata, "2".getBytes());
        s.putBlob(filea, newPayload);
        Assert.assertEquals("2", new String(newPayload.getData()));
        LOGGER.info("ok, inhalt nach dem zweiten Schreiben auch ok");
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
        sb.append("List of StorageMetadata");
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
