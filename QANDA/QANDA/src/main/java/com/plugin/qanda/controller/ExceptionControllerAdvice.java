
package com.plugin.qanda.controller;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.plugin.qanda.base.rest.model.Response;

/**
 *
 * @author Sankha
 */
@ControllerAdvice
public class ExceptionControllerAdvice {

	@Value("${spring.profiles.active:local}")
	private String activeEnvProfile;
	protected Logger logger = LoggerFactory.getLogger(ExceptionControllerAdvice.class);
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> exceptionHandler(Exception ex) {
        if(activeEnvProfile.equalsIgnoreCase("local")) 
            ex.printStackTrace();
        else if(activeEnvProfile.equalsIgnoreCase("dev"))
        	logger.error(ExceptionUtils.getStackTrace(ex));
        
        Response error = new Response();
        error.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setMessage(ExceptionUtils.getStackTrace(ex));
        return new ResponseEntity<Response>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
