package com.downloadtheinternet.data;

import lombok.*;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadRequestDTO {
    @NotEmpty
    private String url;

    private String username;
    private String password;
}
