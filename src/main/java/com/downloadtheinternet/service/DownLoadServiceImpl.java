package com.downloadtheinternet.service;

import com.downloadtheinternet.data.DownloadEntity;
import com.downloadtheinternet.data.DownloadResponseDTO;
import com.downloadtheinternet.exception.DownloadException;
import com.downloadtheinternet.repository.DownloadRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

@Service
@Slf4j
public class DownLoadServiceImpl implements DownloadService {

    private DownloadRepository downloadRepository;

    @Value("${downloader.downloadfolder}")
    private String downloadFolder;
    @Value("${downloader.buffersize}")
    private String bufferSize;


    @Autowired
    public DownLoadServiceImpl(DownloadRepository downloadRepository) {
        this.downloadRepository = downloadRepository;
    }

    @Override
    public DownloadResponseDTO saveFile(String urlString) throws DownloadException {
        try {
            URL url = new URL(urlString);
            url.getPath();
            FileSystemManager fileSystemManager = VFS.getManager();
            FileObject fileObject = fileSystemManager.resolveFile(url);
            FileContent fileContent = fileObject.getContent();
            File newFile = resolveFile(url);
            FileOutputStream fileOutputStream = new FileOutputStream(newFile);
            fileContent.write(fileOutputStream,Integer.valueOf(bufferSize));
            DownloadEntity downloadEntity = DownloadEntity.builder()
                    .id(newFile.toPath().getFileName().toString())
                    .build();
            downloadRepository.save(downloadEntity);
            return DownloadResponseDTO.builder()
                    .fileId(downloadEntity.getId())
                    .build();
        } catch (IOException e) {
            throw new DownloadException(e);
        }
    }

    @Override
    public DownloadEntity retrieveEntity(String id) {
        return downloadRepository.findFirstById(id);
    }

    private String createFilename(String path) {
        String[] parts = path.split("/");
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
}
