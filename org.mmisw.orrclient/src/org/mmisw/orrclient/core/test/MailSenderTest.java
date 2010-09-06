package org.mmisw.orrclient.core.test;

import org.mmisw.orrclient.core.util.MailSender;

/**
 * Test of MailSender.
 */
public class MailSenderTest {

	/**
	 * @param args 2 args expected: username and password of the account to be used to send the email.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		final String user = args[0];
		final String password = args[1];
		final boolean debug = true;
		final String from = "MMI-ORR <techlead@marinemetadata.org>";
		final String to = "carueda@gmail.com";
		final String replyTo = "techlead@marinemetadata.org";
		final String subject = "MailSenderTest";
		final String text = "test message";
		
		MailSender.sendMessage(user, password, debug, from, to, replyTo, subject, text);

		if ( debug ) {
			System.out.println("\nMail was sent successfully.");
		}
	}
}