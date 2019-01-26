package org.smbaiwsy.file_copier_with_camel;

import org.apache.camel.Handler;
/**
 * Creates the path to the file 
 * @author anamattuzzi-stojanovic
 *
 */
public class FilePathCreatorBean {
	private String path ="file:foo";
	@Handler
	public String createFilePath(String body){
		 path = "file:"+body +"?noop=true";
		 return path;
	}
	public String getPath(){
		return path;
	}
}
