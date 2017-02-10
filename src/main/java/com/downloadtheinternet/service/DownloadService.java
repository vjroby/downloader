package com.downloadtheinternet.service;


import com.downloadtheinternet.data.DownloadEntity;
import com.downloadtheinternet.data.DownloadResponseDTO;
import com.downloadtheinternet.exception.DownloadException;

/**
 * Interface for DownloadService
 */
public interface DownloadService {

    /**
     * Method for saving a file from a given url
     * @param url the url where the file is
     */
    DownloadResponseDTO saveFile(String url) throws DownloadException;

    /**
     * Method for
     * @param id
     * @return
     */
    DownloadEntity retrieveEntity(String id);
}
