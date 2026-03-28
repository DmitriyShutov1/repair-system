package com.system.users.service;

import com.system.users.entity.Branch;
import com.system.users.entity.UserAccount;
import com.system.users.repository.BranchRepository;
import com.system.users.repository.UserAccountRepository;
import com.system.users.DTO.BranchRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;


import java.util.List;


@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendCredentials(String to, String phone, String password) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Account created");

        message.setText("""
                Your account has been created.

                Login: %s
                Phone: %s
                Password: %s

                Please change password after first login.
                """.formatted(to != null ? to : phone, phone, password));

        mailSender.send(message);
    }

    public void sendNewPassword(String to, String password) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Password reset");

        message.setText("""
                Your password has been reset.

                New password: %s

                Please change it immediately after login.
                """.formatted(password));

        mailSender.send(message);
    }
}