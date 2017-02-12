package com.downloadtheinternet.exception;

import org.apache.commons.vfs2.FileSystemException;

import java.io.IOException;

public class DownloadException extends RuntimeException {
    public DownloadException(FileSystemException e) {
        super(e);
    }

    public DownloadException(IOException e) {
        super(e);
    }

    public DownloadException(InterruptedException e) {
        super(e);
    }
}
