package org.smbaiwsy.file_copier_with_camel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
/**
 * Reads the file attachment and creates a separate message out of it
 * @author anamattuzzi-stojanovic
 *
 */
public class HttpUploadProcessor implements Processor {

	//@Override
	public void process(Exchange exchange) throws Exception {

		// Read the incoming message
		File file = exchange.getIn().getBody(File.class);
	//	String name = exchange.getIn().getHeader(Exchange.FILE_NAME,
	//			String.class);

		// Encode the file as a multi-part entity
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		Charset chars = Charset.forName("UTF-8");
		builder.setCharset(chars);
	    FileBody fb = new FileBody(file);

	    builder.addPart("file", fb);

		// Set multi-part entity as the outgoing messages body
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		builder.build().writeTo(out);
		InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
		exchange.getOut().setBody(inputStream);
	}

}
