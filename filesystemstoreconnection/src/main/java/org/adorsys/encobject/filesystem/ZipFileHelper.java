package org.adorsys.encobject.filesystem;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.BucketPathUtil;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.exceptions.StorageConnectionException;
import org.adorsys.encobject.service.SimplePayloadImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.FileSystemException;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by peter on 21.02.18 at 19:31.
 */
public class ZipFileHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipFileHelper.class);
    private static final String ZIP_STORAGE_METADATA_JSON = "StorageMetadata.json";
    private static final String ZIP_CONTENT_BINARY = "Content.binary";
    public static final String ZIP_SUFFIX = ".zip";


    private BucketDirectory baseDir;
    private StorageMetadataFlattenerGSON gsonHelper = new StorageMetadataFlattenerGSON();
    
    public ZipFileHelper(BucketDirectory bucketDirectory) {
        this.baseDir = bucketDirectory;
    } 
        
    /**
     * https://stackoverflow.com/questions/14462371/preferred-way-to-use-java-zipoutputstream-and-bufferedoutputstream
     */
    public void writeZip(BucketPath bucketPath, Payload payload) {
        payload.getStorageMetadata().setType(StorageType.BLOB);
        payload.getStorageMetadata().setName(BucketPathUtil.getAsString(bucketPath));
        byte[] storageMetadata = gsonHelper.toJson(payload.getStorageMetadata()).getBytes();
        byte[] content = payload.getData();

        ZipOutputStream zos = null;
        try {
            createDirectoryIfNecessary(bucketPath);
            File tempFile = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath.add(ZIP_SUFFIX).add("." + UUID.randomUUID().toString())));
            if (tempFile.exists()) {
                throw new StorageConnectionException("Temporary File exists. This must not happen." + tempFile);
            }
            LOGGER.debug("write temporary zip file to " + tempFile);

            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));

            zos.putNextEntry(new ZipEntry(ZIP_STORAGE_METADATA_JSON));
            zos.write(storageMetadata, 0, storageMetadata.length);
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry(ZIP_CONTENT_BINARY));
            zos.write(content, 0, content.length);
            zos.closeEntry();

            zos.close();
            zos = null;

            File origFile = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath.add(ZIP_SUFFIX)));
            if (origFile.exists()) {
                LOGGER.info("ACHTUNG, file existiert bereits, wird nun neu verlinkt " + bucketPath);
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

    public Payload readZip(BucketPath bucketPath) {
        try {
            StorageMetadata storageMetadata = readZipMetadataOnly(bucketPath);

            File file = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath.add(ZIP_SUFFIX)));
            ZipFile zipFile = new ZipFile(file, ZipFile.OPEN_READ);
            ZipEntry entry = zipFile.getEntry(ZIP_CONTENT_BINARY);
            Payload payload = new SimplePayloadImpl(storageMetadata, IOUtils.toByteArray(zipFile.getInputStream(entry)));
            return payload;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public StorageMetadata readZipMetadataOnly(BucketPath bucketPath) {
        try {
            File file = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath.add(ZIP_SUFFIX)));
            if (!file.exists()) {
                throw new FileSystemException("File does not exist" + bucketPath);
            }
            ZipFile zipFile = new ZipFile(file, ZipFile.OPEN_READ);
            ZipEntry entry = zipFile.getEntry(ZIP_STORAGE_METADATA_JSON);
            if (entry == null) {
                throw new StorageConnectionException("Zipfile " + bucketPath + " does not have entry for " + ZIP_STORAGE_METADATA_JSON);
            }

            return gsonHelper.fromJson(new String(IOUtils.toByteArray(zipFile.getInputStream(entry))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createDirectoryIfNecessary(BucketPath bucketPath) {
        File dir = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath).getBucketDirectory());
        if (dir.exists()) {
            return;
        }
        boolean success = dir.mkdirs();
        if (!success) {
            throw new StorageConnectionException("cant create directory " + dir);
        }

    }


}
