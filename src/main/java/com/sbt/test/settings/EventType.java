package com.sbt.test.settings;

import java.nio.file.WatchEvent;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public enum EventType {
    CREATE(ENTRY_CREATE),
    DELETE(ENTRY_DELETE),
    MODIFY(ENTRY_MODIFY);

    EventType(WatchEvent.Kind kind) {
        this.kind = kind;
    }

    private WatchEvent.Kind kind;

    public WatchEvent.Kind getKind() {
        return kind;
    }
}
