package com.sbt.test.settings;

import com.sbt.test.settings.Settings;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface SettingsRepository extends PagingAndSortingRepository<Settings, Long> {

    String SETTING_NAME_TRACE = "trace";
    String SETTING_NAME_DIR = "dir_source";
    String SETTING_NAME_DIR_DEST =  "dir_dest";

    Optional<Settings> findBySettingName(String settintName);
}
