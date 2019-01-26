package org.smbaiwsy.file_copier_with_camel;

import org.apache.camel.Handler;
/**
 * Does nothing
 * @author anamattuzzi-stojanovic
 *
 */
public class ToUpperCaseBean {
	@Handler
	public String toUpperCase(String message){
		return message;
	}

}
