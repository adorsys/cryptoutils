package org.adorsys.encobject.filesystem;

import junit.framework.Assert;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
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
        PageSet<? extends StorageMetadata> list = s.list(new BucketDirectory(""), ListRecursiveFlag.FALSE);
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

        s.putBlob(file.getObjectHandle(), "Inhalt".getBytes());
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
        BucketDirectory bd = new BucketDirectory(container);
        BucketDirectory file = bd.appendDirectory("file1");
        s.putBlob(file.getObjectHandle(), "Inhalt".getBytes());
        PageSet<? extends StorageMetadata> list = s.list(file, ListRecursiveFlag.FALSE);
        Assert.assertEquals(0, list.size());
    }


    @Test
    public void test6() {
        String container = "affe6/1/2/3";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketDirectory bd = new BucketDirectory(container);
        s.putBlob(bd.append(new BucketPath("filea")).getObjectHandle(), "Inhalt".getBytes());
        s.putBlob(bd.append(new BucketPath("fileb")).getObjectHandle(), "Inhalt".getBytes());
        s.putBlob(bd.append(new BucketPath("subdir1/filec")).getObjectHandle(), "Inhalt".getBytes());
        s.putBlob(bd.append(new BucketPath("subdir1/filed")).getObjectHandle(), "Inhalt".getBytes());
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
        s.putBlob(bd.append(new BucketPath("subdir1/filea")).getObjectHandle(), "Inhalt".getBytes());
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
        s.putBlob(bd.append(new BucketPath("filea")).getObjectHandle(), "Inhalt".getBytes());
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
}
