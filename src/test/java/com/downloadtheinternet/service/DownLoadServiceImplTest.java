package com.downloadtheinternet.service;

import com.downloadtheinternet.data.DownloadEntity;
import com.downloadtheinternet.data.DownloadRequestDTO;
import com.downloadtheinternet.data.DownloadResponseDTO;
import com.downloadtheinternet.exception.DownloadException;
import com.downloadtheinternet.repository.DownloadRepository;
import com.downloadtheinternet.util.FileUtils;
import org.apache.commons.vfs2.*;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
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
    private ArgumentCaptor<DownloadEntity> downloadEntityArgumentCaptor;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private DownloadService downloadService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        downloadService = new DownLoadServiceImpl(
                downloadRepository, fileSystemManager, "dltest", "120000"
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
        verify(downloadRepository, times(1)).findFirstById(id);
    }

    @Test
    public void shouldHandleSaveFile() throws Exception {
        // Given
        DownloadRequestDTO requestDTO = DownloadRequestDTO.builder()
                .url("/some/path/to/a/file")
                .build();
        when(fileSystemManager.resolveFile(any(), any(FileSystemOptions.class)))
                .thenReturn(fileObject);
        when(fileObject.getContent()).thenReturn(fileContent);


        // When
        DownloadResponseDTO responseDTO = downloadService.saveFile(requestDTO);

        // Then
        verify(downloadRepository, atLeast(1)).save(any(DownloadEntity.class));
        assertNotNull(responseDTO);
    }

    @Test
    public void shouldHandleUrlException() throws Exception {
        // Given
        expectedException.expect(DownloadException.class);

        DownloadRequestDTO requestDTO = DownloadRequestDTO.builder()
                .url("121332131:321*^&*^&*")
                .build();

        // When
        downloadService.saveFile(requestDTO);
        // Then
        verify(downloadRepository, never()).save(any(DownloadEntity.class));
    }

    @Test
    public void shouldHandleFileException() throws Exception {
        // Given
        DownloadRequestDTO requestDTO = DownloadRequestDTO.builder()
                .url("/some/path")
                .build();
        when(fileSystemManager.resolveFile(any(String.class), any()))
                .thenThrow(new FileSystemException("312312")); // <- exception handle in biConsumer

        doNothing().when(downloadRepository).save(downloadEntityArgumentCaptor.capture());
        // When
        downloadService.saveFile(requestDTO);
        Thread.sleep(2000); // <- for async
        // Then
        verify(downloadRepository, times(2)).save(any(DownloadEntity.class));
        DownloadEntity downloadEntity = downloadEntityArgumentCaptor.getValue();
        assertThat(downloadEntity.getStatus().name(), is("ERROR"));
    }
}