package com.trackingpath.util;

import java.util.Properties;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;

import org.apache.log4j.Logger;

import com.trackingpath.dtos.MessageDTO;

public class MailUtil {
    private static Logger log = Logger.getLogger(MailUtil.class);
    private static final String FROM_EMAIL = "noreply@trackingpath.com";

    public static void send(final String recipientEmailAddress, final String recipientCCEmailAddress, final MessageDTO msg, final String attachmentPath) {
        new Thread(new Runnable() {
            public void run() {
                try {
                	final String username = "noreply@trackingpath.com";
            		final String password = "ak34&@%789";

                    Properties props = new Properties();
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.host", "smtp.zoho.com");
                    props.put("mail.smtp.port", "587");
                    props.put("mail.smtp.ssl.protocols", "TLSv1.2");

                    Session session = Session.getInstance(props, new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

                    MimeMessage mimeMessage = new MimeMessage(session);
                    Multipart mimeMultipart = new MimeMultipart();
                    MimeBodyPart messageBodyPart = new MimeBodyPart();
                    messageBodyPart.setContent(msg.getMessageBody(), "text/html; charset=utf-8");
                    mimeMultipart.addBodyPart(messageBodyPart);

                    // Attach the file if attachmentPath is provided
                    if (attachmentPath != null && !attachmentPath.isEmpty()) {
                        addAttachment(mimeMultipart, attachmentPath);
                    }

                    mimeMessage.setFrom(new InternetAddress(FROM_EMAIL));
                    mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmailAddress));
                    if(recipientCCEmailAddress!=null && !recipientCCEmailAddress.isEmpty()) {
                    mimeMessage.setRecipients(Message.RecipientType.CC, InternetAddress.parse(recipientCCEmailAddress));
                    }
                    mimeMessage.setSubject(msg.getMessageTitle());
                    mimeMessage.setContent(mimeMultipart);
                    Transport.send(mimeMessage);

                    log.info("Email Sent Successfully !!");
                    log.info("Mail sent to: " + recipientEmailAddress);
                } catch (MessagingException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }).start();
    }

    private static void addAttachment(Multipart multipart, String attachmentPath) throws MessagingException {
        MimeBodyPart attachmentPart = new MimeBodyPart();
        DataSource source = new FileDataSource(attachmentPath);
        attachmentPart.setDataHandler(new DataHandler(source));
        attachmentPart.setFileName(attachmentPath); // You can set a different name if needed
        multipart.addBodyPart(attachmentPart);
    }

    public static void main(String[] args) {
        MessageDTO bean = new MessageDTO();
        bean.setMessage("Hello");
        bean.setMessageTitle("Greeting Message");
        bean.setMessageBody("This is the message body.");
        String attachmentPath = "path_to_your_attachment_file.txt"; // Replace with the actual file path
        MailUtil.send("recipient@example.com", "cc@example.com", bean, attachmentPath);
    }
}
