package com.sbt.test.loggingDB;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface LogFileRepository extends PagingAndSortingRepository<LogFile, Long> {
}
