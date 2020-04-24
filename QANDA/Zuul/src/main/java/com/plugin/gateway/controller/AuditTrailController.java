/**
 * 
 */
package com.sdgt.gateway.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sdgt.gateway.enums.AuditStorageEngine;
import com.sdgt.gateway.property.AuditConfigurationProperties;
import com.sdgt.gateway.repository.AuditTrailRepository;
import com.sdgt.gateway.trail.model.AuditTrail;

/**
 * @author Sankha
 *
 */
@CrossOrigin
@RestController
@RequestMapping(value = { "/auditTrail", "/audittrail", "/AuditTrail" })
public class AuditTrailController {

	AuditTrailRepository auditTrailRepository;
	private Logger controllerLogger = LoggerFactory.getLogger(AuditTrailController.class);

	@Autowired
	public AuditTrailController(AuditConfigurationProperties auditProperties) {
		AuditStorageEngine storageEngineEnum = Arrays.asList(AuditStorageEngine.values()).stream()
				.filter(predicate -> predicate.getValue().equalsIgnoreCase(auditProperties.getStorageEngine()))
				.findFirst().orElse(AuditStorageEngine.FILESYSTEM);
		auditTrailRepository = new AuditTrailRepository(storageEngineEnum);
	}

	@GetMapping("/{id}")
	public AuditTrail getTrailDataById(@PathVariable("id") String id) {
		return auditTrailRepository.findById(id);
	}

	@GetMapping(value = { "/", "" })
	public List<AuditTrail> getAllTrailData() {
		return auditTrailRepository.findAll();
	}

	@PostMapping(value = { "/search", "/Search" }, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public List<AuditTrail> searchFailedData(@RequestBody String object) {
		if (StringUtils.isNotBlank(object))
			return auditTrailRepository.search(new JsonParser().parse(object).getAsJsonObject());

		return Collections.emptyList();
	}

	@PatchMapping(value = { "/{id}" })
	public ResponseEntity<String> updateTrailObject(@RequestHeader("x-username") String user,
			@PathVariable("id") String id, @RequestBody String object) {
		if (StringUtils.isNotBlank(object)) {
			JsonObject patchObject = new JsonParser().parse(object).getAsJsonObject();
			patchObject.addProperty("updatedBy", user);
			boolean updateStatus = auditTrailRepository.updateById(id, patchObject);
			if (updateStatus) {
				JsonObject response = new JsonObject();
				response.addProperty("status", HttpStatus.OK.toString());
				response.addProperty("message", "Audit Trail Updated");
				return ResponseEntity.ok(response.toString());
			} else {
				return ResponseEntity.noContent().build();
			}
		}
		return ResponseEntity.badRequest().build();
	}

	// TODO: work on bulk update
}
