package fileCopierWithCamel;

import java.io.UnsupportedEncodingException;

import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

import org.junit.Test;
/**
 * 
 * @author anamattuzzi-stojanovic
 *
 */
public class MimeUtilityTest {
	
	@Test
	public void testdecoding() throws ParseException, UnsupportedEncodingException{
		String encoded ="=?ISO-8859-1?Q?Ringstra=DFe_70,_70736_Fellbach_nach_Buschl?= =?ISO-8859-1?Q?estra=DFe,_70178_Stuttgart_-_Google_Maps.pdf?=";
		String[] parts = encoded.split(" ");
		String decoded = "";
		for (String part:parts)
			decoded= decoded.concat(MimeUtility.decodeWord(part));
		System.out.println(decoded);
	}
}
