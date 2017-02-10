package com.downloadtheinternet.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class DownloadEntity {

    private String id;
    private String path;
}
