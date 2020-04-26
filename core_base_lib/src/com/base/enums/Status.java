package com.base.enums;

public enum Status {
	 NEW("new"), TAGGED("tagged"), DEACTIVE("deactive"),HIDDEN("hidden");

	String value;

	public String getValue() {
		return value;
	}

	private Status(String value) {
		this.value = value;
	}
}
