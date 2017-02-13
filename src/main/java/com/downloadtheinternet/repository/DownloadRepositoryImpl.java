package com.downloadtheinternet.repository;

import com.downloadtheinternet.data.DownloadEntity;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DownloadRepositoryImpl implements DownloadRepository {

    private ConcurrentHashMap<String, DownloadEntity> storage = new ConcurrentHashMap<>();

    @Override
    public void save(DownloadEntity downloadEntity) {
        storage.put(downloadEntity.getId(), downloadEntity);
    }

    @Override
    public DownloadEntity findFirstById(String id) {
        return storage.get(id);
    }


}
