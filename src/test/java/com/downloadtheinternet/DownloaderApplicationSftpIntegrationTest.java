package com.downloadtheinternet;


import com.downloadtheinternet.data.DownloadRequestDTO;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
                // TODO Auto-generated method stub
                return true;
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
    public void teardown() throws Exception { sshd.stop(); }

    @Test
    public void shouldGetSftpFile() {
        // Given

        DownloadRequestDTO requestDTO = DownloadRequestDTO.builder()
                .url("sftp://localhost:22999/testsftp.conf")
                .password("somepassword")
                .username("someuser")
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DownloadRequestDTO> entity = new HttpEntity<DownloadRequestDTO>(requestDTO, headers);

        // When
        String responseEntity = this.restTemplate
                .postForObject("/download", entity, String.class);

        // Then
        assertThat(responseEntity, containsString("fileId"));
    }

    @Test
    public void testPutAndGetFile() throws Exception {
        JSch jsch = new JSch();
        Hashtable config = new Hashtable();
        config.put("StrictHostKeyChecking", "no");
        JSch.setConfig(config);
        Session session = jsch.getSession("remote-username", "localhost", 22999);
        session.setPassword("remote-password");
        session.connect();
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        final String testFileContents = "some file contents";
        InputStream fileInputStream = DownloaderApplication.class.getClass().getResourceAsStream("/testsftp.conf");

        try(ByteArrayInputStream bais = new ByteArrayInputStream(testFileContents.getBytes())) {
            sftpChannel.put(fileInputStream, "testsftp.conf",ChannelSftp.OVERWRITE);
            String downloadedFileName = "downLoadFile";
            sftpChannel.get("testsftp.conf", downloadedFileName);
            File downloadedFile = new File(downloadedFileName);
            assertTrue(downloadedFile.exists());

        }

    }

}
