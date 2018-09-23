package com.sbt.test.fileprocess;

import com.sbt.test.loggingDB.LogDirDest;
import com.sbt.test.loggingDB.LogDirDestRepository;

import java.io.IOException;
import java.nio.file.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class SaveDBProcessor implements Runnable {

    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private ComplextProcess complextProcess;
    private LogDirDestRepository logDirDestRepository;
    private Path dirDestArchive;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    public SaveDBProcessor(Path dirDestTemp, Path dirDestArchive, ComplextProcess complextProcess,
                           LogDirDestRepository logDirDestRepository) throws IOException {
        this.keys = new HashMap<>();
        this.watcher = FileSystems.getDefault().newWatchService();
        register(dirDestTemp);
        this.complextProcess = complextProcess;
        this.logDirDestRepository = logDirDestRepository;
        this.dirDestArchive = dirDestArchive;
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE);
        keys.put(key, dir);
    }

    @Override
    public void run() {
        while (true) {
            LogDirDest logDirDest = new LogDirDest(new Date());

            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                if (kind == OVERFLOW) {
                    continue;
                }

                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                complextProcess.complexProcessingFile(child, logDirDest, dirDestArchive);

                logDirDest.setEvent(String.format("Added new file %s", child.toString()));
                logDirDestRepository.save(logDirDest);

                System.out.format("%s: %s\n", event.kind().name(), child);

            }

            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
}
