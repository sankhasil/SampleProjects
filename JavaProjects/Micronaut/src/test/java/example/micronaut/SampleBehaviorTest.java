package example.micronaut;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cucumber.junit.platform.engine.Cucumber;

/**
 * 
 * @author sankha
 *
 */

@Cucumber
public class SampleBehaviorTest {
	Logger log = LoggerFactory.getLogger(SampleBehaviorTest.class);
	
	@BeforeAll
	public void init() {
		log.info("before all called.");
	}

	@AfterAll
	public void cleanUp() {
		log.info("after all called.");
	}
}
