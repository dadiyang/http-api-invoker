package com.github.dadiyang.httpinvoker.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * IO Utils
 *
 * @author dadiyang
 * @since 2019-06-12
 */
public class IoUtils {
    private static final Logger logger = LoggerFactory.getLogger(IoUtils.class);
    private static final String CLASSPATH_PRE = "classpath:";
    private static final String FILE_PRE = "file:";

    private IoUtils() {
        throw new UnsupportedOperationException("utils should not be initialized!");
    }

    public static void closeStream(Closeable in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                logger.error("close config file error", e);
            }
        }
    }

    public static Properties getPropertiesFromFile(String path) throws IOException {
        if (path.startsWith(FILE_PRE)) {
            path = path.replaceFirst(FILE_PRE, "");
        }
        Properties p = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(path);
            p.load(in);
        } finally {
            closeStream(in);
        }
        return p;
    }

    public static Properties getPropertiesFromClassPath(String path) throws IOException {
        path = path.replaceFirst(CLASSPATH_PRE, "");
        Properties p = new Properties();
        InputStream in = null;
        try {
            in = IoUtils.class.getClassLoader().getResourceAsStream(path);
            p.load(in);
        } finally {
            closeStream(in);
        }
        return p;
    }

    /**
     * read properties from either classpath or file
     *
     * @param path file in classpath (starts with classpath:) or file path
     */
    public static Properties getProperties(String path) throws IOException {
        if (path.startsWith(CLASSPATH_PRE)) {
            return getPropertiesFromClassPath(path);
        } else {
            return getPropertiesFromFile(path);
        }
    }
}
