package com.sdgt.gateway.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.sdgt.gateway.service.CustomUserDetailsService;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	JwtTokenProvider jwtTokenProvider;
	@Value("${security.permitAll:false}")
	private boolean permitAll;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		UserDetailsService userDetailsService = mongoUserDetails();
		auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());

	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry security = http.httpBasic()
				.disable().csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and().authorizeRequests();
		if (permitAll) {
			security.anyRequest().permitAll();
		} else {
			security.antMatchers("/api/auth/login").permitAll().antMatchers("/api/auth/register").permitAll()
					.antMatchers("/id-service/users/login").permitAll().antMatchers(HttpMethod.OPTIONS, "/oauth/token")
					.permitAll().antMatchers(HttpMethod.OPTIONS).permitAll()
					.antMatchers(HttpMethod.POST, "/appconfig-service/defaultConfiguration/save").hasAuthority("USER")
					.antMatchers(HttpMethod.GET, "/appconfig-service/defaultConfiguration/").hasAuthority("ADMIN")
					.antMatchers("/opd-service/**").hasAuthority("ADMIN").antMatchers("/cart/**").hasAuthority("USER")
					.antMatchers("/api/products").hasAuthority("USER").anyRequest().authenticated().and().csrf()
					.disable().exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint()).and()
					.apply(new JwtConfigurer(jwtTokenProvider));
		}
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**")
				.antMatchers(HttpMethod.OPTIONS);
	}

	@Bean
	public PasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public AuthenticationEntryPoint unauthorizedEntryPoint() {
		return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
				"Unauthorized/Invalid token");
	}

	@Bean
	public UserDetailsService mongoUserDetails() {
		return new CustomUserDetailsService();
	}
}
