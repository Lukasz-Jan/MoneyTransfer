package com.lj.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
public class FileFetchService {

    private static final Logger logger = LoggerFactory.getLogger(FileFetchService.class);
    private static final String CLASSPATH_PREFIX = "classpath:data/";

    public File fetchFile(String path) throws IOException {

        File file = new File(path);

        if (file.exists()) {
            logger.info("loading data from file  " + file);
            return file;
        }
        else {
            logger.info("file " + path + " not found in file system ");
            String resourcePath = addClasspathPrefix(path);
            Resource resource = new DefaultResourceLoader().getResource(resourcePath);

            if (resource.isFile()) {
                logger.info("Init file fetched from file : " + resourcePath);
                return resource.getFile();
            }
            else {
                File tempFile = File.createTempFile("initFile", null);
                logger.info("Resource not a file, file created from resource stream: " );
                Files.copy(resource.getInputStream(), tempFile.toPath(), REPLACE_EXISTING);
                return tempFile;
            }
        }
    }

    private String addClasspathPrefix(String path) {

        if(!path.contains(CLASSPATH_PREFIX)) {
            String resourcePath = CLASSPATH_PREFIX + path;
            return resourcePath;
        }
        else return path;
    }
}
