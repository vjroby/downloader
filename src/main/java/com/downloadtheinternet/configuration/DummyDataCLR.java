package com.downloadtheinternet.configuration;

import com.downloadtheinternet.data.DownloadEntity;
import com.downloadtheinternet.repository.DownloadRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class DummyDataCLR implements CommandLineRunner{

    private static int i = 0;

    @Autowired
    private DownloadRepositoryImpl repository;

    @Override
    public void run(String... strings) throws Exception {


        Stream.of("http://example.com/text.txt","ftp://localhost/some/path/file.zip")
                .forEach(e ->
                        this.repository.save(DownloadEntity
                        .builder()
                        .id(getUnique())
                        .path(e)
                        .build()
                        )
                );
    }

    private String getUnique() {
        String notSoUnique = "notSoUnique";
        i++;
        return notSoUnique+i;
    }

}
