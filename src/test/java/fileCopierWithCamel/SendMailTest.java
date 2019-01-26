package fileCopierWithCamel;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;
/**
 * 
 * @author anamattuzzi-stojanovic
 *
 */
public class SendMailTest {

	@Test
	public void testSendHeaders() throws Exception {
		CamelContext context = new DefaultCamelContext();
		context.start();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("To", "smbaiwsy@gmail.com");
		map.put("From", "smbaiwsy@gmail.com");
		map.put("Subject", "Camel rocks");

		String body = "Hello there.\n Camel rocks. Yes it does.\n\nRegards Ana.";
		ProducerTemplate template = context.createProducerTemplate();
		template.sendBodyAndHeaders("smtps://smtp.gmail.com?username=yourmailhere@gmail.com&password=yourpasswordhere", body,
				map);
	}
}
