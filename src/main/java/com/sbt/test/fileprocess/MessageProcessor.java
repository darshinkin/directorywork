package com.sbt.test.fileprocess;

import com.sbt.test.loggingDB.LogFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

import javax.inject.Named;

@Named
public class MessageProcessor {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @JmsListener(destination = "loggings")
    public void processMassage(LogFile logFile) {
        log.info(String.format("recieved message: file name: [%s]; content: [%s]",
                logFile.getName(), logFile.getFile()));
    }
}
