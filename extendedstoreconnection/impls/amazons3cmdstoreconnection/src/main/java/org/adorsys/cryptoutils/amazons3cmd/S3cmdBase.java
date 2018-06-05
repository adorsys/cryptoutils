package org.adorsys.cryptoutils.amazons3cmd;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by peter on 05.06.18 at 16:22.
 */
public abstract class S3cmdBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(S3cmdBase.class);
    final private static String S3CMD = "/opt/s3cmd-2.0.1/s3cmd";

    static protected ExecResult exec(String... params) {
        List<String> paramList = new ArrayList<>();
        for (int i = 0; i < params.length; i++) {
            paramList.add(params[i]);
        }
        return exec(paramList);
    }

    static protected ExecResult exec(List<String> params) {
        try {
            List<String> cmds = new ArrayList<>();
            cmds.add(S3CMD);
            cmds.addAll(params);
            String[] cmdArray = cmds.stream().toArray(String[]::new);

            Process exec = Runtime.getRuntime().exec(cmdArray);

            ExecResult execResult = new ExecResult();
            {
                InputStream stdout = exec.getInputStream();
                InputStreamReader isr = new InputStreamReader(stdout);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    execResult.output.add(line);
                    LOGGER.debug("output:" + line);
                }
            }

            int exitCode = exec.waitFor();
            LOGGER.info("execution exit code:" + exitCode);
            if (exitCode == 0) {
                return execResult;
            }

            {
                StringBuilder sb = new StringBuilder();
                InputStream stderr = exec.getErrorStream();
                InputStreamReader isr = new InputStreamReader(stderr);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null)
                    LOGGER.debug("error:" + line);
                sb.append(line);
                throw new BaseException("exitcode:" + exitCode + " für " + showParams(params) + " message " + sb.toString());
            }


        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private static String showParams(List<String> params) {
        return params.stream().map(a -> o(a)).collect(Collectors.joining(","));
    }

    static public String o(String s) {
        return s.substring(0, s.length() - 1);
    }

    public static class ExecResult {
        private List<String> output = new ArrayList<>();

        public List<String> getOutput() {
            return output;
        }

        public void setOutput(List<String> output) {
            this.output = output;
        }
    }

}
