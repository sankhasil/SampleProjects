/**
 * 
 */
package example.micronaut.definition;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * @author sankha
 *
 */
public class SampleDefinition {

	private final HttpClient httpClient = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofMinutes(5))
			.build();
	private String user;
	@When("{} is provided.")
	public void dataProvided(String user) {
		this.user = user;
	}
	@Then("service should respond with {int}")
	public void checkResponseCode(int responseCode) throws IOException, InterruptedException {
		HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:8877")).build();
		httpClient.send(null, null);
	}
}
