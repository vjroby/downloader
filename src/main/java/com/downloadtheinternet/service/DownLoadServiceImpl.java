package com.downloadtheinternet.service;

import com.downloadtheinternet.data.DownloadEntity;
import com.downloadtheinternet.data.DownloadResponseDTO;
import com.downloadtheinternet.exception.DownloadException;
import com.downloadtheinternet.repository.DownloadRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.util.concurrent.*;

@Service
@Slf4j
public class DownLoadServiceImpl implements DownloadService {

    private DownloadRepository downloadRepository;
    private SecureRandom random = new SecureRandom();;


    @Value("${downloader.downloadfolder}")
    private String downloadFolder;
    @Value("${downloader.buffersize}")
    private String bufferSize;

    @Autowired
    public DownLoadServiceImpl(DownloadRepository downloadRepository) {
        this.downloadRepository = downloadRepository;
    }

    @Override
    public DownloadEntity retrieveEntity(String id) {
        return downloadRepository.findFirstById(id);
    }

    @Override
    public DownloadResponseDTO saveFile(String urlString) throws DownloadException {
        try {
            URL url = new URL(urlString);
            url.getPath();
            File newFile = resolveFile(url);
            DownloadEntity downloadEntity = DownloadEntity.builder()
                    .id(nextUUID())
                    .fileName(newFile.toPath().getFileName().toString())
                    .path(url.getPath())
                    .status(DownloadEntity.Status.STARTED)
                    .build();

            CompletionStage<DownloadEntity> future = CompletableFuture.supplyAsync(() -> {
                    downloadFile(newFile, url, downloadEntity);
                return downloadEntity;

            });
            future.thenAccept(this::updateRepository);
            downloadRepository.save(downloadEntity);
            return DownloadResponseDTO.builder()
                    .fileId(downloadEntity.getId())
                    .build();
        } catch (IOException e) {
            throw new DownloadException(e);
        }
    }

    private DownloadEntity downloadFile(File newFile, URL url, DownloadEntity downloadEntity) {
        try {
            FileSystemManager fileSystemManager = VFS.getManager();
            FileObject fileObject = fileSystemManager.resolveFile(url);
            FileContent fileContent = fileObject.getContent();
            FileOutputStream fileOutputStream = new FileOutputStream(newFile);
            fileContent.write(fileOutputStream,Integer.valueOf(bufferSize));
            downloadEntity.completed();
            log.info("A new file was downloaded. URL: {}, Path: {}", url, newFile.getPath());
            return downloadEntity;

        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
            throw new DownloadException(e);
        }
    }

    private void updateRepository(DownloadEntity downloadEntity) {
        downloadRepository.save(downloadEntity);
    }

    private String createFilename(String path) {
        File folder = new File(downloadFolder);
        if(!folder.exists()){
            folder.mkdir();
            log.info("Created a new folder with this path: '{}'",folder.getPath());
        }
        String basename = FilenameUtils.getBaseName(path);
        String extension = FilenameUtils.getExtension(path);
        if(extension.length() !=0){
            extension  = "." +extension;
        }

        return downloadFolder +"/" + basename +"_"+ System.currentTimeMillis() + extension;
    }

    private File resolveFile(URL url) throws IOException {
        File file = new File(createFilename(url.getPath()));
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    /**
     * his works by choosing 130 bits from a cryptographically secure random bit generator,
     * and encoding them in base-32. 128 bits is considered to be cryptographically strong,
     * but each digit in a base 32 number can encode 5 bits, so 128 is rounded up to the next multiple of 5.
     * This encoding is compact and efficient, with 5 random bits per character.
     * Compare this to a random UUID, which only has 3.4 bits per character in standard layout,
     * and only 122 random bits in total.
     *
     * @return the uuid
     */
    private String nextUUID() {
        return new BigInteger(130, random).toString(32);
    }
}
