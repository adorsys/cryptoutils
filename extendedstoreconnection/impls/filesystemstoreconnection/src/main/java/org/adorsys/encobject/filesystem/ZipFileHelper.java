package org.adorsys.encobject.filesystem;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.BucketPathUtil;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.exceptions.StorageConnectionException;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.FileSystemException;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by peter on 21.02.18 at 19:31.
 */
public class ZipFileHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipFileHelper.class);
    private static final Logger SPECIAL_LOGGER = LoggerFactory.getLogger("SPECIAL_LOGGER");
    protected static final String ZIP_STORAGE_METADATA_JSON = "StorageMetadata.json";
    protected static final String ZIP_CONTENT_BINARY = "Content.binary";
    protected static final String ZIP_SUFFIX = ".zip";
    public static final String CHARSET_NAME = "UTF-8";
    private boolean absolutePath = false;


    protected BucketDirectory baseDir;
    protected StorageMetadataFlattenerGSON gsonHelper = new StorageMetadataFlattenerGSON();

    public ZipFileHelper(BucketDirectory bucketDirectory, boolean absolutePath) {
        this.baseDir = bucketDirectory;
        this.absolutePath = absolutePath;
    }

    /**
     * https://stackoverflow.com/questions/14462371/preferred-way-to-use-java-zipoutputstream-and-bufferedoutputstream
     */
    public void writeZip(BucketPath bucketPath, SimplePayloadImpl payload) {

        ZipOutputStream zos = null;
        try {
            payload.getStorageMetadata().setType(StorageType.BLOB);
            payload.getStorageMetadata().setName(BucketPathUtil.getAsString(bucketPath));
            byte[] content = payload.getData();

            createDirectoryIfNecessary(bucketPath);
            File tempFile = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath.add(ZIP_SUFFIX).add("." + UUID.randomUUID().toString())), absolutePath);
            if (tempFile.exists()) {
                throw new StorageConnectionException("Temporary File exists. This must not happen." + tempFile);
            }
            LOGGER.debug("write temporary zip file to " + tempFile);

            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));

            zos.putNextEntry(new ZipEntry(ZIP_STORAGE_METADATA_JSON));
            String jsonString = gsonHelper.toJson(payload.getStorageMetadata());
            LOGGER.debug("WRITE metadata " + jsonString + " with charset " + CHARSET_NAME);
            byte[] storageMetadata = jsonString.getBytes(CHARSET_NAME);
            zos.write(storageMetadata, 0, storageMetadata.length);
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry(ZIP_CONTENT_BINARY));
            zos.write(content, 0, content.length);
            zos.closeEntry();

            zos.close();
            zos = null;

            File origFile = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath.add(ZIP_SUFFIX)), absolutePath);
            if (origFile.exists()) {
                LOGGER.debug("ACHTUNG, file existiert bereits, wird nun neu verlinkt " + bucketPath);
                FileUtils.forceDelete(origFile);
            }
            FileUtils.moveFile(tempFile, origFile);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (Exception e) {
                    LOGGER.error("error during close of zip output stream for file " + bucketPath);
                }
            }
        }
    }

    public void writeZipStream(BucketPath bucketPath, SimplePayloadStreamImpl payloadStream) {

        ZipOutputStream zos = null;
        InputStream is = null;
        try {
            payloadStream.getStorageMetadata().setType(StorageType.BLOB);
            payloadStream.getStorageMetadata().setName(BucketPathUtil.getAsString(bucketPath));
            String jsonString = gsonHelper.toJson(payloadStream.getStorageMetadata());
            byte[] storageMetadata = jsonString.getBytes(CHARSET_NAME);
            LOGGER.debug("WRITE metadata string " + jsonString + "with " + CHARSET_NAME);

            createDirectoryIfNecessary(bucketPath);
            File tempFile = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath.add(ZIP_SUFFIX).add("." + UUID.randomUUID().toString())), absolutePath);
            if (tempFile.exists()) {
                throw new StorageConnectionException("Temporary File exists. This must not happen." + tempFile);
            }
            LOGGER.debug("write temporary zip file to " + tempFile);

            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));

            zos.putNextEntry(new ZipEntry(ZIP_STORAGE_METADATA_JSON));
            zos.write(storageMetadata, 0, storageMetadata.length);
            zos.closeEntry();

            is = payloadStream.openStream();
            zos.putNextEntry(new ZipEntry(ZIP_CONTENT_BINARY));
            IOUtils.copy(is, zos);

            IOUtils.closeQuietly(is);
            is = null;
            IOUtils.closeQuietly(zos);
            zos = null;

            File origFile = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath.add(ZIP_SUFFIX)), absolutePath);
            if (origFile.exists()) {
                LOGGER.debug("ACHTUNG, file existiert bereits, wird nun neu verlinkt " + bucketPath);
                FileUtils.forceDelete(origFile);
            }
            FileUtils.moveFile(tempFile, origFile);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        } finally {
            if (zos != null) {
                IOUtils.closeQuietly(zos);
            }
            if (is != null) {
                IOUtils.closeQuietly(is);
            }
        }
    }


    public Payload readZip(BucketPath bucketPath, StorageMetadata storageMetadata) {
        try {
            if (storageMetadata == null) {
                storageMetadata = readZipMetadataOnly(bucketPath);
            }

            File file = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath.add(ZIP_SUFFIX)), absolutePath);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
            ZipEntry entry;
            byte[] data = null;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(ZIP_CONTENT_BINARY)) {
                    data = IOUtils.toByteArray(zis);
                }
                zis.closeEntry();
            }
            if (data == null) {
                throw new StorageConnectionException("Zipfile " + bucketPath + " does not have entry for " + ZIP_CONTENT_BINARY);
            }
            Payload payload = new SimplePayloadImpl(storageMetadata, data);
            return payload;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public PayloadStream readZipStream(BucketPath bucketPath, StorageMetadata storageMetadata) {
        try {
            if (storageMetadata == null) {
                storageMetadata = readZipMetadataOnly(bucketPath);
            }

            File file = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath.add(ZIP_SUFFIX)), absolutePath);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(ZIP_CONTENT_BINARY)) {
                    return new SimplePayloadStreamImpl(storageMetadata, zis);
                }
                zis.closeEntry();
            }
            throw new StorageConnectionException("Zipfile " + bucketPath + " does not have entry for " + ZIP_CONTENT_BINARY);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public StorageMetadata readZipMetadataOnly(BucketPath bucketPath) {
        SPECIAL_LOGGER.debug("readmetadata " + bucketPath); // Dies LogZeile ist fuer den JUNIT-Tests StorageMetaDataTest
        try {
            File file = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath.add(ZIP_SUFFIX)), absolutePath);
            if (!file.exists()) {
                throw new FileSystemException("File does not exist" + bucketPath);
            }

            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
            ZipEntry entry;
            String jsonString = null;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(ZIP_STORAGE_METADATA_JSON)) {
                    jsonString = new String(IOUtils.toByteArray(zis), CHARSET_NAME);
                    LOGGER.debug("READ metadata string " + jsonString + "with " + CHARSET_NAME);
                }
                zis.closeEntry();
            }
            if (jsonString == null) {
                throw new StorageConnectionException("Zipfile " + bucketPath + " does not have entry for " + ZIP_STORAGE_METADATA_JSON);
            }

            StorageMetadata storageMetadata = gsonHelper.fromJson(jsonString);
            return storageMetadata;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createDirectoryIfNecessary(BucketPath bucketPath) {
        File dir = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath).getBucketDirectory(), absolutePath);
        if (dir.exists()) {
            return;
        }
        boolean success = dir.mkdirs();
        if (!success) {
            throw new StorageConnectionException("cant create directory " + dir);
        }

    }


}
