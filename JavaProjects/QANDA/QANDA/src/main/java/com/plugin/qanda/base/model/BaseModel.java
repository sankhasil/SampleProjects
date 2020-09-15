/**
 * 
 */
package com.plugin.qanda.base.model;

import org.springframework.data.annotation.Id;

import com.plugins.qanda.base.enums.Status;

/**
 * @author Sankha
 *
 */
public class BaseModel {
	@Id private String id;
	private String createdBy,updatedBy;
	private Long createdDateTimeInEpoch,updatedDateTimeInEpoch;
	private Status status;
	public String getId() {
		return id;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	public Long getCreatedDateTimeInEpoch() {
		return createdDateTimeInEpoch;
	}
	public void setCreatedDateTimeInEpoch(Long createdDateTimeInEpoch) {
		this.createdDateTimeInEpoch = createdDateTimeInEpoch;
	}
	public Long getUpdatedDateTimeInEpoch() {
		return updatedDateTimeInEpoch;
	}
	public void setUpdatedDateTimeInEpoch(Long updatedDateTimeInEpoch) {
		this.updatedDateTimeInEpoch = updatedDateTimeInEpoch;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}

	
}
