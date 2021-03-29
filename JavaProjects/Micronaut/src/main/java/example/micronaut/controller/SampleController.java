/**
 *
 */
package example.micronaut.controller;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.annotation.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

/**
 * @author sankha
 *
 */

@Controller("/hello")
public class SampleController {
	Logger log = LoggerFactory.getLogger(this.getClass());

	@Get("/")
	public HttpResponse<String> index(@Header(HttpHeaders.AUTHORIZATION) String auth) {
		if(auth.isEmpty() || auth.equalsIgnoreCase("null"))
			return HttpResponse.status(HttpStatus.NOT_ACCEPTABLE);
		return HttpResponse.ok("Hello "+auth+" in Micronaut");
	}

	@Get("/ping")
	HttpResponse<String> ping() {
		return HttpResponse.ok("Pong!!!");
	}
}