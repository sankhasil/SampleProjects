package com.plugin.gateway.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

/**
 * The @EnableResourceServer annotation adds a filter of type
 * OAuth2AuthenticationProcessingFilter automatically to the Spring Security
 * filter chain.
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

	@Value("${security.permitAll:false}")
	private boolean permitAll;

	@Override
	public void configure(HttpSecurity http) throws Exception {
		ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry security = http.headers()
				.frameOptions().disable().and().authorizeRequests();
		if (permitAll) {
			security.anyRequest().permitAll();
		} else {
			security.antMatchers("/api/auth/register").permitAll().antMatchers(HttpMethod.OPTIONS, "/oauth/token")
					.permitAll().antMatchers(HttpMethod.OPTIONS).permitAll().antMatchers("/id-service/users/login")
					.permitAll().antMatchers(HttpMethod.POST, "/audit-service/**").hasAuthority("ADMIN")
					.antMatchers("/opd-service/**").hasAuthority("ADMIN").anyRequest().authenticated();
		}

	}

}
