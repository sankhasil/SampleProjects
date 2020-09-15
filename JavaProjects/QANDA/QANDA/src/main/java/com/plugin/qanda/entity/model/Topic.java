/**
 * 
 */
package com.plugin.qanda.entity.model;

import java.util.List;

import com.plugin.qanda.base.model.BaseModel;

/**
 * @author Sankha
 *
 */
public class Topic extends BaseModel {
	
	private String title,description;
	private List<Post> listOfPosts;
	
	public List<Post> getListOfPosts() {
		return listOfPosts;
	}

	public void setListOfPosts(List<Post> listOfPosts) {
		this.listOfPosts = listOfPosts;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	 

}
