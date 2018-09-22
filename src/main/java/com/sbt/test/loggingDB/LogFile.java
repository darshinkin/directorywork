package com.sbt.test.loggingDB;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
public class LogFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Lob
    private String file;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSave;

    public LogFile(String file, Date dateSave) {
        this.file = file;
        this.dateSave = dateSave;
    }
}
