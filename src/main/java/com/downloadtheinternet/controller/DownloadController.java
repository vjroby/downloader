package com.downloadtheinternet.controller;

import com.downloadtheinternet.data.DownloadEntity;
import com.downloadtheinternet.data.DownloadRequestDTO;
import com.downloadtheinternet.data.DownloadResponseDTO;
import com.downloadtheinternet.service.DownloadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@Slf4j
public class DownloadController {

    private DownloadService downloadService;

    @Autowired
    DownloadController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @GetMapping(path = "/download/{fileId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DownloadEntity getDownloadDetails(@PathVariable String fileId) {
        log.info("Getting the download details for the file: {}", fileId);
        DownloadEntity entity = downloadService.retrieveEntity(fileId);
        return entity;
    }

    @PostMapping(path = "/download", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public DownloadResponseDTO saveFileFromURL(@Valid @RequestBody DownloadRequestDTO downloadRequestDTO) {
        return downloadService.saveFile(downloadRequestDTO);
    }
}

