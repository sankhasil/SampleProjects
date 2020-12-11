/**
 * 
 */
package org.sample.test.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.http.HttpResponse;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sample.execution.HttpRequester;
import org.sample.util.CommonUtils;

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
    
    @Given("sso server address {}")
    public void ssoLogin(String api) {
        this.api = api;
    }

    @When("{} and {} is provided")
    public void isProvided(String user, String pass) {
        user = getEnvParameter(user);
        pass = getEnvParameter(pass);
        
        response = new HttpRequester().postWithParam(api, Map.of("username", user, "password", pass));
    }


    @Then("sso login should respond with {int}")
    public void theResponseShouldMatch(int code) {
        assertEquals("Service Response should match.",code, response.statusCode());
    }
    
    @Then("for correct username and password a valid authorization token.")
    public void theResponseShouldHaveValidToken() {
      if(response.statusCode()==201) {
        assertNotNull("Response body should not be empty",response.body());
        assertTrue("Authorization key should match regex",response.body().matches("[\\w-+\\/=]+\\.[\\w-+\\/=]+\\.[\\w-+\\/=]+"));
      }
      
  }
    private String getEnvParameter(String param) {
      if(param.toLowerCase().startsWith("env:"))
        return CommonUtils.getEnvValue(StringUtils.substringAfter(param, ":"));
      return param;
    }
    
    
}
