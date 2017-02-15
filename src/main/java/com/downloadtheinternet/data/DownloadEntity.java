package com.downloadtheinternet.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class DownloadEntity {

    private String id;
    private String path;
    private String fileName;
    private Status status = Status.STARTED;

    public void completed() {
        this.status = Status.COMPLETED;
    }

    public void error() {
        this.status = Status.ERROR;
    }

    public enum Status {
        STARTED,
        COMPLETED,
        ERROR;
    }
}
