package org.sample.test;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.CucumberOptions.SnippetType;


/**
 * @author SankyS
 *
 */
@RunWith(Cucumber.class)
@CucumberOptions(features = { "classpath:sso-login.feature" }, plugin = { "pretty","summary",
"json:target/reports/json/login.json","html:target/reports/login.html" },snippets = SnippetType.CAMELCASE)
public class SsoLoginStoryTest {

}
