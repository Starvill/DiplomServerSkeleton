package com.example.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Random;

@Service
public class EmailService {


    private final JavaMailSender mailSender;
    private final Random random = new Random();

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String sendVerificationCode(String toEmail) {
        String code = generateCode();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject("Ваш код подтверждения для входа Liquid");
            helper.setText("Здравствуйте!\n" +
                    "Ваш код подтверждения:: " + code + "\n" +
                    "Введите его на Liquid для завершения регистрации.\n" +
                    "Если вы не запрашивали код, проигнорируйте это письмо.");
            mailSender.send(message);
            System.out.println("Sending code");
        } catch (MessagingException e) {
            throw new RuntimeException("Ошибка отправки email", e);
        }
        return code;
    }

    private String generateCode() {
        return String.format("%06d", random.nextInt(1000000));
    }
}

