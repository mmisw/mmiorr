package org.mmisw.orrclient.core.util;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.PasswordAuthentication;
import javax.mail.Authenticator;

import com.sun.mail.smtp.SMTPTransport;
import org.mmisw.orrportal.gwt.server.OrrConfig;

/**
 * Helper to send email.
 * 
 * @author Carlos Rueda
 */
// Based on JavaMail demo programs by Max Spivak and Bill Shannon
public class MailSender {

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
			final String user, final String password,
			boolean debug,
			String from, String to, String replyTo,
			String subject,
			String text
	) throws Exception {
		
		final String mailer   = OrrConfig.instance().emailMailer;
		final String mailhost = OrrConfig.instance().emailServerHost;
		final String mailport = OrrConfig.instance().emailServerPort;
		final String prot     = OrrConfig.instance().emailServerProt;

		String cc = null, bcc = null;

		Properties props = System.getProperties();
		
		props.put("mail." + prot + ".host", mailhost);
	    props.put("mail." + prot + ".auth", "true");
	    if ( mailport != null ) {
	    	props.put("mail." + prot + ".port", mailport);
	    }

		Session session = Session.getInstance(props,
				new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(user, password);
					}
				});

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