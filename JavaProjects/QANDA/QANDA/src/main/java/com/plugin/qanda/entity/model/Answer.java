/**
 * 
 */
package com.plugin.qanda.entity.model;

import com.plugin.qanda.base.model.BaseModel;

/**
 * @author Sankha
 *
 */
public class Answer extends BaseModel {
	
	private String content,questionId;
	private Long voteCount;
	private Boolean isCorrect;
	
	public Answer() {
	}
	
	public String getQuestionId() {
		return questionId;
	}


	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public Answer(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Long getVoteCount() {
		return voteCount;
	}
	public void setVoteCount(Long voteCount) {
		this.voteCount = voteCount;
	}
	public Boolean getIsCorrect() {
		return isCorrect;
	}
	public void setIsCorrect(Boolean isCorrect) {
		this.isCorrect = isCorrect;
	}
	

}
