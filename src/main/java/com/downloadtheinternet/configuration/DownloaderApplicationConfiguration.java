package com.downloadtheinternet.configuration;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DownloaderApplicationConfiguration {

    @Bean
    public FileSystemManager fileSystemManager() throws FileSystemException {
        return VFS.getManager();
    }
}
