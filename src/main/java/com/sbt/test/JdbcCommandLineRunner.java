package com.sbt.test;

import com.sbt.test.exceptions.NotFoundSettingException;
import com.sbt.test.fileprocess.FileProcessor;
import com.sbt.test.settings.Settings;
import com.sbt.test.settings.SettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Component
@Profile("dev")
class JdbcCommandLineRunner implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final SettingsRepository settingsRepository;
    private final WatchDir watchDir;
    private final FileProcessor fileProcessor;

    @Autowired
    JdbcCommandLineRunner(WatchDir watchDir, SettingsRepository settingsRepository, FileProcessor fileProcessor) {
        this.settingsRepository = settingsRepository;
        this.fileProcessor = fileProcessor;
        this.watchDir = watchDir;
    }

    @Override
    public void run(String... strings) throws Exception {
        Path dirDest = retrievDestDir();
        Path dirDestTemp = Paths.get(dirDest.toString(), "temp");
        if (!Files.exists(dirDestTemp)) {
            Files.createDirectories(dirDestTemp);
        }
        Path dirDestArchive = Paths.get(dirDest.toString(), "archive");
        if (!Files.exists(dirDestArchive)) {
            Files.createDirectories(dirDestArchive);
        }
        fileProcessor.submitToSaveDB(dirDestTemp, dirDestArchive);
        watchDir.processEvents(dirDestTemp);
    }

    private Path retrievDestDir() throws IOException {
        Settings setting = settingsRepository.findBySettingName(SettingsRepository.SETTING_NAME_DIR_DEST).
                orElseThrow(() -> new NotFoundSettingException(SettingsRepository.SETTING_NAME_DIR_DEST));
        Path dirDest = Paths.get(setting.getSettingValue());
        if (!Files.exists(dirDest)) {
            Files.createDirectories(dirDest);
        }
        return dirDest;
    }
}
