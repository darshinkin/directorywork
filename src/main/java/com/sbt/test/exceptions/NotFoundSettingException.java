package com.sbt.test.exceptions;

public class NotFoundSettingException extends RuntimeException {
    public NotFoundSettingException(String s) {
        super(String.format("Not found setting [%s]", s));
    }
}
