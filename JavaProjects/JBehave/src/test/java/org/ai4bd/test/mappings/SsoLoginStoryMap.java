/**
 * 
 */
package org.ai4bd.test.mappings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.http.HttpResponse;
import java.util.Map;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.playground.execution.HttpRequester;

/**
 * @author SankyS
 *
 */
public class SsoLoginStoryMap {

	private String api;
	private HttpResponse<String> response;

	@Given("sso login api")
	public void givenSsoLoginApi() {
		api = "https://sso.ai4bd.org/login";
	}

	@When("username $user and password $pass is provided")
	public void whenWrongUser(String user, String pass) {
		// perform the action
		response = new HttpRequester().postWithParam(api, Map.of("username", user, "password", pass));
	}

	@Then("sso login respond: 401 unauthorized")
	public void theResponseShouldBe401() {
		assertEquals("Wrong user should return 401.",401, response.statusCode());
	}
	
	@Then("sso login respond: 201 Authorization code created")
	public void theResponseShouldBe201() {
		assertEquals("Correct user and pass should create a token",201, response.statusCode());
	}
	
	@Then("sso login respond a valid token")
	public void theResponseShouldHaveValidToken() {
		assertNotNull("Response body should not be empty",response.body());
		assertTrue("Authorization key should match regex", response.body().matches("[\\w-+\\/=]+\\.[\\w-+\\/=]+\\.[\\w-+\\/=]+"));
		
	}
	
}
