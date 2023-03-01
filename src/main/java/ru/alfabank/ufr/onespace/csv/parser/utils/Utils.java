package ru.alfabank.ufr.onespace.csv.parser.utils;

import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static final String STANDARD_ENCODING = "windows-1251";

    public static String convertListToString(List<String> items) {
        return items.stream().map(Objects::toString).collect(
              Collectors.joining(System.getProperty("line.separator")));
    }

    public static InputStream convertStringToInputStream(String content,
          String encoding) {
        return new ByteArrayInputStream(
              content.getBytes(Charset.forName(encoding)));
    }

    public static String processFileEncoding(InputStream file)
          throws IOException {
        UniversalDetector detector = new UniversalDetector(null);
        byte[] buf = new byte[4096];

        int nread;
        while ((nread = file.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }

        detector.dataEnd();

        String encoding = detector.getDetectedCharset();
        if (encoding != null) {
            logger.info("Detected encoding {}", encoding);

        } else {
            encoding = STANDARD_ENCODING;
            logger.info("No encoding detected. Standard encoding is {}",
                  encoding);
        }

        detector.reset();
        return encoding;
    }

}
