/**
 * 
 */
package com.plugin.gateway.trail.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * @author Sankha
 *
 */
public class AuditTrail implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8040907596160627791L;
	/**
	 * 
	 */
	private UUID id;
	private String serviceName, action, requestMethod,requestUrl,performedBy,updatedBy,contextPath,queryParams,status;
	private Map<String,Object> requestHeaders;
	private Map<String,String[]> requestParams;
	private Object requestBody;
	private int responseCode;
	private Long requestTimestamp,updateTimestamp;
	
	public AuditTrail() {
	}

	
	

	/**
	 * @param id
	 * @param serviceName
	 * @param action
	 * @param requestMethod
	 * @param requestUrl
	 * @param performedBy
	 * @param contextPath
	 * @param requestHeaders
	 */
	public AuditTrail(String serviceName, String action, String requestMethod, String requestUrl,
			String performedBy, String contextPath,
			Map<String, Object> requestHeaders) {
		this.id = UUID.randomUUID();
		this.serviceName = serviceName;
		this.action = action;
		this.requestMethod = requestMethod;
		this.requestUrl = requestUrl;
		this.performedBy = performedBy;
		this.contextPath = contextPath;
		this.requestHeaders = requestHeaders;
		this.requestTimestamp = Instant.now().getEpochSecond();
		this.status = "NEW";
	}




	/**
	 * @return the contextPath
	 */
	public String getContextPath() {
		return contextPath;
	}


	/**
	 * @param contextPath the contextPath to set
	 */
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}


	/**
	 * @return the queryParams
	 */
	public String getQueryParams() {
		return queryParams;
	}


	/**
	 * @param queryParams the queryParams to set
	 */
	public void setQueryParams(String queryParams) {
		this.queryParams = queryParams;
	}


	/**
	 * @return the requestParams
	 */
	public Map<String, String[]> getRequestParams() {
		return requestParams;
	}


	/**
	 * @param requestParams the requestParams to set
	 */
	public void setRequestParams(Map<String, String[]> requestParams) {
		this.requestParams = requestParams;
	}


	/**
	 * @return the requestBody
	 */
	public Object getRequestBody() {
		return requestBody;
	}


	/**
	 * @param requestBody the requestBody to set
	 */
	public void setRequestBody(Object requestBody) {
		this.requestBody = requestBody;
	}


	/**
	 * @return the id
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(UUID id) {
		this.id = id;
	}

	/**
	 * @return the resourceName
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * @param resourceName the resourceName to set
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the requestMethod
	 */
	public String getRequestMethod() {
		return requestMethod;
	}

	/**
	 * @param requestMethod the requestMethod to set
	 */
	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	/**
	 * @return the requestUrl
	 */
	public String getRequestUrl() {
		return requestUrl;
	}

	/**
	 * @param requestUrl the requestUrl to set
	 */
	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	/**
	 * @return the performedBy
	 */
	public String getPerformedBy() {
		return performedBy;
	}

	/**
	 * @param performedBy the performedBy to set
	 */
	public void setPerformedBy(String performedBy) {
		this.performedBy = performedBy;
	}


	/**
	 * @return the requestHeaders
	 */
	public Map<String, Object> getRequestHeaders() {
		return requestHeaders;
	}

	/**
	 * @param requestHeaders the requestHeaders to set
	 */
	public void setRequestHeaders(Map<String, Object> requestHeaders) {
		this.requestHeaders = requestHeaders;
	}

	/**
	 * @return the requestTimestamp
	 */
	public Long getRequestTimestamp() {
		return requestTimestamp;
	}

	/**
	 * @param requestTimestamp the requestTimestamp to set
	 */
	public void setRequestTimestamp(Long requestTimestamp) {
		this.requestTimestamp = requestTimestamp;
	}


	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}




	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}




	/**
	 * @return the updatedBy
	 */
	public String getUpdatedBy() {
		return updatedBy;
	}




	/**
	 * @param updatedBy the updatedBy to set
	 */
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}




	/**
	 * @return the updateTimestamp
	 */
	public Long getUpdateTimestamp() {
		return updateTimestamp;
	}




	/**
	 * @param updateTimestamp the updateTimestamp to set
	 */
	public void setUpdateTimestamp(Long updateTimestamp) {
		this.updateTimestamp = updateTimestamp;
	}







	/**
	 * @return the responseCode
	 */
	public int getResponseCode() {
		return responseCode;
	}




	/**
	 * @param responseCode the responseCode to set
	 */
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}




	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuditTrail other = (AuditTrail) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AuditTrail [ServiceName=" + serviceName + ", action=" + action + ", requestMethod=" + requestMethod
				+ ", requestUrl=" + requestUrl + ", performedBy=" + performedBy  + ", requestTimestamp=" + requestTimestamp + "]";
	}
	
	
}
