package com.example.phamnav.kafka.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Service
public class PharmacyProducer {

    private static final Logger log = LoggerFactory.getLogger(PharmacyProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${pharmacy.csv.filepath}")
    private String csvFilePath;

    public PharmacyProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {
        kafkaTemplate.send("pharmacy-data", message)
                .addCallback(
                        result -> log.info("Message sent successfully: {}", message),
                        ex -> log.error("Failed to send message: {}", message, ex)
                );
    }

    public int sendPharmacyDataFromCsv() {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                sendMessage(line);
                count++;
                if (count % 100 == 0) {
                    log.info("Sent {} messages to Kafka", count);
                }
            }
            log.info("Total {} messages sent to Kafka", count);
        } catch (IOException e) {
            log.error("Error reading CSV file: {}", e.getMessage());
        }
        return count;
    }
}
