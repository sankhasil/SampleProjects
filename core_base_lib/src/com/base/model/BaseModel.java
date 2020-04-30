/**
 * 
 */
package com.base.model;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.hibernate.annotations.GenericGenerator;

import com.base.enums.Status;

/**
 * @author Sankha
 *
 */
@MappedSuperclass
public class BaseModel {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String createdBy, updatedBy;
	private Long createdDateTimeInEpoch, updatedDateTimeInEpoch;
	private Status status;
	private Date createdOn, modifiedOn;

	@PrePersist
	void createdAt() {
		this.createdOn = new Date();
		setCreatedDateTimeInEpoch(Instant.now().getEpochSecond());
	}

	@PreUpdate
	void updatedAt() {
		this.modifiedOn = new Date();
		setUpdatedDateTimeInEpoch(Instant.now().getEpochSecond());
	}

	public Long getId() {
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
	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(Date modifiedOn) {
		this.modifiedOn = modifiedOn;
	}


}
