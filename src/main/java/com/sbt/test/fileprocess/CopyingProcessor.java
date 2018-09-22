package com.sbt.test.fileprocess;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class CopyingProcessor implements Runnable {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Path child;
    private Path dirDest;

    public CopyingProcessor(Path child, Path dirDest) {
        this.child = child;
        this.dirDest = dirDest;
    }

    @Override
    public void run() {
        boolean isFault = true;
        while (isFault && !Thread.currentThread().isInterrupted()) {
            isFault = false;
            try {
                FileUtils.copyFileToDirectory(child.toFile(), dirDest.toFile());
                log.info(String.format("File %s had been copied to directory %s", child.toString(), dirDest.toString()));
            } catch (IOException e) {
                String textError = String.format("Occurant error during copying file %s to directory %s",
                        child.toAbsolutePath().toFile(), dirDest.toAbsolutePath().toString());
                log.error(textError, e);
                isFault = true;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    log.error(String.format("Interrupted thread %s on operation: %s. Error: %s", Thread.currentThread().getName(), textError, e1.getMessage()));
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
