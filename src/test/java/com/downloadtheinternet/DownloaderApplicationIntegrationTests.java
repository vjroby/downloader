package com.downloadtheinternet;

import com.downloadtheinternet.data.DownloadEntity;
import com.downloadtheinternet.data.DownloadRequestDTO;
import com.downloadtheinternet.data.DownloadResponseDTO;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:test.properties")
public class DownloaderApplicationIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(wireMockConfig()
            .containerThreads(20)
            .port(5555)
    );

    @BeforeClass
    public static void setUp() {
        wireMockRule.stubFor(get(urlPathEqualTo("/stub/textfile.txt"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "plain/text")
                        .withBody("test\ntest\n")
                )
        );

        wireMockRule.stubFor(get(urlPathEqualTo("/stub/delayed.html"))
                .willReturn(aResponse()
                        .withFixedDelay(10_000)
                        .withHeader("Content-Type", "plain/text")
                        .withBody("<html>" +
                                "<title>Some small html</title>" +
                                "<body>" +
                                "this is from delayed text file" +
                                "</body>" +
                                "</html>\n")
                )
        );
    }

    @Test
    public void getDownloadDetailsById() throws Exception {
        // Given
        final String fileId = "notSoUnique1"; // <- it's already there

        // When
        ResponseEntity<String> responseEntity = this.restTemplate
                .getForEntity("/download/{fileId}", String.class, fileId);

        // Then
        org.assertj.core.api.Assertions.assertThat(responseEntity.getBody()).contains("http://example.com/text.txt");
    }

    @Test
    public void postHttpToDownload() throws Exception {
        // Given

        DownloadRequestDTO requestDTO = DownloadRequestDTO.builder()
                .url("http://localhost:5555/stub/textfile.txt")
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
    public void shouldHandleLongResponseFromURL() throws Exception {
        // Given
        DownloadRequestDTO requestDTO = DownloadRequestDTO.builder()
                .url("http://localhost:5555/stub/delayed.html") // <- delayed the stub with 10 seconds
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Long started = System.currentTimeMillis();
        HttpEntity<DownloadRequestDTO> entity = new HttpEntity<DownloadRequestDTO>(requestDTO, headers);

        // When
        DownloadResponseDTO postEntity = this.restTemplate
                .postForObject("/download", entity, DownloadResponseDTO.class);
        Long completedIn = System.currentTimeMillis() - started;
        DownloadEntity getEntityBeforeCompletion = this.restTemplate
                .getForObject("/download/{fileId}", DownloadEntity.class, postEntity.getFileId());
        Thread.sleep(11000); // <- wait for file to be downloaded
        DownloadEntity getEntityAfterCompletion = this.restTemplate
                .getForObject("/download/{fileId}", DownloadEntity.class, postEntity.getFileId());

        // Then
        assertTrue(completedIn < 9000);
        assertThat(getEntityBeforeCompletion.getStatus().toString(), is(equalTo("STARTED")));
        assertThat(getEntityAfterCompletion.getStatus().toString(), is(equalTo("COMPLETED")));
    }
}
