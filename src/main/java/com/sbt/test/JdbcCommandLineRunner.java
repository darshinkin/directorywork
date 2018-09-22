package com.sbt.test;

import com.sbt.test.settings.EventType;
import com.sbt.test.settings.Settings;
import com.sbt.test.settings.SettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Profile("prod")
class JdbcCommandLineRunner implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JdbcTemplate jdbcTemplate;
    private final WatchDir watchDir;

    @Autowired
    JdbcCommandLineRunner(JdbcTemplate jdbcTemplate, WatchDir watchDir) {
        this.jdbcTemplate = jdbcTemplate;
        this.watchDir = watchDir;
    }

    @Override
    public void run(String... strings) throws Exception {
        jdbcTemplate.execute("DROP TABLE settings IF EXISTS");
        jdbcTemplate
                .execute("CREATE TABLE settings( id serial, setting_name " +
                        " VARCHAR(255), setting_value VARCHAR(255))");

        List<Object[]> settingsRecords = Stream
                .of(String.format("%s %s", SettingsRepository.SETTING_NAME_TRACE, Boolean.FALSE.toString()),
                        String.format("%s %s", SettingsRepository.SETTING_NAME_DIR, "/home/dima/test/"),
                        String.format("%s %s", SettingsRepository.SETTING_NAME_DIR_DEST, "/home/dima/test/dest"),
                        String.format("%s %s", EventType.CREATE.name(), Boolean.TRUE.toString()),
                        String.format("%s %s", EventType.DELETE.name(), Boolean.FALSE.toString()),
                        String.format("%s %s", EventType.MODIFY.name(), Boolean.FALSE.toString())
                        )
                .map(name -> name.split(" "))
                .collect(Collectors.toList());

        jdbcTemplate
                .batchUpdate(
                        "INSERT INTO settings(setting_name, setting_value) VALUES (?,?)",
                        settingsRecords);

        RowMapper<Settings> settingsRowMapper = (rs, rowNum) -> new Settings(rs.getLong("id"),
                rs.getString("setting_name"), rs.getString("setting_value"));

        List<Settings> settings = jdbcTemplate.query(
                "SELECT id, setting_name, setting_value FROM settings", settingsRowMapper);

        settings.forEach(setting -> {log.info(setting.toString());
            System.out.println(setting.toString());});

        watchDir.processEvents();
    }
}
