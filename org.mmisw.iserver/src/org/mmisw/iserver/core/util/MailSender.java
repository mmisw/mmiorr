package org.mmisw.iserver.core.util;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.smtp.SMTPTransport;

/**
 * Helper to send email.
 * 
 * @author Carlos Rueda
 */
// Based on JavaMail demo programs by Max Spivak and Bill Shannon
public class MailSender {
	//
	// TODO (low priority) get these params from a configuration resource
	private static final String mailer = "MMI-ORR";
	private static final String mailhost = "smtp.gmail.com";
	private static final String mailport = "465";   //"587"; 
	private static final String prot = "smtps";

	/**
	 * Sends a message.
	 * @param user
	 * @param password
	 * @param debug
	 * @param from
	 * @param to
	 * @param replyTo
	 * @param subject
	 * @param text
	 * @throws Exception
	 */
	public static void sendMessage(
			String user, String password,
			boolean debug,
			String from, String to, String replyTo,
			String subject,
			String text
	) throws Exception {
		
		String cc = null, bcc = null;

		Properties props = System.getProperties();
		
		props.put("mail." + prot + ".host", mailhost);
	    props.put("mail." + prot + ".auth", "true");
	    if ( mailport != null ) {
	    	props.put("mail." + prot + ".port", mailport);
	    }

		Session session = Session.getInstance(props, null);
		if (debug) {
			session.setDebug(true);
		}
		
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(from));

		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
		if (cc != null) {
			msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
		}
		if (bcc != null) {
			msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));
		}
		
		if ( replyTo != null ) {
			msg.setReplyTo(InternetAddress.parse(replyTo, false));
		}

		msg.setSubject(subject);

		msg.setText(text);

		msg.setHeader("X-Mailer", mailer);
		msg.setSentDate(new Date());

		
	    SMTPTransport t = (SMTPTransport) session.getTransport(prot);
	    try {
			t.connect(mailhost, user, password);
			t.sendMessage(msg, msg.getAllRecipients());
		}
		finally {
			System.out.println("Response: " + t.getLastServerResponse());
			t.close();
		}
	}
}