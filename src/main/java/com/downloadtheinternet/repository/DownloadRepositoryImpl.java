package com.downloadtheinternet.repository;

import com.downloadtheinternet.data.DownloadEntity;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class DownloadRepositoryImpl implements DownloadRepository {

    private Map<String, DownloadEntity> storage = new HashMap<>();

    @Override
    public void save(DownloadEntity downloadEntity){
        storage.put(downloadEntity.getId(), downloadEntity);
    }

    @Override
    public DownloadEntity findFirstById(String id){
        return storage.get(id);
    }

}
