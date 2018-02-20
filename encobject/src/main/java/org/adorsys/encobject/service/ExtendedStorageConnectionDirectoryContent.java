package org.adorsys.encobject.service;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 19.02.18 at 14:04.
 */
public class ExtendedStorageConnectionDirectoryContent {
    private BucketDirectory directory;
    private List<BucketPath> files = new ArrayList<>();
    private List<ExtendedStorageConnectionDirectoryContent> subidrs = new ArrayList<>();

    public ExtendedStorageConnectionDirectoryContent(BucketDirectory directory) {
        this.directory = directory;
    }

    public BucketDirectory getDirectory() {
        return directory;
    }

    public List<BucketPath> getFiles() {
        return files;
    }

    public List<ExtendedStorageConnectionDirectoryContent> getSubidrs() {
        return subidrs;
    }

    @Override
    public String toString() {
        return "ExtendedStorageConnectionDirectoryContent{" +
                "directory=" + directory.toString() +
                ", files=" + showFile(files) +
                ", subidrs=" + showDir(subidrs) +
                '}';
    }

    private static String showFile(List<BucketPath> files) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (BucketPath bp : files) {
            sb.append(bp.toString());
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }


    private static String showDir(List<ExtendedStorageConnectionDirectoryContent> dirs) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (ExtendedStorageConnectionDirectoryContent d : dirs) {
            sb.append(d.toString());
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
}
