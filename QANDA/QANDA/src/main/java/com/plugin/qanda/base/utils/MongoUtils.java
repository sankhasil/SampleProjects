/**
 * 
 */
package com.plugin.qanda.base.utils;

import java.util.regex.Pattern;

/**
 * @author Sankha
 *
 */
public final class MongoUtils {
	public static String escapeMetaCharacters(String inputString){
	    final String[] metaCharacters = {"\\","^","$","{","}","[","]","(",")",".","*","+","?","|","<",">","-","&","%"};

	    for (int i = 0 ; i < metaCharacters.length ; i++){
	        if(inputString.contains(metaCharacters[i])){
	            inputString = inputString.replace(metaCharacters[i],"\\"+metaCharacters[i]);
	        }
	    }
	    return inputString;
	}

	public static boolean checkIfHasSpecialCharacter(String searchData) {
		Pattern special = Pattern.compile ("[!@#$%&*()_+=|<>?{}\\[\\]~-]");
		return special.matcher(searchData).find();
	}
}
