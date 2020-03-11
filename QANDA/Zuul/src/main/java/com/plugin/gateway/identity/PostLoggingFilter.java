package com.plugin.gateway.identity;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class PostLoggingFilter extends ZuulFilter {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("identity.url")
	private String identityUrl;

	@Override
	public Object run() throws ZuulException {
		RequestContext currentContext = RequestContext.getCurrentContext();
		HttpServletRequest request = currentContext.getRequest();
		logger.info("Zuul PostLoggingFilter request -> {}, request uri -> {}", request, request.getRequestURI());
		if(identityUrl.equals(currentContext.getRequest().getRequestURI()) &&
                currentContext.getResponse().getStatus() == HttpStatus.OK.value()){
            logger.info("request is authorized -> {}, request uri -> {}", request, request.getRequestURI());
            // get the user from the request

        }
		return null;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public int filterOrder() {
		return 1;
	}

	@Override
	public String filterType() {
		return "post";
	}
}
