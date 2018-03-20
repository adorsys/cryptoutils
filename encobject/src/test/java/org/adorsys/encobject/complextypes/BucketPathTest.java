package org.adorsys.encobject.complextypes;

import org.adorsys.encobject.exceptions.BucketException;
import org.adorsys.encobject.exceptions.BucketRestrictionException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 16.01.18.
 */
public class BucketPathTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(BucketPathTest.class);
    @Test
    public void test1() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath("aaa/b/c/d");
        Assert.assertEquals("bucket", "aaa", bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", "b/c/d", bp.getObjectHandle().getName());
    }

    @Test
    public void test2() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath("aaa");
        Assert.assertEquals("bucket", "aaa", bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", null,  bp.getObjectHandle().getName());
    }

    @Test (expected = BucketRestrictionException.class)
    public void test3() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath(null, null);
        Assert.assertEquals("bucket", null, bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", null,  bp.getObjectHandle().getName());
    }

    @Test (expected = BucketRestrictionException.class)
    public void test3a() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath(null, "");
        Assert.assertEquals("bucket", null, bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", null,  bp.getObjectHandle().getName());
    }

    @Test (expected = BucketRestrictionException.class)
    public void test3b() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath("   ", "   ");
        Assert.assertEquals("bucket", null, bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", null,  bp.getObjectHandle().getName());
    }

    @Test (expected = BucketException.class)
    public void test4() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath(null, "affe");
    }

    @Test
    public void test5() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath("affe", " / / /");
        Assert.assertEquals("bucket", "affe",  bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", null,  bp.getObjectHandle().getName());
    }

    @Test
    public void test6() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath("aaa", "/affe//und//so/");
        Assert.assertEquals("bucket", "aaa",  bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", "affe/und/so",  bp.getObjectHandle().getName());
    }

    @Test(expected = BucketException.class)
    public void test7() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath("und/", "/affe//und//so/");
    }

    @Test
    public void test8() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath("aaa", "b");
        Assert.assertEquals("bucket", "aaa",  bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", "b",  bp.getObjectHandle().getName());
    }

    @Test
    public void test9() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath("willi/affe//und//so/").append("und/nochwas");
        Assert.assertEquals("bucket", "willi",  bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", "affe/und/so/und/nochwas",  bp.getObjectHandle().getName());
    }

    @Test
    public void test10() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath("willi/affe//und//so/").append(new BucketPath("und/nochwas"));
        Assert.assertEquals("bucket", "willi",  bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", "affe/und/so/und/nochwas",  bp.getObjectHandle().getName());
    }

    @Test
    public void test11() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath("willi/affe//und//so/").append(new BucketPath("und", "/nochwas"));
        Assert.assertEquals("bucket", "willi",  bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", "affe/und/so/und/nochwas",  bp.getObjectHandle().getName());
    }

    @Test
    public void test12() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath("willi/affe//und//so/").add("affe.txt");
        Assert.assertEquals("bucket", "willi",  bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", "affe/und/soaffe.txt",  bp.getObjectHandle().getName());
    }

    @Test
    public void test13() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath("aaa", null).append("affe");
        Assert.assertEquals("bucket", "aaa",  bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", "affe",  bp.getObjectHandle().getName());
    }

    @Test (expected = BucketException.class)
    public void test14() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath((String) null);
    }

    @Test
    public void test15() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketDirectory bd = new BucketDirectory("dir1/dir2");
        Assert.assertEquals("bucket", "dir1",  bd.getObjectHandle().getContainer());
        Assert.assertEquals("name", "dir2",  bd.getObjectHandle().getName());

    }

    @Test
    public void test16() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketDirectory bd = new BucketDirectory("dir1/dir2");
        BucketDirectory bd2 = bd.appendDirectory("dir3/dir4");
        Assert.assertEquals("bucket", "dir1",  bd2.getObjectHandle().getContainer());
        Assert.assertEquals("name", "dir2/dir3/dir4",  bd2.getObjectHandle().getName());

    }

    @Test
    public void test17() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp = new BucketPath("aaa");
        BucketPath bp2 = bp.append("dir1/dir2");
        Assert.assertEquals("bucket", "aaa",  bp2.getObjectHandle().getContainer());
        Assert.assertEquals("name", "dir1/dir2",  bp2.getObjectHandle().getName());

    }

    @Test
    public void test18() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketDirectory bd = new BucketDirectory("dir1");
        BucketDirectory bd2 = bd.appendDirectory("dir2/dir3");
        Assert.assertEquals("bucket", "dir1",  bd2.getObjectHandle().getContainer());
        Assert.assertEquals("name", "dir2/dir3",  bd2.getObjectHandle().getName());

    }

    @Test
    public void test19() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketDirectory bd = new BucketDirectory("dir1");
        BucketDirectory bd2 = bd.appendDirectory("dir2/dir3");
        BucketPath bp = bd2.appendName("file1");
        Assert.assertEquals("bucket", "dir1",  bp.getObjectHandle().getContainer());
        Assert.assertEquals("name", "dir2/dir3/file1",  bp.getObjectHandle().getName());
    }

    @Test
    public void test20() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketDirectory bd = new BucketDirectory("dir1");
        BucketDirectory bd2 = bd.appendDirectory("dir2/dir3");
        BucketPath bp = bd2.addSuffix(".file1");
        Assert.assertEquals("bucket", "dir1",  bp.getObjectHandle().getContainer());
        Assert.assertEquals("name", "dir2/dir3.file1",  bp.getObjectHandle().getName());
    }

}
