package com.sbt.test;

import com.sbt.test.settings.SettingsRepository;
import org.springframework.stereotype.Component;

@Component
public class Processor {

    private final SettingsRepository settingsRepository;
    private final WatchDir watchDir;

    public Processor(SettingsRepository settingsRepository, WatchDir watchDir) {
        this.settingsRepository = settingsRepository;
        this.watchDir = watchDir;
    }
}
