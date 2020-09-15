/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.plugin.qanda.base.exception;

import org.springframework.http.HttpStatus;

/**
 *
 * @author Sankha
 */
public class BaseRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3957088912731872875L;
	private HttpStatus http;

	public BaseRuntimeException(String message) {
		super(message);
	}
	public BaseRuntimeException(String message, Throwable e) {
		super(message, e);
	}

	public BaseRuntimeException(String exceptionMessge, Throwable e, HttpStatus http) {
		this(exceptionMessge, e);
		this.http = http;
	}

}
