package com.downloadtheinternet.service;


import com.downloadtheinternet.data.DownloadEntity;
import com.downloadtheinternet.data.DownloadRequestDTO;
import com.downloadtheinternet.data.DownloadResponseDTO;
import com.downloadtheinternet.exception.DownloadException;

/**
 * Interface for DownloadService
 */
public interface DownloadService {

    /**
     * Method for saving a file from a given url
     * @param downloadRequestDTO the url where the file is
     */
    DownloadResponseDTO saveFile(DownloadRequestDTO downloadRequestDTO) throws DownloadException;

    /**
     * Method for
     * @param id
     * @return
     */
    DownloadEntity retrieveEntity(String id);
}
