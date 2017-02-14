package com.downloadtheinternet.service;

import com.downloadtheinternet.data.DownloadEntity;
import com.downloadtheinternet.data.DownloadRequestDTO;
import com.downloadtheinternet.data.DownloadResponseDTO;
import com.downloadtheinternet.repository.DownloadRepository;
import com.downloadtheinternet.util.FileUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class DownLoadServiceImplTest {

    @Mock
    private DownloadRepository downloadRepository;
    @Mock
    private FileSystemManager fileSystemManager;
    @Mock
    private FileObject fileObject;
    @Mock
    private FileContent fileContent;
    @Captor
    private ArgumentCaptor<FileSystemOptions> optionsArgumentCaptor;

    private DownloadService downloadService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        downloadService = new DownLoadServiceImpl(
                downloadRepository, fileSystemManager,"dltest","120000"
        );
    }
    @AfterClass
    public static void deleteFolder() {
        FileUtils.deleteFolder("dltest");
    }

    @Test
    public void shouldHandleRetrieveEntityHappyFlow() throws Exception {
        // Given
        String id = "some_unique_id";

        // When
        downloadService.retrieveEntity(id);

        // Then
        verify(downloadRepository,times(1)).findFirstById(id);
    }

    @Test
    public void shouldHandleSaveFileNoAuthorization() throws Exception {
        // Given
        DownloadRequestDTO requestDTO = DownloadRequestDTO.builder()
                .url("/some/path/to/a/file")
                .build();
        when(fileSystemManager.resolveFile(any(),any(FileSystemOptions.class)))
                .thenReturn(fileObject);
        when(fileObject.getContent()).thenReturn(fileContent);


        // When
        DownloadResponseDTO responseDTO = downloadService.saveFile(requestDTO);

        // Then
        verify(downloadRepository, atLeast(1)).save(any(DownloadEntity.class));

    }
}