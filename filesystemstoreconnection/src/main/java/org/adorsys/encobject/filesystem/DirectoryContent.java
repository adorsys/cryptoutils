package org.adorsys.encobject.filesystem;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 19.02.18 at 14:04.
 */
class DirectoryContent {
    private BucketDirectory directory;
    private List<BucketPath> files = new ArrayList<>();
    private List<DirectoryContent> subidrs = new ArrayList<>();

    public DirectoryContent(BucketDirectory directory) {
        this.directory = directory;
    }

    public BucketDirectory getDirectory() {
        return directory;
    }

    public List<BucketPath> getFiles() {
        return files;
    }

    public List<DirectoryContent> getSubidrs() {
        return subidrs;
    }

    @Override
    public String toString() {
        return "DirectoryContent{" +
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


    private static String showDir(List<DirectoryContent> dirs) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (DirectoryContent d : dirs) {
            sb.append(d.toString());
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
}
