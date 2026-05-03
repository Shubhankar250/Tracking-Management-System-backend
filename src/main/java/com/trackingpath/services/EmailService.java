package com.trackingpath.services;

import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.trackingpath.entities.ReportSchedule;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.UserRepository;

import java.io.File;
import java.util.Properties;

@Service
public class EmailService {
	  @Autowired
	    UserRepository userRepository;
    public void sendReportEmail(ReportSchedule schedule, File file) throws Exception {

        // ✅ USER FETCH (ASSUMPTION: schedule me user link hai)
    	  Users user = userRepository.findById(schedule.getUserId())
                  .orElseThrow(() -> new RuntimeException("User not found for id: " + schedule.getUserId()));

        // ==============================
        // ✅ DYNAMIC SMTP CONFIG
        // ==============================
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(user.getSmtpHost());
        mailSender.setPort(Integer.parseInt(user.getSmtpPort()));
        mailSender.setUsername(user.getSmtpUsername());
        mailSender.setPassword(user.getSmtpPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        // ==============================
        // EMAIL BUILD
        // ==============================
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // ✅ FROM from USER TABLE
        helper.setFrom(user.getSmtpUsername());

        helper.setTo(splitEmails(schedule.getEmailTo()));

        if (schedule.getEmailCc() != null && !schedule.getEmailCc().isBlank()) {
            helper.setCc(splitEmails(schedule.getEmailCc()));
        }

        // ==============================
        // SUBJECT
        // ==============================
        String subject = (schedule.getSubject() != null && !schedule.getSubject().isBlank())
                ? schedule.getSubject()
                : "Scheduled Report - " + schedule.getScheduleName();

        // ==============================
        // BODY
        // ==============================
        String body = (schedule.getEmail_body() != null && !schedule.getEmail_body().isBlank())
                ? schedule.getEmail_body()
                : "Please find attached scheduled report.";

        helper.setSubject(subject);
        helper.setText(body, true);

        // ==============================
        // ATTACHMENT
        // ==============================
        if (file != null && file.exists()) {
            helper.addAttachment(file.getName(), new FileSystemResource(file));
        }

        // ==============================
        // LOGS
        // ==============================
        System.out.println("SMTP HOST: " + user.getSmtpHost());
        System.out.println("FROM: " + user.getSmtpUsername());
        System.out.println("TO: " + schedule.getEmailTo());

        // ==============================
        // SEND
        // ==============================
        mailSender.send(message);
    }

    private String[] splitEmails(String emails) {
        return emails.split("\\s*,\\s*");
    }
}