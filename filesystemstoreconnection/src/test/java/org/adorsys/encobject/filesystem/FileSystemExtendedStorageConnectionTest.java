package org.adorsys.encobject.filesystem;

import junit.framework.Assert;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.BlobMetaInfo;
import org.adorsys.encobject.domain.PageSet;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.service.ExtendedStoreConnection;
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

    @Test
    public void test1() {
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        PageSet<? extends StorageMetadata> list = s.list(new BucketDirectory("a"), ListRecursiveFlag.FALSE);
        LOGGER.debug("list" + list);
        Assert.assertEquals(0, list.size());
    }


    @Test
    public void test2() {
        String container = "affe2";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        PageSet<? extends StorageMetadata> list = s.list(new BucketDirectory(container), ListRecursiveFlag.FALSE);
        Assert.assertEquals(0, list.size());
    }

    @Test
    public void test3() {
        String container = "affe3";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketDirectory bd = new BucketDirectory(container);
        BucketPath file = bd.appendName("file1");

        s.putBlob(file, "Inhalt".getBytes());
        PageSet<? extends StorageMetadata> list = s.list(bd, ListRecursiveFlag.FALSE);
        Assert.assertEquals(1, list.size());
        LOGGER.debug("found: " + list.iterator().next().getName());
    }

    // Ein nicht existentes Directory darf keinen Fehler verursachen
    // so ist es zumindes bei der jclouldFilesystem umsetzung
    @Test
    public void test4() {
        String container = "affe4";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        BucketDirectory bd = new BucketDirectory(container);

        PageSet<? extends StorageMetadata> list = s.list(bd, ListRecursiveFlag.FALSE);
        Assert.assertEquals(0, list.size());
    }

    // Wenn als Verzeichnis eine Datei angegeben wird, dann muss eine leere Liste
    // zurückkommen, so zuindest verhält sich jcloud
    @Test
    public void test5() {
        String container = "affe5";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketPath bp = new BucketPath(container);
        BucketPath file = bp.append("file1");
        s.putBlob(file, "Inhalt".getBytes());
        BucketDirectory bd = new BucketDirectory(file);
        PageSet<? extends StorageMetadata> list = s.list(bd, ListRecursiveFlag.FALSE);
        Assert.assertEquals(0, list.size());
    }


    @Test
    public void test6() {
        String container = "affe6/1/2/3";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketDirectory bd = new BucketDirectory(container);
        s.putBlob(bd.append(new BucketPath("filea")), "Inhalt".getBytes());
        s.putBlob(bd.append(new BucketPath("fileb")), "Inhalt".getBytes());
        s.putBlob(bd.append(new BucketPath("subdir1/filec")), "Inhalt".getBytes());
        s.putBlob(bd.append(new BucketPath("subdir1/filed")), "Inhalt".getBytes());
        PageSet<? extends StorageMetadata> list = s.list(bd, ListRecursiveFlag.TRUE);
        Assert.assertEquals(4, list.size());
        list = s.list(bd, ListRecursiveFlag.FALSE);
        LOGGER.debug("List ist " + list.toString());
        Assert.assertEquals(3, list.size());
    }


    @Test
    public void test7() {
        String container = "affe7/1/2/3";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketDirectory bd = new BucketDirectory(container);
        s.putBlob(bd.append(new BucketPath("subdir1/filea")), "Inhalt".getBytes());
        PageSet<? extends StorageMetadata> list = s.list(bd, ListRecursiveFlag.TRUE);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("name has to be fullname", "1/2/3/subdir1/filea", list.iterator().next().getName());

        list = s.list(bd, ListRecursiveFlag.FALSE);
        LOGGER.debug("List ist " + list.toString());
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("name has to be fullname", "1/2/3/subdir1/", list.iterator().next().getName());
    }


    @Test
    public void test8() {
        String container = "affe8/1/2/3";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketDirectory bd = new BucketDirectory(container);
        s.putBlob(bd.append(new BucketPath("filea")), "Inhalt".getBytes());
        PageSet<? extends StorageMetadata> list = s.list(bd, ListRecursiveFlag.TRUE);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("name has to be fullname", "1/2/3/filea", list.iterator().next().getName());

        list = s.list(bd, ListRecursiveFlag.FALSE);
        LOGGER.debug("List ist " + list.toString());
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("name has to be fullname", "1/2/3/filea", list.iterator().next().getName());
    }

    @Test
    public void test9() {
        String container = "affe9/1/2/3";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketDirectory bd = new BucketDirectory(container);
        FileSystemBlobMetaInfo documentMetaInfo = new FileSystemBlobMetaInfo();
        for (int i = 0; i<10; i++) {
            documentMetaInfo.putString("key" + i, "value" + i);
        }

        BucketPath filea = bd.append(new BucketPath("filea"));
        Payload origPayload = new FileSystemPayload("Inhalt".getBytes(), documentMetaInfo);
        s.putBlob(filea, origPayload);

        Payload loadedPayload = s.getBlob(filea);

        Assert.assertEquals("document", HexUtil.convertBytesToHexString(origPayload.getData()), HexUtil.convertBytesToHexString(loadedPayload.getData()));
        Assert.assertEquals("number of metainfoentries", origPayload.getBlobMetaInfo().keySet().size(), loadedPayload.getBlobMetaInfo().keySet().size());
    }

    @Test
    public void test10() {
        String container = "affe10/1/2/3";
        // containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketDirectory bd = new BucketDirectory(container);
        FileSystemBlobMetaInfo documentMetaInfo = new FileSystemBlobMetaInfo();
        for (int i = 0; i<10; i++) {
            documentMetaInfo.putString("key" + i, "value" + i);
        }

        BucketPath filea = bd.append(new BucketPath("filea"));
        Payload origPayload = new FileSystemPayload("Inhalt".getBytes(), documentMetaInfo);
        s.putBlob(filea, origPayload);

        BlobMetaInfo blobMetaInfo  = s.getBlobMetaInfo(filea);

        Assert.assertEquals("number of metainfoentries", origPayload.getBlobMetaInfo().keySet().size(), blobMetaInfo.keySet().size());
    }

    @Test
    public void testList() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();

        BucketDirectory rootDirectory = new BucketDirectory("user1");
        createFiles(s, rootDirectory, 3,2);

        PageSet<? extends StorageMetadata> list = s.list(rootDirectory, ListRecursiveFlag.FALSE);
        LOGGER.debug("1 einfaches listing" + show(list));
        org.junit.Assert.assertEquals("nicht rekursiv erwartete Einträge", 5, list.size());

        PageSet<? extends StorageMetadata> list2 = s.list(rootDirectory, ListRecursiveFlag.TRUE);
        LOGGER.debug("2 recursives listing " + show(list2));
        org.junit.Assert.assertEquals("rekursiv erwartete Einträge", 26, list2.size());

        BucketDirectory bp = rootDirectory.appendDirectory("subdir1");
        PageSet<? extends StorageMetadata> list3 = s.list(bp, ListRecursiveFlag.FALSE);
        LOGGER.debug("3 einfaches listing " + show(list3));
        org.junit.Assert.assertEquals("rekursiv erwartete Einträge", 5, list3.size());
        org.junit.Assert.assertTrue("es gibt file", contains(list3, "subdir1/file0"));
        org.junit.Assert.assertTrue("es gibt directory", contains(list3, "subdir1/subdir0/"));

        PageSet<? extends StorageMetadata> list4 = s.list(bp, ListRecursiveFlag.TRUE);
        LOGGER.debug("4 recursives listing " + show(list4));
        org.junit.Assert.assertEquals("rekursiv erwartete Einträge", 8, list4.size());
        org.junit.Assert.assertTrue("es gibt", contains(list4, "subdir1/file0"));
        org.junit.Assert.assertTrue("es gibt directory", contains(list4, "subdir1/subdir0/file0"));
    }



    private void createFiles(ExtendedStoreConnection extendedStoreConnection, BucketDirectory rootDirectory, int subdirs, int subfiles) {
        createFilesAndFoldersRecursivly(rootDirectory, subdirs, subfiles, 3, extendedStoreConnection);
    }

    private void createFilesAndFoldersRecursivly(BucketDirectory rootDirectory, int subdirs, int subfiles, int depth , ExtendedStoreConnection extendedStoreConnection) {
        if (depth == 0) {
            return;
        }

        for (int i = 0; i<subfiles; i++) {
            byte[] content = ("Affe of file " + i + "").getBytes();
            extendedStoreConnection.putBlob(rootDirectory.appendName("file" + i), content);
        }
        for (int i = 0; i<subdirs; i++) {
            createFilesAndFoldersRecursivly(rootDirectory.appendDirectory("subdir" + i), subdirs, subfiles, depth-1, extendedStoreConnection);
        }
    }

    private boolean contains(PageSet<? extends StorageMetadata> strippedContent, String file0) {
        for (StorageMetadata m : strippedContent) {
            if (m.getName().equals(file0)){
                return true;
            }
        }
        return false;
    }

    private String show(PageSet<? extends StorageMetadata> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("PageSet of StorageMetadata");
        sb.append("\n");
        for (StorageMetadata m : list) {
            sb.append(m.getName());
            sb.append(", ");
            sb.append("\n");
        }
        return sb.toString();
    }

}
