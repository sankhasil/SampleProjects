package example.micronaut.controller.filter;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.FilterChain;
import io.micronaut.http.filter.HttpFilter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import javax.inject.Singleton;
import org.reactivestreams.Publisher;

@Filter("/**")
public class SampleFilter implements HttpServerFilter {

	@Override
	public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
		if(request.getHeaders().contains("useFilter") && request.getHeaders().getFirst("useFilter",Boolean.class,Boolean.FALSE).booleanValue()){
			request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
			return Publishers.just(HttpResponse.ok("Filter Used."));
		}
		return chain.proceed(request);
	}

}
