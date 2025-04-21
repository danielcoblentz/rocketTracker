package com.rockettracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.sterotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class EmailService {
    @Autowired
    private javaMailSender emailSender;
}
