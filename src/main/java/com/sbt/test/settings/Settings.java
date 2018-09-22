package com.sbt.test.settings;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String settingName;

    private String settingValue;

    public Settings() {
    }

    public Settings(Long id, String settingName, String settingValue) {
        this.id = id;
        this.settingName = settingName;
        this.settingValue = settingValue;
    }

    public Settings(String settingName, String settingValue) {
        this.settingName = settingName;
        this.settingValue = settingValue;
    }
}
