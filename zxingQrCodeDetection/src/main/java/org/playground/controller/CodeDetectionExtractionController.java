package org.playground.controller;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.playground.service.CodeDetectAndExtractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author AlexN
 * @author SankyS
 */
@RestController
@RequestMapping(value = { "/extraction", "/detectAndExtract" })
public class CodeDetectionExtractionController {

	Logger controllerLogger = LoggerFactory.getLogger(CodeDetectionExtractionController.class);

	@Autowired
	CodeDetectAndExtractService detectAndExtractService;

	@GetMapping("/ping")
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public String ping() {
		return "Pong!!";
	}

	@PostMapping(value = { "/" }, produces = MediaType.APPLICATION_JSON_VALUE,
		      consumes = {
		    	        "application/zip",
		    	        "application/tar",
		    	        MediaType.IMAGE_JPEG_VALUE,
		    	        "image/jpg",
		    	        MediaType.IMAGE_PNG_VALUE
		    	      })
	@ResponseBody
	public ResponseEntity<String> detectAndExtract(@RequestHeader("Content-Type")String contentType,@RequestBody byte[] content) throws JSONException, IOException {
		if(content.length > 0) {
			return ResponseEntity.ok(detectAndExtractService.processImageForCodes(content,contentType).toString(2));
		}
		return ResponseEntity.badRequest().build();
	}

}
