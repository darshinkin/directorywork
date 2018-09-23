package com.sbt.test.exceptions;

public class ProcessFileException extends RuntimeException {
    public ProcessFileException(String s) {
        super(String.format("Occured error during process file: [%s]", s));
    }
}
