package example.micronaut.test.controller;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;
import io.reactivex.Single;

@Client("/hello")
public interface SampleControllerClientApi {
	@Get("/ping")
	HttpResponse<String> callPing();
	@Get("/ping")
	HttpResponse<String> callPingWithHeader(@Header("useFilter") boolean useFilter,@Header(HttpHeaders.AUTHORIZATION) String authHeader);
	@Get("/")
	String callIndex(@Header(HttpHeaders.AUTHORIZATION) String authHeader);
}
