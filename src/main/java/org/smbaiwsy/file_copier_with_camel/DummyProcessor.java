package org.smbaiwsy.file_copier_with_camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
/**
 * 
 * @author anamattuzzi-stojanovic
 *
 */
public class DummyProcessor implements Processor {

	public void process(Exchange exchange) throws Exception {
		String payload = exchange.getIn().getBody(String.class);
		System.out.println(payload);
		exchange.getIn().setBody("Bye");
	}

}
