/**
 * 
 */
package org.ai4bd.test;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

/**
 * @author SankyS
 *
 */
@RunWith(Cucumber.class)
@CucumberOptions(features = { "classpath:sso-login.feature" }, plugin = { "pretty",
		"json:target/reports/json/login.json" })
public class LoginStoryTest {
}
