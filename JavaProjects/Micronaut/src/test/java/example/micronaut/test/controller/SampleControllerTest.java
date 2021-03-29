package example.micronaut.test.controller;

import io.micronaut.http.client.RxHttpClient;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
public class SampleControllerTest {

	@Inject
	SampleControllerClientApi sampleControllerClientApi;


	@Test
	public void testPing(){
		Assertions.assertEquals("Pong!!!",sampleControllerClientApi.callPing().body());
	}
	@Test
	public  void testIndex(){
		Assertions.assertEquals("Hello Micronaut Token",sampleControllerClientApi.callIndex("Token"));
	}

	@Test
	public void testFilterForPing(){
		Assertions.assertEquals("Filter Used.",sampleControllerClientApi.callPingWithHeader(true,"Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJUcmhtX0l5Z3hoWTV4TmtVWnQ1MzlidVg0dmVvR3NwZ3ByMFVoY19na0E4In0.").body());
	}
}
