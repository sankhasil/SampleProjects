/**
 * 
 */
package com.plugin.gateway.property;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Sankha
 *
 */
@ConfigurationProperties(prefix = "audit")
public class AuditConfigurationProperties {

	@NotNull
	private String storageEngine;

	/**
	 * @return the storageEngine
	 */
	public String getStorageEngine() {
		return storageEngine;
	}

	/**
	 * @param storageEngine the storageEngine to set
	 */
	public void setStorageEngine(String storageEngine) {
		this.storageEngine = storageEngine;
	}


	
	
}
