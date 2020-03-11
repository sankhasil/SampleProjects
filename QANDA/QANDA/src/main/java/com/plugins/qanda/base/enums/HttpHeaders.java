package com.plugins.qanda.base.enums;

/**
 * 
 * @author Sankha
 *
 */
public enum HttpHeaders {
	USER_NAME("x-username"), USER_ID("x-userid"), ACTION("x-action"), ROLE("x-role");

	String value;

	public String getValue() {
		return value;
	}

	private HttpHeaders(String value) {
		this.value = value;
	}
}
