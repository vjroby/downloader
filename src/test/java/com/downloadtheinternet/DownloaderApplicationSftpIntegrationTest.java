package com.downloadtheinternet;


import com.downloadtheinternet.data.DownloadEntity;
import com.downloadtheinternet.data.DownloadRequestDTO;
import com.downloadtheinternet.data.DownloadResponseDTO;
import com.downloadtheinternet.util.FileUtils;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:test.properties")
public class DownloaderApplicationSftpIntegrationTest {
    private SshServer sshd;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void beforeTestSetup() throws Exception {
        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(22999);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
        sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
            public boolean authenticate(String username, String password, ServerSession session) {
                return username.equals("someuser") && password.equals("somepassword");
            }
        });
        CommandFactory myCommandFactory = new CommandFactory() {
            public Command createCommand(String command) {
                System.out.println("Command: " + command);
                return null;
            }
        };
        sshd.setCommandFactory(new ScpCommandFactory());
        List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
        namedFactoryList.add(new SftpSubsystem.Factory());
        sshd.setSubsystemFactories(namedFactoryList);
        sshd.start();
    }

    @After
    public void teardown() throws Exception {
        sshd.stop();
    }

    @AfterClass
    public static void deleteFolder() {
        FileUtils.deleteFolder("downloadstest");
    }

    @Test
    public void shouldHandleFailedAuthentication() throws Exception {
        // Given
        DownloadRequestDTO requestDTO = DownloadRequestDTO.builder()
                .url("sftp://localhost:22999/src/main/resources/testsftp.conf")
                .password("wrongpassword")
                .username("someuser")
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DownloadRequestDTO> entity = new HttpEntity<DownloadRequestDTO>(requestDTO, headers);

        // When
        DownloadResponseDTO postEntity = this.restTemplate
                .postForObject("/download", entity, DownloadResponseDTO.class);
        DownloadEntity getEntity = this.restTemplate
                .getForObject("/download/{fileId}", DownloadEntity.class, postEntity.getFileId());

        while (getEntity.getStatus().toString().equals("STARTED")) {
            Thread.sleep(1000);
            getEntity = this.restTemplate
                    .getForObject("/download/{fileId}", DownloadEntity.class, postEntity.getFileId());
        }
        // Then
        assertThat(postEntity.getFileId(), is(notNullValue()));
        assertThat(getEntity.getStatus().toString(), is(equalTo("ERROR")));
    }

    @Test
    public void shouldGetSftpFileFromMockServer() throws Exception {
        // Given
        DownloadRequestDTO requestDTO = DownloadRequestDTO.builder()
                .url("sftp://localhost:22999/src/main/resources/testsftp.conf")
                .password("somepassword")
                .username("someuser")
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DownloadRequestDTO> entity = new HttpEntity<DownloadRequestDTO>(requestDTO, headers);

        // When
        DownloadResponseDTO postEntity = this.restTemplate
                .postForObject("/download", entity, DownloadResponseDTO.class);
        DownloadEntity getEntity = this.restTemplate
                .getForObject("/download/{fileId}", DownloadEntity.class, postEntity.getFileId());

        while (getEntity.getStatus().toString().equals("STARTED")) {
            Thread.sleep(10000);
            getEntity = this.restTemplate
                    .getForObject("/download/{fileId}", DownloadEntity.class, postEntity.getFileId());
        }

        // Then
        assertThat(postEntity.getFileId(), is(notNullValue()));
        assertThat(getEntity.getStatus().toString(), is(equalTo("COMPLETED")));
    }


    /**
     * Test ignored because the sftp service is outside so it can not be relayed on
     *
     * @throws Exception the exception
     */
    @Ignore
    @Test
    public void shouldGetSftpFile() throws Exception {
        // Given
        DownloadRequestDTO requestDTO = DownloadRequestDTO.builder()
                .url("sftp://test.rebex.net:22/pub/example/KeyGenerator.png")
                .password("password")
                .username("demo")
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DownloadRequestDTO> entity = new HttpEntity<DownloadRequestDTO>(requestDTO, headers);

        // When
        DownloadResponseDTO postEntity = this.restTemplate
                .postForObject("/download", entity, DownloadResponseDTO.class);
        DownloadEntity getEntity = this.restTemplate
                .getForObject("/download/{fileId}", DownloadEntity.class, postEntity.getFileId());

        while (getEntity.getStatus().toString().equals("STARTED")) {
            Thread.sleep(30000);
            getEntity = this.restTemplate
                    .getForObject("/download/{fileId}", DownloadEntity.class, postEntity.getFileId());
        }

        // Then
        assertThat(postEntity.getFileId(), is(notNullValue()));
        assertThat(getEntity.getStatus().toString(), is(equalTo("COMPLETED")));
    }
}
