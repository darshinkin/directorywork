package com.sbt.test.fileprocess;

import com.sbt.test.loggingDB.LogDirDest;
import com.sbt.test.loggingDB.LogDirDestRepository;
import com.sbt.test.loggingDB.LogFile;
import com.sbt.test.loggingDB.LogFileRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class SaveDBProcessor implements Runnable {

    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private LogFileRepository logFileRepository;
    private LogDirDestRepository logDirDestRepository;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    public SaveDBProcessor(Path dirDest, LogFileRepository logFileRepository,
                           LogDirDestRepository logDirDestRepository) throws IOException {
        this.keys = new HashMap<>();
        this.watcher = FileSystems.getDefault().newWatchService();
        register(dirDest);
        this.logFileRepository = logFileRepository;
        this.logDirDestRepository = logDirDestRepository;
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE);
        keys.put(key, dir);
    }

    @Override
    public void run() {
        while (true) {

            LogDirDest logDirDest = new LogDirDest(new Date());

            // wait for key to be signalled
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

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                String content;
                try {
                    byte[] data = Files.readAllBytes(child);
                    content = new String(data, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    System.err.println(e);
                    logDirDest.setEvent(String.format("Occured error %s", e.getMessage()));
                    logDirDestRepository.save(logDirDest);
                    continue;
                }
                logFileRepository.save(new LogFile(content, new Date()));
                logDirDest.setEvent(String.format("Added new file %s", child.toString()));
                logDirDestRepository.save(logDirDest);

                // print out event
                System.out.format("%s: %s\n", event.kind().name(), child);

            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
}
