/**
 * 
 */
package com.plugin.qanda.entity.model;

import java.util.ArrayList;
import java.util.List;

import com.plugin.qanda.base.model.BaseModel;

/**
 * @author Sankha
 *
 */
public class Post extends BaseModel {

	private String text,topicId;
	private Boolean isQuestion;
	private List<Answer> answerList;
	private List<String> tagList;
	private List<String> comments;
	public List<String> getComments() {
		return comments;
	}

	public void setComments(List<String> comments) {
		this.comments = comments;
	}


	public Post(String text, Boolean isQuestion) {
		this.text = text;
		this.isQuestion = isQuestion;
	}

	
	public String getTopicId() {
		return topicId;
	}


	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}


	public Boolean getIsQuestion() {
		return isQuestion;
	}

	public void setIsQuestion(Boolean isQuestion) {
		this.isQuestion = isQuestion;
	}


	public List<Answer> getAnswerList() {
		return answerList;
	}

	public void setAnswerList(List<Answer> answerList) {
		this.answerList = answerList;
	}

	public List<String> getTagList() {
		return tagList;
	}

	public void setTagList(List<String> tagList) {
		this.tagList = tagList;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void addAnswer(Answer answer) {
		if(answerList == null) {
			answerList = new ArrayList<>();
		}
		answerList.add(answer);
	}

	public boolean isAQuestion() {
		if(isQuestion!=null)
			return isQuestion.booleanValue();
		return false;
	}
	
	
}
