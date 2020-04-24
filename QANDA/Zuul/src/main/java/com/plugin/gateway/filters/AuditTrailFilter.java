/**
 * 
 */
package com.sdgt.gateway.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.sdgt.gateway.enums.AuditStorageEngine;
import com.sdgt.gateway.property.AuditConfigurationProperties;
import com.sdgt.gateway.repository.AuditTrailRepository;
import com.sdgt.gateway.trail.model.AuditTrail;

/**
 * @author Sankha
 *
 */
@Component
public class AuditTrailFilter extends ZuulFilter {

	AuditTrailRepository auditTrailRepository;
	private Logger logger = LoggerFactory.getLogger(AuditTrailFilter.class);


	/**
	 * 
	 */
	@Autowired
	public AuditTrailFilter(AuditConfigurationProperties auditProperties) {
		AuditStorageEngine storageEngineEnum = Arrays.asList(AuditStorageEngine.values()).stream()
				.filter(predicate -> predicate.getValue().equalsIgnoreCase(auditProperties.getStorageEngine()))
				.findFirst().orElse(AuditStorageEngine.FILESYSTEM);
		auditTrailRepository = new AuditTrailRepository(storageEngineEnum);
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() throws ZuulException {
		HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
		HttpServletResponse response = RequestContext.getCurrentContext().getResponse();
		updateServiceAuditTrail(request, response);
		logger.info("request -> {}, request uri -> {}", request, request.getRequestURI());
		return null;
	}

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 1;
	}

	/**
	 * @param request
	 */
	private void updateServiceAuditTrail(HttpServletRequest request, HttpServletResponse response) {
		String serviceName = request.getRequestURI().split("/")[1];
		if (auditTrailRepository != null) {
			String performedBy = request.getHeader("x-username");
			String action = request.getHeader("x-action") != null ? request.getHeader("x-action") : "";
			String requestMethod = request.getMethod();
			Map<String, Object> headerMap = new HashMap<>();
			Enumeration<String> headerName = request.getHeaderNames();
			while (headerName.hasMoreElements()) {
				String header = headerName.nextElement();
				headerMap.put(header, request.getHeader(header));
			}
			AuditTrail auditObject = new AuditTrail(serviceName, action, requestMethod, request.getRequestURI(),
					performedBy, request.getContextPath(), headerMap);
			if (requestMethod.equals(HttpMethod.POST.name()) || requestMethod.equals(HttpMethod.PUT.name())
					|| requestMethod.equals(HttpMethod.PATCH.name())) {
				try {
					auditObject.setRequestBody(
							request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
				} catch (IOException e) {
					logger.error(ExceptionUtils.getFullStackTrace(e));
				}
			}
			if (!request.getParameterMap().isEmpty()) {
				auditObject.setRequestParams(request.getParameterMap());
			}
			if (StringUtils.isNotBlank(request.getQueryString())) {
				auditObject.setQueryParams(request.getQueryString());
			}
			auditObject.setResponseCode(response.getStatus());
			auditTrailRepository.save(auditObject);
		}
	}
}
