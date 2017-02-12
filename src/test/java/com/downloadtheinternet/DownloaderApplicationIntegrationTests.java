package com.downloadtheinternet;

import com.downloadtheinternet.data.DownloadEntity;
import com.downloadtheinternet.data.DownloadRequestDTO;
import com.downloadtheinternet.data.DownloadResponseDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DownloaderApplicationIntegrationTests {

	@Autowired
	private TestRestTemplate restTemplate;

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

		HttpEntity<DownloadRequestDTO> entity = new HttpEntity<DownloadRequestDTO>(requestDTO,headers);

		// When
		String responseEntity = this.restTemplate
				.postForObject("/download", entity, String.class );

		// Then
		assertThat(responseEntity,containsString("textfile"));
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
		HttpEntity<DownloadRequestDTO> entity = new HttpEntity<DownloadRequestDTO>(requestDTO,headers);

		// When
		DownloadResponseDTO postEntity = this.restTemplate
				.postForObject("/download", entity, DownloadResponseDTO.class );
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
