package com.plugin.qanda.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class PostControllerTest {
	@LocalServerPort
	private int port;
	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	public void test_ping() throws Exception {
		//Dont work if @EnableDiscovery is used for service registry
		ResponseEntity<String> response = testRestTemplate
				.getForEntity(new URL("http://localhost:" + port + "/post/ping").toString(), String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
}
