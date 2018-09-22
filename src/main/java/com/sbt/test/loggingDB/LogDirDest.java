package com.sbt.test.loggingDB;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
public class LogDirDest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date changeTime;

    private String event;

    public LogDirDest(Date changeTime) {
        this.changeTime = changeTime;
    }
}
