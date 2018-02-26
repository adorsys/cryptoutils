package org.adorsys.encobject.filesystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.Assert;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peter on 21.02.18 at 17:26.
 */
public class ZipRaceconditionTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(ZipRaceconditionTest.class);
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
     * Überschreiben der Datei und prüfen, ob die Daten wirklich überschrieben wurden
     * oder ob noch die alten gelesen werden.
     */
    @Test
    public void testOverwriteFileCompareData() {
        int size = 2;
        byte[] data1 = new byte[size];
        byte[] data2 = new byte[size];
        for (int i = 0; i<size;i++) {
            data1[i] = (byte) i;
            data2[i] = (byte) (i >> 1);
        }
        Assert.assertFalse(Arrays.equals(data1, data2));

        String container = "overwrite1";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketDirectory bd = new BucketDirectory(container);

        BucketPath filea = bd.append(new BucketPath("filea"));
        {
            Payload origPayload = new SimplePayloadImpl(data1);
            s.putBlob(filea, origPayload);
            Payload loadedPayload = s.getBlob(filea);
            LOGGER.debug("size of payload is " + loadedPayload.getStorageMetadata().getSize());
            Assert.assertTrue(Arrays.equals(data1, loadedPayload.getData()));
        }
        {
            Payload origPayload = new SimplePayloadImpl(data2);
            s.putBlob(filea, origPayload);
            Payload loadedPayload = s.getBlob(filea);
            LOGGER.debug("size of payload is " + loadedPayload.getStorageMetadata().getSize());
            Assert.assertTrue(Arrays.equals(data2, loadedPayload.getData()));
        }
    }

    /**
     * Überschreiben der Datei und prüfen, ob die StorageMetadata wirklich überschrieben wurden
     * oder ob noch die alten gelesen werden.
     */
    @Test
    public void testOverwriteFileCompareStorageMetadata() {
        int size = 2;
        byte[] data1 = new byte[size];
        byte[] data2 = new byte[size];
        for (int i = 0; i<size;i++) {
            data1[i] = (byte) i;
            data2[i] = (byte) (i >> 1);
        }
        Assert.assertFalse(Arrays.equals(data1, data2));

        String container = "overwrite2";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketDirectory bd = new BucketDirectory(container);

        BucketPath filea = bd.append(new BucketPath("filea"));
        {
            Payload origPayload = new SimplePayloadImpl(data1);
            origPayload.getStorageMetadata().getUserMetadata().put("KEY", "FIRST-USER-METADATAENTRY");
            s.putBlob(filea, origPayload);
            Payload loadedPayload = s.getBlob(filea);
            LOGGER.debug("size of payload is " + loadedPayload.getStorageMetadata().getSize());
            Assert.assertEquals(origPayload.getStorageMetadata().getUserMetadata().get("KEY"), loadedPayload.getStorageMetadata().getUserMetadata().get("KEY"));
        }
        {
            Payload origPayload = new SimplePayloadImpl(data2);
            origPayload.getStorageMetadata().getUserMetadata().put("KEY", "SECOND-USER-METADATAENTRY");
            s.putBlob(filea, origPayload);
            Payload loadedPayload = s.getBlob(filea);
            LOGGER.debug("size of payload is " + loadedPayload.getStorageMetadata().getSize());
            Assert.assertEquals(origPayload.getStorageMetadata().getUserMetadata().get("KEY"), loadedPayload.getStorageMetadata().getUserMetadata().get("KEY"));
        }
    }

    /**
     * Überschreiben einer Datei
     */
    @Test
    public void testOverwrite2() {

        String container = "affe11/.grants/";
        containers.add(container);
        ExtendedStoreConnection s = new FileSystemExtendedStorageConnection();
        s.createContainer(container);
        BucketDirectory bd = new BucketDirectory(container);
        BucketPath filea = bd.append(new BucketPath("grantfile"));
        {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            GrantList grantList = new GrantList();
            grantList.put("peter", RIGHT.READ);
            StorageMetadata storageMetadata = new SimpleStorageMetadataImpl();
            storageMetadata.getUserMetadata().put("myinfo", "first time");
            String jsonString = gson.toJson(grantList);
            LOGGER.debug("write 1 " + jsonString);
            Payload origPayload = new SimplePayloadImpl(storageMetadata, jsonString.getBytes());
            s.putBlob(filea, origPayload);
        }
        {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Payload payload = s.getBlob(filea);
            String jsonString = new String(payload.getData());
            LOGGER.debug("read 1 " + jsonString);
            GrantList grantList = gson.fromJson(jsonString, GrantList.class);
            Assert.assertEquals(RIGHT.READ, grantList.get("peter"));
        }
        {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            GrantList grantList = new GrantList();
            grantList.put("peter", RIGHT.WRITE);
            StorageMetadata storageMetadata = new SimpleStorageMetadataImpl();
            storageMetadata.getUserMetadata().put("myinfo", "second time");
            String jsonString = gson.toJson(grantList);
            LOGGER.debug("write 2 " + jsonString);
            Payload origPayload = new SimplePayloadImpl(storageMetadata, jsonString.getBytes());
            s.putBlob(filea, origPayload);
        }
        {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Payload payload = s.getBlob(filea);
            String jsonString = new String(payload.getData());
            LOGGER.debug("read 2 " + jsonString);
            GrantList grantList = gson.fromJson(jsonString, GrantList.class);
            Assert.assertEquals(RIGHT.WRITE, grantList.get("peter"));
        }
    }

    static enum RIGHT {
        READ,
        WRITE;
    }

    static class GrantList {
        Map<String, RIGHT> userRights = new HashMap<>();

        public void put(String user, RIGHT right) {
            userRights.put(user, right);
        }

        public RIGHT get(String user) {
            if (userRights.containsKey(user)) {
                return userRights.get(user);
            }
            throw new BaseException("user " + user + " not found");
        }
    }

}
