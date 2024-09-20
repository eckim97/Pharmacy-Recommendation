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

    @PostMapping("/send-pharmacy-data")
    public String sendPharmacyData() {
        int count = pharmacyProducer.sendPharmacyDataFromCsv();
        return "Pharmacy data sent to Kafka. Total messages sent: " + count;
    }
}
