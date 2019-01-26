package org.smbaiwsy.file_copier_with_camel;

import java.io.IOException;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.util.ByteArrayDataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.mail.MailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create Attachment out of the message body and add links to the files that
 * will be copied to the appropriate folders after the message is split
 * 
 * @author anamattuzzi-stojanovic
 *
 */
public class AttachmentCreateProcessor implements Processor {
	private static final Logger LOG = LoggerFactory.getLogger(AttachmentCreateProcessor.class);

	/**
	 * create Attachment out of message body add links to the files that will be
	 * created after the message is split
	 */
	public void process(Exchange exchange) throws Exception {
		MailMessage mailMessage = (MailMessage) exchange.getIn();

		String text = getText(mailMessage.getMessage());
		Map<String, DataHandler> attachments = exchange.getIn().getAttachments();
		if (attachments.size() > 0) {
			for (String name : attachments.keySet()) {
				DataHandler dh = attachments.get(name);
				// get the file name
				String filename = CamelDecoder.decodeRFC2047(dh.getName());
				LOG.debug("decoded filename is " + filename);
				String link = createLink(filename, exchange.getContext());
				text = text.concat(link).concat("<br />");
			}
		}
		byte[] newAttachment = text.getBytes("UTF-8");
		exchange.getIn().setHeader("CamelFileName", "message.mail");
		exchange.getIn().addAttachment("message.mail",
				new DataHandler(new ByteArrayDataSource(newAttachment, "application/octet-stream")));

	}

	/**
	 * Creates the HTML link to the downloaded file
	 * @param filename the name of the file
	 * @param context Camel context
	 * @return HTML link tag
	 * @throws Exception  exception
	 */
	private String createLink(String filename, CamelContext context) throws Exception {
		String[] parts = filename.split("\\.");
		StringBuilder sb = new StringBuilder();
		sb.append("<a href='");
		String key = "{{".concat(parts[1]).concat(".").concat("upload}}");
		sb.append(context.resolvePropertyPlaceholders(key).toString());
		sb.append("/");
		sb.append(filename);
		sb.append("' target='_blank'>");
		sb.append(parts[0]);
		sb.append("</a>");
		return sb.toString();
	}
	
	/**
	 * Gets the text of the email message
	 * @param p the part of the multi-part message
	 * @return the text of the e-mail message
	 * @throws MessagingException
	 * @throws IOException
	 */
	private String getText(Part p) throws MessagingException, IOException {

		if (p.isMimeType("text/*")) {
			String s = (String) p.getContent();
			return s;
		}

		if (p.isMimeType("multipart/alternative")) {
			// prefer html text over plain text
			Multipart mp = (Multipart) p.getContent();
			String text = null;
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);
				if (bp.isMimeType("text/plain")) {
					if (text == null)
						text = getText(bp);
					continue;
				} else if (bp.isMimeType("text/html")) {
					String s = getText(bp);
					if (s != null)
						return s;
				} else {
					return getText(bp);
				}
			}
			return text;
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				String s = getText(mp.getBodyPart(i));
				if (s != null)
					return s;
			}
		}

		return null;
	}

}
