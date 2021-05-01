package com.secres.secresmail_lib;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

/**
 * A utility class that sends an e-mail message with attachments.
 * 
 * @author www.codejava.net
 * @author Pranav Amarnath
 *
 */
public class EmailUtility {

	public static void sendEmail(Properties smtpProperties, String toAddress, String subject, String message, File[] attachFiles) throws AddressException, MessagingException, IOException {

		final String username = smtpProperties.getProperty("mail.user");
		final String password = smtpProperties.getProperty("mail.password");

		// creates a new e-mail message
		Message msg = new MimeMessage(Session.getInstance(smtpProperties));

		msg.setFrom(new InternetAddress(username));
		InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
		msg.setRecipients(Message.RecipientType.TO, toAddresses);
		msg.setSubject(subject);
		msg.setSentDate(new Date());

		// creates message part
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(message, "text/html");

		// creates multi-part
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);

		// adds attachments
		if(attachFiles != null && attachFiles.length > 0) {
			for(File aFile : attachFiles) {
				MimeBodyPart attachPart = new MimeBodyPart();

				try {
					attachPart.attachFile(aFile);
				} catch (IOException ex) {
					throw ex;
				}

				multipart.addBodyPart(attachPart);
			}
		}

		// sets the multi-part as e-mail's content
		msg.setContent(multipart);

		// sends the e-mail
		Transport.send(msg, username, password);
	}

}
