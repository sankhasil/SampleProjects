package com.plugin.gateway.identity;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class AuthenticationFilter extends ZuulFilter {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private HazelcastInstance hazel;
	@Override
	public Object run() throws ZuulException {
    	HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
		logger.info("request -> {}, request uri -> {}", request, request.getRequestURI());
		if(true){
			//if login allow
			// if restricted 401 non auth
		}
		Map<Object, Object> map = hazel.getMap("logged-users");
		String key = "X-USER";
		String userId = request.getHeader(key);
		if(userId !=null ){
			if(!map.containsKey(key)){
				 map.put(key,userId);
			}
			System.out.println(map.get(key));
		}
		//if request header has a token
        // validate the token life time
        //validate the user authority
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
		return "pre";
	}
}
