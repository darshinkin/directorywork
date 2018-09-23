package com.sbt.test.fileprocess;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import com.sbt.test.exceptions.ProcessFileException;
import com.sbt.test.loggingDB.LogDirDest;
import com.sbt.test.loggingDB.LogDirDestRepository;
import com.sbt.test.loggingDB.LogFile;
import com.sbt.test.loggingDB.LogFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

@Named
@Transactional
public class ComplextProcess {

    @Inject
    private JmsTemplate jmsTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    private LogFileRepository logFileRepository;
    private LogDirDestRepository logDirDestRepository;

    public ComplextProcess(LogFileRepository logFileRepository, LogDirDestRepository logDirDestRepository) {
        this.logFileRepository = logFileRepository;
        this.logDirDestRepository = logDirDestRepository;
    }

    public void complexProcessingFile(Path child, LogDirDest logDirDest) {
        String content;
        try {
            byte[] data = Files.readAllBytes(child);
            content = new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println(e);
            String errorMsg = String.format("Occured error %s", e.getMessage());
            logDirDest.setEvent(errorMsg);
            logDirDestRepository.save(logDirDest);
            throw new ProcessFileException(errorMsg);
        }

        LogFile logFile = new LogFile(child.getFileName().toString(), content, new Date());
        logFileRepository.save(logFile);
        jmsTemplate.convertAndSend("loggings", logFile);
    }
}
