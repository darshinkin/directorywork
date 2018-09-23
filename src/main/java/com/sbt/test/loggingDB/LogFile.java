package com.sbt.test.loggingDB;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
public class LogFile implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @Lob
    private String file;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSave;

    public LogFile(String name, String file, Date dateSave) {
        this.name = name;
        this.file = file;
        this.dateSave = dateSave;
    }
}
