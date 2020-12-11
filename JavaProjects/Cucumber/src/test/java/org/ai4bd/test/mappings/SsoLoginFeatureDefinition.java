/**
 * 
 */
package org.ai4bd.test.mappings;

import static org.junit.Assert.assertEquals;

import java.net.http.HttpResponse;
import java.util.Map;

import org.playground.execution.HttpRequester;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * @author SankyS
 *
 */
public class SsoLoginFeatureDefinition {

	private String api;
	private HttpResponse<String> response;

	@Given("sso login {string}")
	public void ssoLoginApi(String api) {
		this.api = api;
	}

	@When("^\"([^\"]*)\" and \"([^\"]*)\" is provided$")
	public void whenWrongUser(String user, String pass) {
		response = new HttpRequester().postWithParam(api, Map.of("username", user, "password", pass));
	}

	@Then("service should respond {int}")
	public void theResponseShouldMatch(int code) {
		assertEquals("Service Response should match.",code, response.statusCode());
	}
	
	
}
