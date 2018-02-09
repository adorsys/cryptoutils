package org.adorsys.encobject.complextypes;

import org.adorsys.encobject.exceptions.BucketException;
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
        BucketPath bp=new BucketPath("a/b/c/d");
        Assert.assertEquals("bucket", "a", bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", "b/c/d", bp.getObjectHandle().getName());
    }

    @Test
    public void test2() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath("a");
        Assert.assertEquals("bucket", "a", bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", null,  bp.getObjectHandle().getName());
    }

    @Test
    public void test3() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath(null, null);
        Assert.assertEquals("bucket", null,  bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", null,  bp.getObjectHandle().getName());
    }

    @Test
    public void test4() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath(null, "affe");
        Assert.assertEquals("bucket", null,  bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", "affe",  bp.getObjectHandle().getName());
    }

    @Test
    public void test5() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath(null, "/affe/");
        Assert.assertEquals("bucket", null,  bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", "affe",  bp.getObjectHandle().getName());
    }

    @Test
    public void test6() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath(null, "/affe//und//so/");
        Assert.assertEquals("bucket", null,  bp.getObjectHandle().getContainer());
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
        BucketPath bp=new BucketPath("a", "b");
        Assert.assertEquals("bucket", "a",  bp.getObjectHandle().getContainer());
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
        BucketPath bp=new BucketPath("willi/affe//und//so/").append(new BucketPath(null, "und/nochwas"));
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
        BucketPath bp=new BucketPath("a", null).append("affe");
        Assert.assertEquals("bucket", "a",  bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", "affe",  bp.getObjectHandle().getName());
    }

    @Test
    public void test14() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketPath bp=new BucketPath((String) null);
        Assert.assertEquals("bucket", null,  bp.getObjectHandle().getContainer());
        Assert.assertEquals("name  ", null,  bp.getObjectHandle().getName());
    }

}
