package com.sbt.test.settings;

import com.sbt.test.TestConfiguration;
import com.sbt.test.ApplicationConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfiguration.class, ApplicationConfiguration.class})
@ActiveProfiles(profiles = "test")
public class ApplicationTest {
    @Autowired
    private SettingsRepository settingsRepository;

    @Test
    public void getSettings() {
        Optional<Settings> bySettingName = settingsRepository.findBySettingName(SettingsRepository.SETTING_NAME_TRACE);
        Assert.assertTrue(bySettingName.isPresent());
        Settings setting = bySettingName.get();
        Assert.assertEquals(SettingsRepository.SETTING_NAME_TRACE, setting.getSettingName());
        Assert.assertEquals(Boolean.TRUE.toString(), setting.getSettingValue());
    }
}
