package org.adorsys.encobject.filesystem;

import junit.framework.Assert;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.service.ExtendedStorageConnectionDirectoryContent;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.service.SimplePayloadImpl;
import org.adorsys.encobject.service.SimpleStorageMetadataImpl;
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
public class FileSystemExtendedStorageConnectionTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(FileSystemExtendedStorageConnectionTest.class);
    private List<String> containers = new ArrayList<>();

    @Before
    public void before() {
        containers.clear();
    }

    @After
    public void after() {
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        for (String c : containers) {
            try {
                LOGGER.debug("AFTER TEST DELETE CONTAINER " + c);
                s.deleteContainer(c);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * Suche in einem nicht vorhandenem Bucket sollte einfach eine leere Liste zurückgeben
     */
    @Test
    public void testList1() {
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        List<StorageMetadata> content = s.list(new BucketDirectory("a"), ListRecursiveFlag.FALSE);
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
        String container = "affe2";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        List<StorageMetadata> content = s.list(new BucketDirectory(container), ListRecursiveFlag.FALSE);
        List<BucketPath> files = getFilesOnly(content);
        Assert.assertEquals(0, files.size());
        List<BucketDirectory> dirs = getDirectoresOnly(content);
        Assert.assertEquals(1, dirs.size());
    }

    /**
     * Liste einer Datei sollte genau diese mit zurückliefern
     */
    @Test
    public void testList3() {
        String container = "affe3";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketDirectory bd = new BucketDirectory(container);
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
        String container = "affe4";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        BucketDirectory bd = new BucketDirectory(container);

        List<StorageMetadata> content = s.list(bd, ListRecursiveFlag.FALSE);
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
        String container = "affe5";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketPath bp = new BucketPath(container);
        BucketPath file = bp.append("file1");
        s.putBlob(file, "Inhalt".getBytes());
        BucketDirectory bd = new BucketDirectory(file);
        List<StorageMetadata> content = s.list(bd, ListRecursiveFlag.FALSE);
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
        String container = "affe6/1/2/3";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketDirectory bd = new BucketDirectory(container);
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
        String container = "affe7/1/2/3";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketDirectory bd = new BucketDirectory(container);
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
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();

        BucketDirectory rootDirectory = new BucketDirectory("user1");
        createFiles(s, rootDirectory, 3, 2);

        {
            List<StorageMetadata> list = s.list(rootDirectory, ListRecursiveFlag.FALSE);
            LOGGER.debug("1 einfaches listing");
            LOGGER.debug(show(list));
            List<BucketPath> files = getFilesOnly(list);
            List<BucketDirectory> dirs = getDirectoresOnly(list);

            Assert.assertEquals(4, dirs.size());
            Assert.assertEquals(2, files.size());
        }
        {
            List<StorageMetadata> list = s.list(rootDirectory, ListRecursiveFlag.TRUE);
            LOGGER.debug("2 recursives listing");
            LOGGER.debug(show(list));
            List<BucketPath> files = getFilesOnly(list);
            List<BucketDirectory> dirs = getDirectoresOnly(list);

            Assert.assertEquals(13, dirs.size());
            Assert.assertEquals(26, files.size());
        }

        {
            BucketDirectory bp = rootDirectory.appendDirectory("subdir1");
            List<StorageMetadata> list = s.list(bp, ListRecursiveFlag.FALSE);
            LOGGER.debug("3 einfaches listing");
            LOGGER.debug(show(list));
            List<BucketPath> files = getFilesOnly(list);
            List<BucketDirectory> dirs = getDirectoresOnly(list);

            Assert.assertEquals(4, dirs.size());
            Assert.assertEquals(2, files.size());
        }

        {
            BucketDirectory bp = rootDirectory.appendDirectory("subdir1");
            List<StorageMetadata> list = s.list(bp, ListRecursiveFlag.TRUE);
            LOGGER.debug("4 recursives listing");
            LOGGER.debug(show(list));
            List<BucketPath> files = getFilesOnly(list);
            List<BucketDirectory> dirs = getDirectoresOnly(list);

            Assert.assertEquals(4, dirs.size());
            Assert.assertEquals(8, files.size());
        }
    }

    /**
     * Laden der StorageMetaData über Payload
     */
    @Test
    public void testStorageMetaData1() {
        String container = "affe9/1/2/3";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketDirectory bd = new BucketDirectory(container);
        StorageMetadata storageMetadata = new SimpleStorageMetadataImpl();
        for (int i = 0; i < 10; i++) {
            storageMetadata.getUserMetadata().put("key" + i, "value" + i);
        }

        BucketPath filea = bd.append(new BucketPath("filea"));
        Payload origPayload = new SimplePayloadImpl(storageMetadata, "Inhalt".getBytes());
        s.putBlob(filea, origPayload);

        Payload loadedPayload = s.getBlob(filea);

        Assert.assertEquals("document", HexUtil.convertBytesToHexString(origPayload.getData()), HexUtil.convertBytesToHexString(loadedPayload.getData()));
        Assert.assertEquals("number of metainfoentries", origPayload.getStorageMetadata().getUserMetadata().keySet().size(), loadedPayload.getStorageMetadata().getUserMetadata().keySet().size());
    }

    /**
     * Laden der StorageMetaData direkt
     */
    @Test
    public void testStorageMetaData2() {
        String container = "affe10/1/2/3";
        // containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketDirectory bd = new BucketDirectory(container);
        StorageMetadata storageMetadata = new SimpleStorageMetadataImpl();
        for (int i = 0; i < 10; i++) {
            storageMetadata.getUserMetadata().put("key" + i, "value" + i);
        }

        BucketPath filea = bd.append(new BucketPath("filea"));
        Payload origPayload = new SimplePayloadImpl(storageMetadata, "Inhalt".getBytes());
        s.putBlob(filea, origPayload);

        StorageMetadata loadedStorageMetadata = s.getStorageMetadata(filea);

        Assert.assertEquals("number of metainfoentries", origPayload.getStorageMetadata().getUserMetadata().keySet().size(), loadedStorageMetadata.getUserMetadata().keySet().size());
    }

    /* =========================================================================================================== */

    private boolean contains(List<ExtendedStorageConnectionDirectoryContent> subidrs, BucketDirectory
            bucketDirectory) {
        for (ExtendedStorageConnectionDirectoryContent content : subidrs) {
            LOGGER.debug(content.getDirectory().toString());
            if (content.getDirectory().equals(bucketDirectory)) {
                return true;
            }
        }
        return false;
    }


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
