package org.adorsys.cryptoutils.storageconnection.testsuite;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Location;
import org.adorsys.encobject.domain.LocationScope;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.filesystem.StorageMetadataFlattenerGSON;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimpleLocationImpl;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import junit.framework.Assert;

/**
 * Created by peter on 20.02.18 at 16:53.
 */
public class StorageMetaDataTest {
    private static String logfilename = "target/storeconnectionfactory-test-log-file.log";

    private final static Logger LOGGER = LoggerFactory.getLogger(StorageMetaDataTest.class);
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

    /**
     * Laden der StorageMetaData direkt
     */
    @Test
    public void testStorageMetaData() {
        BucketDirectory bd = new BucketDirectory("storagemetadata/1");
        s.createContainer(bd);
        containers.add(bd);
        StorageMetadata storageMetadata = createStorageMetadata();
        BucketPath filea = bd.append(new BucketPath("filea"));
        Payload origPayload = new SimplePayloadImpl(storageMetadata, "Inhalt".getBytes());
        s.putBlob(filea, origPayload);

        StorageMetadata loadedStorageMetadata = s.getStorageMetadata(filea);
        int fehler = compareStorageMetadata(storageMetadata, loadedStorageMetadata);

        LOGGER.debug("Es werden drei Fehler erwartet für Name, StorageType");
        Assert.assertEquals("number of fehlers", 2, fehler);

        ((SimpleStorageMetadataImpl) storageMetadata).setName(loadedStorageMetadata.getName());
        Assert.assertFalse(storageMetadata.equals(loadedStorageMetadata));
        ((SimpleStorageMetadataImpl) storageMetadata).setType(loadedStorageMetadata.getType());
        Assert.assertTrue(storageMetadata.equals(loadedStorageMetadata));
    }


    @Test
    public void jsonTest() {
        StorageMetadataFlattenerGSON gsonHelper = new StorageMetadataFlattenerGSON();
        StorageMetadata storageMetadata = createStorageMetadata();
        String jsonString = gsonHelper.toJson(storageMetadata);
        LOGGER.debug(jsonString);
        StorageMetadata reloadedStorageMetadata = gsonHelper.fromJson(jsonString);
        int fehler = compareStorageMetadata(storageMetadata, reloadedStorageMetadata);
        Assert.assertEquals("number of fehler", 0, fehler);
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

        BucketDirectory bd = new BucketDirectory("overwrite1");
        s.createContainer(bd);
        containers.add(bd);

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

        BucketDirectory bd = new BucketDirectory("overwrite2");
        s.createContainer(bd);
        containers.add(bd);

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

        BucketDirectory bd = new BucketDirectory("affe11/.grants/");
        containers.add(bd);
        s.createContainer(bd);
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

    /**
     * Achtung, dieser Test möchte sicherstellen, dass in Cryptoutils die Methode zum Lesen der StorageMetadata
     * wirklich nur einmal aufgerufen wird. Um das zu machen, wird einfach eine spezielle Logmeldung gesucht, die
     * nach dem Test genau einmal mehr geschrieben sein muss, als vor dem Test. Daher wird für diesen Test
     * logback benötigt, denn der Simpple-Logger schreibt nicht in Dateien und auf stdout gleichzeitig
     */
    @Test
    public void testCheckMetaInfoOnlyReadOnceForDocument() {
        try {
            LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
            String searchname = UUID.randomUUID().toString();
            BucketPath bucketPath = new BucketPath("first/next/" + searchname);
            waitUntilLogfileisSynched(logfilename);
            int count1 = countReadMetaData(logfilename, searchname);
            StorageMetadata storageMetadata = new SimpleStorageMetadataImpl();
            byte[] documentContent = "Einfach nur a bisserl Text".getBytes();
            Payload payload = new SimplePayloadImpl(storageMetadata, documentContent);
            s.createContainer(bucketPath.getBucketDirectory());
            containers.add(bucketPath.getBucketDirectory());
            s.putBlob(bucketPath, payload);
            StorageMetadata storageMetadata1 = s.getStorageMetadata(bucketPath);
            s.getBlob(bucketPath, storageMetadata1);
            waitUntilLogfileisSynched(logfilename);
            int count2 = countReadMetaData(logfilename, searchname);
            org.junit.Assert.assertEquals(count1 + 1, count2);
            LOGGER.debug("found " + count2 + " lines :-)");
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
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


    public static final String PATTERN = "yyyy-MM-dd-HH:mm:ss";
    public static final String NAME_VALUE = "Name is Peter";
    public static final String STORAGE_TYPE = "FOLDER";
    public static final String PROVIDER_ID_VALUE = "Die ProviderID 1";
    public static final String LOCATION_ID_0 = "LocationID0";
    public static final String LOCATION_0_DESCRIPTION = "any description of parent location";
    public static final String LOCATION_ID_1 = "LocationID1";
    public static final String LOCATION_1_DESCRIPTION = "any description of this location";
    public static final String LOCATION_0_SCOPE = "HOST";
    public static final String LOCATION_1_SCOPE = "SYSTEM";
    public static final String URI_VALUE = "www.electronicpeter.de";
    public static final String JSON_STRING_ELEMENT = "{\"quote\":\"VALUE\"} ";
    public static final String ETAG_VALUE = "DAs legendäre ETAG";
    public static final String CREATION_DATE = "2018-02-20-14:54:18";
    public static final String MODIFIED_DATE = "2018-02-20-14:54:41";
    public static final String ISO3166_CODE = "Iso3166Code";
    public static final int NUMBER_OF_ISOCODECS = 1;
    public static final int NUMBER_OF_KEY_VALUE_PAIRS = 1;
    public static final String SHOULD_BE_COMPRESSED = "true";
    public static final String CONTENT_TYPE = "application/xml";


    private StorageMetadata createStorageMetadata() {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);
            SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
            // ResourceMetadata
            {
                storageMetadata.setName(NAME_VALUE);
                storageMetadata.setType(StorageType.valueOf(STORAGE_TYPE));
                storageMetadata.setProviderID(PROVIDER_ID_VALUE);
                {
                    SimpleLocationImpl parentLocation = new SimpleLocationImpl();
                    parentLocation.setId(LOCATION_ID_0);
                    parentLocation.setDescription(LOCATION_0_DESCRIPTION);
                    parentLocation.setScope(LocationScope.valueOf(LOCATION_0_SCOPE));

                    SimpleLocationImpl location = new SimpleLocationImpl();
                    location.setId(LOCATION_ID_1);
                    location.setDescription(LOCATION_1_DESCRIPTION);
                    location.setScope(LocationScope.valueOf(LOCATION_1_SCOPE));
                    location.setParent(parentLocation);

                    for (int i = 0; i < NUMBER_OF_ISOCODECS; i++) {
                        location.getIso3166Codes().add(ISO3166_CODE + i);
                    }
                    storageMetadata.setLocation(location);


                }
                storageMetadata.setUri(java.net.URI.create(URI_VALUE));
                for (int i = 0; i < NUMBER_OF_KEY_VALUE_PAIRS; i++) {
                    storageMetadata.getUserMetadata().put("key_" + i, JSON_STRING_ELEMENT);
                }
            }
            // StorageMetaData
            storageMetadata.setETag(ETAG_VALUE);
            storageMetadata.setCreationDate(sdf.parse(CREATION_DATE));
            storageMetadata.setLastModified(sdf.parse(MODIFIED_DATE));
            storageMetadata.setSize(new Long(1111));
            storageMetadata.setContentType(CONTENT_TYPE);
            storageMetadata.setShouldBeCompressed(new Boolean(SHOULD_BE_COMPRESSED));
            return storageMetadata;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private int compareStorageMetadata(StorageMetadata m1, StorageMetadata m2) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);
            // ResourceMetadata
            int fehler = 0;
            fehler += compareStrings(m1.getName(), m2.getName(), NAME_VALUE, "Name");
            {
                StorageType storageType1 = m1.getType();
                StorageType storageType2 = m2.getType();
                if (!storageType1.equals(storageType2) || !storageType1.equals(StorageType.valueOf(STORAGE_TYPE))) {
                    LOGGER.debug("StorageType nicht korrekt");
                    fehler += 1;
                }
            }
            fehler += compareStrings(m1.getProviderID(), m2.getProviderID(), PROVIDER_ID_VALUE, "ProviderID");
            {
                {
                    Location l1 = m1.getLocation();
                    Location l2 = m2.getLocation();
                    {
                        {
                            LocationScope lsc1 = l1.getScope();
                            LocationScope lsc2 = l2.getScope();
                            if (!lsc1.equals(lsc2) || !lsc1.equals(LocationScope.valueOf(LOCATION_1_SCOPE))) {
                                LOGGER.debug("Location Scope nicht korrekt");
                                fehler += 1;
                            }
                        }
                        fehler += compareStrings(l1.getID(), l2.getID(), LOCATION_ID_1, "Location ID");
                        fehler += compareStrings(l1.getDescription(), l2.getDescription(), LOCATION_1_DESCRIPTION, "Location Description");
                        {
                            if (l1.getIso3166Codes().size() != l2.getIso3166Codes().size() || l1.getIso3166Codes().size() != NUMBER_OF_ISOCODECS) {
                                LOGGER.debug("Anzahl der Iso4166 Codecs in Location falsch: " + l1.getIso3166Codes().size() + " "
                                        + l2.getIso3166Codes().size() + " "
                                        + NUMBER_OF_ISOCODECS);
                                fehler += 1;
                            }
                        }
                    }
                    {
                        l1 = l1.getParent();
                        l2 = l2.getParent();
                        if (l2 == null) {
                            LOGGER.debug("Parent Location ist nicht gesetzt.");
                            fehler += 1;
                        } else {
                            {
                                LocationScope lsc1 = l1.getScope();
                                LocationScope lsc2 = l2.getScope();
                                if (!lsc1.equals(lsc2) || !lsc1.equals(LocationScope.valueOf(LOCATION_0_SCOPE))) {
                                    LOGGER.debug("Parent Location Scope nicht korrekt");
                                    fehler += 1;
                                }
                            }
                            fehler += compareStrings(l1.getID(), l2.getID(), LOCATION_ID_0, "Parent Location ID");
                            fehler += compareStrings(l1.getDescription(), l2.getDescription(), LOCATION_0_DESCRIPTION, "Parent Location Description");
                            {
                                if (l1.getIso3166Codes().size() != l2.getIso3166Codes().size() || l1.getIso3166Codes().size() != 0) {
                                    LOGGER.debug("Anzahl der Iso4166 Codecs in Parent Location falsch: " + l1.getIso3166Codes().size() + " "
                                            + l2.getIso3166Codes().size() + " "
                                            + 0);
                                    fehler += 1;
                                }
                            }
                        }
                    }
                }
            }
            {
                URI r1 = m1.getUri();
                URI r2 = m2.getUri();
                URI expected = URI.create(URI_VALUE);
                if (!r1.equals(r2) || !r1.equals(expected)) {
                    LOGGER.debug("URI nicht korrekt " + r1 + " " + r2 + " " + expected);
                    fehler += 1;
                }
            }
            {
                if (m1.getUserMetadata().keySet().size() != m2.getUserMetadata().keySet().size() || m1.getUserMetadata().keySet().size() != NUMBER_OF_KEY_VALUE_PAIRS) {
                    LOGGER.debug("User MetaData Anzahl Element stimmt nicht " + m1.getUserMetadata().keySet().size() + " "
                            + m2.getUserMetadata().keySet().size() + " " + 10);
                    fehler += 1;
                } else {
                    String key = m1.getUserMetadata().keySet().iterator().next();
                    fehler += compareStrings(m1.getUserMetadata().get(key), m2.getUserMetadata().get(key), JSON_STRING_ELEMENT, "UserMetadata Value for key " + key);
                }

            }
            fehler += compareStrings(m1.getETag(), m2.getETag(), ETAG_VALUE, "ETag");
            fehler += compareStrings(sdf.format(m1.getCreationDate()), sdf.format(m2.getCreationDate()), CREATION_DATE, "Creation Date");
            fehler += compareStrings(sdf.format(m1.getLastModified()), sdf.format(m2.getLastModified()), MODIFIED_DATE, "Creation Date");
            fehler += compareStrings(m1.getSize().toString(), m2.getSize().toString(), "1111", "Size");
            fehler += compareStrings(m1.getShouldBeCompressed().toString(), m2.getShouldBeCompressed().toString(), SHOULD_BE_COMPRESSED, "should be compressed");
            fehler += compareStrings(m1.getContentType(), m2.getContentType(), CONTENT_TYPE, "content type");

            if (fehler > 0) {
                Assert.assertTrue(!m1.equals(m2));
            } else {
                Assert.assertTrue(m1.equals(m2));
            }


            return fehler;
        } catch (
                Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private int compareStrings(String v1, String v2, String expected, String description) {
        if (!v1.equals(v2) || !v1.equals(expected)) {
            LOGGER.debug(description + " nicht korrekt: v1(" + v1 + ") v2(" + v2 + ") expected(" + expected + ")");
            return 1;
        }
        return 0;
    }

    private void waitUntilLogfileisSynched(String logfilename) {
        try {
            if (!new File(logfilename).exists()) {
                throw new BaseException("logfile " + logfilename + " not found. I am in "
                        + new java.io.File(".").getCanonicalPath()
                        + ". This tests requires the logfilefile to succeed.");
            }
            int MAX_WAIT = 10;
            int trials = 0;
            String unique = UUID.randomUUID().toString();
            int count = 0;

            LOGGER.debug(unique);
            do {
                if (trials > MAX_WAIT) {
                    throw new BaseException("Did not find unique entry in logfile "
                            + new java.io.File(".").getCanonicalPath() + "/" + logfilename
                            + " for " + MAX_WAIT + " seconds.");
                }
                Thread.currentThread().sleep(1000);
                
                // Charset defined in logback.xml
                count = Files.lines(Paths.get(logfilename), Charset.forName("UTF-8"))
                        .filter(line -> line.indexOf(unique) != -1)
                        .collect(Collectors.toSet())
                        .size();
                trials++;
            } while (count != 1);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private int countReadMetaData(String logfilename, String searchname) {
        // Seit die Pfade verschluesselt werden, ist es nicht mehr moeglich nach dem namen zu suchen.
        // In der Annahme, dass die Tests nicht parallel laufen, reicht es, nur das tag zu suchen
        try {
            if (!new File(logfilename).exists()) {
                throw new BaseException("logfile " + logfilename + " not found. I am in "
                        + new java.io.File(".").getCanonicalPath()
                        + "This tests requires the logfilefile to succeed.");
            }
            // Charset defined in logback.xml
            return Files.lines(Paths.get(logfilename), Charset.forName("UTF-8"))
                    .filter(line -> line.indexOf("SPECIAL_LOGGER") != -1)
                    .filter(line -> line.indexOf("readmetadata ") != -1)
             //       .filter(line -> line.indexOf(searchname) != -1)
                    .collect(Collectors.toSet())
                    .size();
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


}
