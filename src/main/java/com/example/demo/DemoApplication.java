package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Date;

@SpringBootApplication

public class DemoApplication {
    @Autowired
    private JavaMailSender mailSender;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Scheduled(fixedRate = 3600000L)
    void send() throws InterruptedException, MessagingException, IOException {
        Message message = new Message();
        mailSender.send(message.emailMessage());
        System.out.println("Now is" + new Date());
    }

}
