package org.adorsys.encobject.filesystem;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.NYIException;
import org.adorsys.encobject.domain.PageSet;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by peter on 06.02.18 at 15:56.
 */
public class FileSystemPageSet<T> extends HashSet<T> implements PageSet<T>  {
    @Override
    public String getNextMarker() {
        throw new NYIException();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FileSystemPageSet{");

        Iterator<T> iterator = iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next().toString());
            sb.append(",");
        }
        sb.append("}");
        return sb.toString();
    }
}
