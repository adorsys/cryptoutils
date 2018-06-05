package org.adorsys.cryptoutils.amazons3cmd;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 05.06.18 at 13:44.
 */
public class S3cmdTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(S3cmdTest.class);

    @Test
    public void a() {
        S3CmdListBuckets.listBuckets().forEach(bucketDirectory -> LOGGER.info(bucketDirectory.toString()));
        S3CmdCreateBucket.createBucket(new BucketDirectory("affe1"));
        S3CmdCreateBucket.createBucket(new BucketDirectory("affe2"));
        S3CmdListBuckets.listBuckets().forEach(bucketDirectory -> LOGGER.info(bucketDirectory.toString()));
    }

    public void exec(String... params) {
        List<String> paramList = new ArrayList<>();
        for (int i = 0; i < params.length; i++) {
            paramList.add(params[i]);
        }
        exec(paramList);
    }

    public void exec(List<String> params) {
        try {
            List<String> cmds = new ArrayList<>();
            cmds.add("/opt/s3cmd-2.0.1/s3cmd");
            cmds.addAll(params);
            String[] cmdArray = cmds.stream().toArray(String[]::new);

            Process exec = Runtime.getRuntime().exec(cmdArray);

            {
                InputStream stderr = exec.getErrorStream();
                InputStreamReader isr = new InputStreamReader(stderr);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null)
                    LOGGER.error(line);
            }
            {
                InputStream stdout = exec.getInputStream();
                InputStreamReader isr = new InputStreamReader(stdout);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null)
                    LOGGER.info(line);
            }

            int exitCode = exec.waitFor();
            LOGGER.info("execution exit code:" + exitCode);


        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
