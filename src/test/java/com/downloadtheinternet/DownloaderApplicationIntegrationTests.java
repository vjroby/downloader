package com.downloadtheinternet;

import com.downloadtheinternet.data.DownloadRequestDTO;
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
import static org.hamcrest.MatcherAssert.assertThat;


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

}
