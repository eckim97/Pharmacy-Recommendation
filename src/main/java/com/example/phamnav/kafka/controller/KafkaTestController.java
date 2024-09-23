package com.example.phamnav.kafka.controller;

import com.example.phamnav.kafka.service.PharmacyProducer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaTestController {

    private final PharmacyProducer pharmacyProducer;

    public KafkaTestController(PharmacyProducer pharmacyProducer) {
        this.pharmacyProducer = pharmacyProducer;
    }

    @PostMapping("/send-pharmacy-data-to-redis")
    public String sendPharmacyDataToRedis() {
        long startTime = System.currentTimeMillis();
        int count = pharmacyProducer.sendPharmacyDataToRedis();
        long endTime = System.currentTimeMillis();
        return "Pharmacy data sent to Redis. Total messages sent: " + count + ". Time taken: " + (endTime - startTime) + " ms";
    }

    @PostMapping("/send-pharmacy-data-to-kafka")
    public String sendPharmacyDataToKafka() {
        long startTime = System.currentTimeMillis();
        int count = pharmacyProducer.sendPharmacyDataFromCsv();
        long endTime = System.currentTimeMillis();
        return "Pharmacy data sent to Kafka. Total messages sent: " + count + ". Time taken: " + (endTime - startTime) + " ms";
    }
}
