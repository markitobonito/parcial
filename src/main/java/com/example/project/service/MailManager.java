package com.example.project.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailManager {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender; // Esto es el "from" del correo

    public MailManager(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendMessage(String toEmail, String subject, String messageText) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(messageText, true); // true para HTML, false para texto plano
            helper.setFrom(sender);
            javaMailSender.send(message);
            System.out.println("Correo enviado a: " + toEmail); // Para depuración
        } catch (MessagingException e) {
            System.err.println("Error al enviar correo: " + e.getMessage()); // Para depuración
            throw new RuntimeException("Error al enviar el correo.", e);
        }
    }
}