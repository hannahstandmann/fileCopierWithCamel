package org.smbaiwsy.file_copier_with_camel;

import java.io.UnsupportedEncodingException;

import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;
/**
 * Decodes the RFC2047 encoded text
 * @author anamattuzzi-stojanovic
 *
 */
public class CamelDecoder {
	/**
	 * Decodes the RFC2047 encoded text
	 * @param encoded encoded text 
	 * @return decoded text
	 * @throws ParseException
	 * @throws UnsupportedEncodingException
	 */
	public static String decodeRFC2047(String encoded) throws ParseException,
			UnsupportedEncodingException {
		String decoded = "";
		if (encoded.startsWith("=?")) {
			String[] parts = encoded.split(" ");

			for (String part : parts)
				decoded = decoded.concat(MimeUtility.decodeWord(part));
		}else
			decoded =decoded.concat(encoded);
		return decoded;
	}
}
