package com.example.demo;

import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

@SpringBootApplication
@EnableScheduling
public class DemoApplication {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${url.viettelbulk}")
    private String urlVtlBulk;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String emailFrom;

    @Value("${spring.mail.properties.mail.smtp.to}")
    private String emailTo;

    @Value("${spring.mail.properties.mail.smtp.cc}")
    private String emailCc;

    @Value("${mimimum.balance}")
    private Double mimumBalanceVtlBulk;

    private static final String XML_BALANCE_VTL_BULK = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:impl=\"http://impl.bulkSms.ws/\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <impl:checkBalance>\n" +
            "         <!--Optional:-->\n" +
            "         <User>smsvnet</User>\n" +
            "         <!--Optional:-->\n" +
            "         <Password>vnet@123</Password>\n" +
            "         <!--Optional:-->\n" +
            "         <CPCode>SMSVNET</CPCode>\n" +
            "      </impl:checkBalance>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

/*
    @Scheduled(fixedRate = 36000L)
    void send() throws InterruptedException, MessagingException, IOException {
        Message message = new Message();
        mailSender.send(message.emailMessage());
        System.out.println("Now is" + new Date());
    }

 */


    /**
     * Canh bao so du cua Viettel Bulk
     * Sdt nhanh canh bao: 0913255236
     * Email: sms@vnet.vn
     * So du canh bao: 5000000
     */
//    @Scheduled(fixedRate = 3600L)
    @Scheduled(cron = "0 */5 * ? * *")
    void scheduleCheckBalanceViettelBulk() throws Exception {
        checkBalanceViettelBulk();
    }
    void checkBalanceViettelBulk() {
        PostMethod post = new PostMethod(urlVtlBulk);
        try {
            StringRequestEntity requestEntity = new StringRequestEntity(XML_BALANCE_VTL_BULK);
            post.setRequestEntity(requestEntity);
            post.setRequestHeader("Content-type",
                    "text/xml; charset=ISO-8859-1");
            HttpClient httpclient = new HttpClient();
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            System.out.println("Response body: ");

            String responseApi = post.getResponseBodyAsString();
//            System.out.println(responseApi);
            String strBalance = getByTagXml("balance", responseApi);
            if (strBalance != null) {
                Double balance = Double.valueOf(strBalance);
                if (balance < mimumBalanceVtlBulk) {
                    DecimalFormat formatter = new DecimalFormat("#,###.00");
//                    System.out.println("*********** SOS BALANCE VIETTEL BULK ************");
                    String titleAlert = "(SOS) So du tk VNET tai VIETTEL BULK dang rat thap: (" + formatter.format(balance) + ") . NAP TIEN NGAY, report time:" + new Date();
                    //String contentAlert = "[SOS] So du tai khoan VNet hien tai la : [" + strBalance + "] . Vui long Nap tien tk VNet o NCC Viettel Bulk ngay";
//                    Message message = new Message();
                    SimpleMailMessage email = getFormatEmail(emailFrom, emailTo, emailCc, titleAlert, titleAlert);
//                    Message message = new Message();
                    mailSender.send(email);
                } else {
                    System.out.println("Balance is OK");
                }
            } else {
                return;
            }
            String strDesc = getByTagXml("errDesc", responseApi);

            //<?xml version="1.0" ?><S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/"><S:Body><ns2:checkBalanceResponse xmlns:ns2="http://impl.bulkSms.ws/"><return><balance>89731189</balance><errCode>0</errCode><errDesc>check balance success</errDesc></return></ns2:checkBalanceResponse></S:Body></S:Envelope>

        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    private String getByTagXml(String tag, String data) {
        try {
//            <balance>89731189</balance>
            String beginTag = "<" + tag + ">";
            int beginIndex = data.indexOf(beginTag);
            int endIndex = data.indexOf("</" + tag + ">");
            String value = data.substring(beginIndex + beginTag.length(), endIndex);
            System.out.println(value);
            return value;
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return null;
    }

    private SimpleMailMessage getFormatEmail(String from, String to, String cc, String subject, String message) throws IOException {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setTo(to);
        mailMessage.setFrom(from);
        mailMessage.setCc(cc);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        return mailMessage;
    }

}
