package com.sbt.test.fileprocess;

import com.sbt.test.loggingDB.LogDirDestRepository;
import com.sbt.test.loggingDB.LogFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.*;

@Component
public class FileProcessor {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private LogDirDestRepository logDirDestRepository;

    @Autowired
    private ComplextProcess complextProcess;

    private final static int TERMINATION_WAITING_INTERVAL = 3;

    private volatile ThreadPoolExecutor executorCopyToDir;
    private volatile ExecutorService executorSaveToDB;

    public FileProcessor(@Value("${scanner.poolsize}") int poolsize) {
        log.info(String.format("scanner.poolsize: [%s]", poolsize));
        executorCopyToDir = new ThreadPoolExecutor(poolsize, poolsize, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        executorSaveToDB = Executors.newSingleThreadExecutor();
    }

    public void submitToCopy(final Path child, final Path dirDest) {
        executorCopyToDir.execute(new CopyingProcessor(child, dirDest));
    }

    public void submitToSaveDB(final Path dirDest) throws IOException {
        executorSaveToDB.execute(new SaveDBProcessor(dirDest, complextProcess, logDirDestRepository));
    }

    private void close() {
        shutdownAnyway(executorCopyToDir);
        shutdownAnyway(executorSaveToDB);
    }

    private void shutdownAnyway(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(TERMINATION_WAITING_INTERVAL, TimeUnit.SECONDS)) {
                executor.shutdownNow();

                if (!executor.awaitTermination(TERMINATION_WAITING_INTERVAL, TimeUnit.SECONDS)) {
                    log.error("Cannot terminate tasks executorCopyToDir");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
