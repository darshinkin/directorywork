package com.sbt.test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.sbt.test.exceptions.NotFoundSettingException;
import com.sbt.test.settings.EventType;
import com.sbt.test.settings.Settings;
import com.sbt.test.settings.SettingsRepository;
import org.springframework.stereotype.Component;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.io.*;
import java.util.*;

@Component
public class WatchDir {

    public static final String XML = "application/xml";
    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private Path dirDest;

    private SettingsRepository settingsRepository;


    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir, boolean trace, WatchEvent.Kind [] kinds) throws IOException {
        WatchKey key = dir.register(watcher, kinds);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    WatchDir(SettingsRepository settingsRepository) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.settingsRepository = settingsRepository;
    }

    private void initSettings() throws IOException {
        Settings setting = settingsRepository.findBySettingName(SettingsRepository.SETTING_NAME_DIR_DEST).
                orElseThrow(() -> new NotFoundSettingException(SettingsRepository.SETTING_NAME_DIR_DEST));
        this.dirDest = Paths.get(setting.getSettingValue());
        if (!Files.exists(dirDest)) {
            Files.createDirectories(dirDest);
        }
        setting = settingsRepository.findBySettingName(SettingsRepository.SETTING_NAME_TRACE).
                orElse(new Settings(SettingsRepository.SETTING_NAME_TRACE, Boolean.FALSE.toString()));
        boolean trace = Boolean.valueOf(setting.getSettingValue());
        setting = settingsRepository.findBySettingName(SettingsRepository.SETTING_NAME_DIR).
                orElseThrow(() -> new NotFoundSettingException(SettingsRepository.SETTING_NAME_DIR));
        Path dir = Paths.get(setting.getSettingValue());
        List<WatchEvent.Kind> kindList = new LinkedList<>();
        setting = settingsRepository.findBySettingName(EventType.CREATE.name()).
                orElseThrow(() -> new NotFoundSettingException(EventType.CREATE.name()));
        if (Boolean.parseBoolean(setting.getSettingValue())) {
            kindList.add(ENTRY_CREATE);
        }
        setting = settingsRepository.findBySettingName(EventType.DELETE.name()).
                orElseThrow(() -> new NotFoundSettingException(EventType.DELETE.name()));
        if (Boolean.parseBoolean(setting.getSettingValue())) {
            kindList.add(ENTRY_DELETE);
        }
        setting = settingsRepository.findBySettingName(EventType.MODIFY.name()).
                orElseThrow(() -> new NotFoundSettingException(EventType.MODIFY.name()));
        if (Boolean.parseBoolean(setting.getSettingValue())) {
            kindList.add(ENTRY_MODIFY);
        }
        WatchEvent.Kind [] kinds =  kindList.toArray(new WatchEvent.Kind[0]);
        register(dir, trace, kinds);
    }

    void processEvents() throws IOException {
        initSettings();
        for (;;) {

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
                try {
                    if (Files.probeContentType(child).equals(XML)) {
                        FileUtils.copyFileToDirectory(child.toFile(), dirDest.toFile());
                    }
                } catch (IOException e) {
                    System.err.println(e);
                    continue;
                }

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