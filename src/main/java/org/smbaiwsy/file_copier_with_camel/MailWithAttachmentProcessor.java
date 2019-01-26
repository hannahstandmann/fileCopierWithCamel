package org.smbaiwsy.file_copier_with_camel;


import java.util.Map;

import javax.activation.DataHandler;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Processor that handles the e-mail attachments
 * @author anamattuzzi-stojanovic
 *
 */
public class MailWithAttachmentProcessor implements Processor {
	private static final Logger LOG = LoggerFactory.getLogger(MailWithAttachmentProcessor.class);
	
	public void process(Exchange exchange) throws Exception {
	     // the API is a bit clunky so we need to loop
	     Map<String, DataHandler> attachments = exchange.getIn().getAttachments();
	     if (attachments.size() > 0) {
	    	 LOG.debug("There are " + attachments.size() + " attachments");
	         for (String name : attachments.keySet()) {
	             DataHandler dh = attachments.get(name);
	             // get the file name
	             String filename = dh.getName();
	             filename = CamelDecoder.decodeRFC2047(filename);
	             //filename = filename.replaceAll(" ", "_");
	             LOG.debug("Name is: "+ name);
	             LOG.debug("Filename is:"+filename);
	             exchange.getOut().setHeader("CamelFileName", filename);
	             
	             exchange.getOut().setBody(dh.getInputStream());
	         }
	     }
	}

}
