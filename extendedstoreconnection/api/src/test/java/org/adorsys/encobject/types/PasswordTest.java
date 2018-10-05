package org.adorsys.encobject.types;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.junit.Test;

/**
 * Created by peter on 05.10.18.
 */
public class PasswordTest {
    @Test (expected= BaseException.class)
    public void length() {
        new BucketPathEncryptionPassword("1");
    }
    @Test (expected= BaseException.class)
    public void uppercase() {
        new BucketPathEncryptionPassword("lasdjfalsdfjsaldf");
    }
    @Test (expected= BaseException.class)
    public void lowercase() {
        new BucketPathEncryptionPassword("LASDJFALSDFJSALDF");
    }
    @Test (expected= BaseException.class)
    public void digit() {
        new BucketPathEncryptionPassword("sldkfjLJLKJdfj");
    }
    @Test (expected= BaseException.class)
    public void noSpecials() {
        new BucketPathEncryptionPassword("sldkfjLJLKJ1");
    }
    @Test
    public void specials() {
        String basestring = "lsdjfsldfjLJLJLJ12";
        for (int i = 0; i<BucketPathEncryptionPassword.specialChars.length(); i++) {
            new BucketPathEncryptionPassword(basestring + BucketPathEncryptionPassword.specialChars.charAt(i));
        }
    }
}
