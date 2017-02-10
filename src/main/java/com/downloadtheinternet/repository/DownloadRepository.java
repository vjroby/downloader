package com.downloadtheinternet.repository;

import com.downloadtheinternet.data.DownloadEntity;

/**
 * Interface for DownloadRepository
 */
public interface DownloadRepository  {
    /**
     * Method for saving a Download entity to the repository
     * @param downloadEntity the download enity to be saved
     */
    void save(DownloadEntity downloadEntity);

    /**
     * Method for getting a DownloadEntity by id
     * @param id the entity id
     * @return the dowload entity
     */
    DownloadEntity findFirstById(String id);
}
