// File: EmailService.java
package com.bicycle.marketplace.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendMagicLink(String toEmail, String magicLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("Sport Bike Exchange <noreply@sportbike.com>");
        message.setTo(toEmail);
        message.setSubject("Đăng nhập vào Sport Bike Exchange");

        String emailContent = "Chào bạn,\n\n"
                + "Bạn vừa yêu cầu đăng nhập vào hệ thống của chúng tôi.\n"
                + "Vui lòng click vào đường link bên dưới để truy cập ngay (Link có hiệu lực trong 15 phút):\n\n"
                + magicLink + "\n\n"
                + "Nếu bạn không yêu cầu, vui lòng bỏ qua email này.";

        message.setText(emailContent);
        mailSender.send(message);
    }
}