package org.smbaiwsy.file_copier_with_camel;

import org.apache.camel.spring.Main;

/**
 * The application starter that reads the XMl configuration
 * @author anamattuzzi-stojanovic
 *
 */
public class FileCopierWithCamelXML{
	/**
	 * the main method
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		Main main = new Main();
		main.enableHangupSupport();
		main.setApplicationContextUri("META-INF/spring/camel-context.xml");
		//main.addRouteBuilder(fcwc);
		main.run(args);
	}
}
