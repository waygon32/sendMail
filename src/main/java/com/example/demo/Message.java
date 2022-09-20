package com.example.demo;

import com.sun.management.OperatingSystemMXBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

@Configuration
public class Message {

    public static ArrayList<String> getLink() throws IOException {
        String file ="link.txt";

        BufferedReader reader = new BufferedReader(new FileReader(file));
        ArrayList<String> listLine = new ArrayList<>();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            listLine.add(line);
        }
        reader.close();

       return listLine;
    }
    public static boolean pingURL(String url, int timeout) {
        url = url.replaceFirst("^https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (IOException exception) {
            return false;
        }
    }
    @Bean
    public SimpleMailMessage emailMessage() throws IOException {
//        Properties mailServerProperties;
 //       Session getMailSession;
        SimpleMailMessage mailMessage = new SimpleMailMessage();
//        MimeMessage mailMessage;
        ArrayList<String> listLink = getLink();
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);
        String cpuUsage= String.valueOf(Math.round(osBean.getProcessCpuLoad()*100)) + "%";
        String memoryUsage =String.valueOf(Math.round((1-(Double.valueOf(osBean.getFreePhysicalMemorySize()))/(Double.valueOf(osBean.getTotalPhysicalMemorySize())))*100)) + "%";
        String freeSpace = String.valueOf(new File("c:/").getFreeSpace()/1073741824) + "GB";
        String statusUrl = "";

        for(int i=0; i<listLink.size();i++){
            if(!pingURL(listLink.get(i),8000)){
                statusUrl += "Khong the truy cap den " + listLink.get(i) + "\n";
            }
        }

        mailMessage.setTo("truongthien2310@gmail.com");
        mailMessage.setFrom("huyenhoac23@gmail.com");
        mailMessage.setSubject("System status");
        mailMessage.setText("CPU su dung: " +cpuUsage + "\n"+ "RAM su dung: " +memoryUsage + "\n" + "O nho con trong: " + freeSpace + "\n"+ statusUrl);
        return mailMessage;
    }
}
