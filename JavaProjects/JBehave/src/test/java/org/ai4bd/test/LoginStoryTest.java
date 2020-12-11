/**
 * 
 */
package org.ai4bd.test;

import java.util.List;

import org.ai4bd.test.mappings.SsoLoginStoryMap;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;

/**
 * @author SankyS
 *
 */
public class LoginStoryTest extends JUnitStories {

	@Override
	public Configuration configuration() {
		return new MostUsefulConfiguration().useStoryLoader(new LoadFromClasspath(this.getClass()))
				.useStoryReporterBuilder(new StoryReporterBuilder()
						.withCodeLocation(CodeLocations.codeLocationFromClass(this.getClass()))
						.withFormats(Format.HTML,Format.CONSOLE));
	}
	
	
	
	@Override
	public InjectableStepsFactory stepsFactory() {
		return new InstanceStepsFactory(configuration(), new SsoLoginStoryMap());
	}
	@Override
	protected List<String> storyPaths() {
		return List.of("sso-login.story");
	}

}
