package org.smbaiwsy.file_copier_with_camel;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.mail.SplitAttachmentsExpression;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.websocket.WebsocketComponent;
import org.apache.camel.main.Main;
import org.apache.camel.model.language.ConstantExpression;
import org.smbaiwsy.report_creator.XLSAppender;
/**
 * Camel route builder
 * @author anamattuzzi-stojanovic
 *
 */
public class FileCopierWithCamel extends RouteBuilder {

	Main main;
    /**
     * main method
     * @param args
     * @throws Exception
     */
	public static void main(String args[]) throws Exception {
		Main main = new Main();
		main.enableHangupSupport();
		FileCopierWithCamel fcwc = new FileCopierWithCamel(main);

		main.addRouteBuilder(fcwc);
		main.run(args);
	}
    /**
     * Constructor
     * @param main a command line tool for booting up a CamelContext
     */
	public FileCopierWithCamel(Main main) {
		this.main = main;
		main.bind("excelReporter", new XLSAppender());
	}

	@Override
	public void configure() {
		// initialize properties component
		getContext().addComponent("properties",
				new PropertiesComponent("classpath:attachments.properties"));

		// setup Camel web-socket component on the port we have defined
		WebsocketComponent wc = getContext().getComponent("websocket",
				WebsocketComponent.class);
		int port;
		String wwwroot;
		try {
			port = Integer.valueOf(getContext().resolvePropertyPlaceholders(
					"properties:websocket.port").toString());
			wwwroot = getContext().resolvePropertyPlaceholders(
					"properties:www.root").toString();
		} catch (Exception e) {
			port = 9090;
			wwwroot = "file:www";
		}
		wc.setPort(port);
		// we can serve static resources from the classpath: or file: system
		wc.setStaticResources(wwwroot);

		// initialize activemq
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory
				.setBrokerURL("vm://localhost?broker.persistent=false&broker.useJmx=true");
		JmsComponent jmsc = getContext()
				.getComponent("jms", JmsComponent.class);
		jmsc.setConnectionFactory(connectionFactory);

		from(
				"{{mail.server}}?username={{mail.username}}&password={{mail.password}}"
						+ "&delete=false&unseen=true&consumer.delay=60000").

				process(new AttachmentCreateProcessor())
				.setProperty(Exchange.CHARSET_NAME, new ConstantExpression("UTF-8"))
				.split(new SplitAttachmentsExpression())
				.process(new MailWithAttachmentProcessor()).to("jms:incomingMessages");

		from("jms:incomingMessages").choice()
				.when(header("CamelFileName").endsWith(".pdf"))
				.to("properties:pdf.target")
				.when(header("CamelFileName").endsWith(".txt"))
				.to("properties:txt.target").otherwise().bean(new ToUpperCaseBean())
				.to("log:newmail", "bean:excelReporter")
				.to("websocket:camel-attachments?sendToAll=true").end();

		// from("file://data?noop=true").
		// // Transform the message into multipart/form-data.
		// process(new HttpUploadProcessor()).
		//
		// // Send the message by HTTP POST and log the response.
		//
		// setHeader(Exchange.CONTENT_TYPE, constant("multipart/form-data")).
		// to("jms:incomingMessages");
		// //
		// from("jms:incomingMessages").
		// to("http4:localhost:9090/upload");

	}
}
