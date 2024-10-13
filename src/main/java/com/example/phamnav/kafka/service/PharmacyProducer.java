package com.example.phamnav.kafka.service;

import com.example.phamnav.direction.entity.Direction;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class PharmacyProducer {

    private static final Logger log = LoggerFactory.getLogger(PharmacyProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendPharmacyData(Direction direction) {
        try {
            String pharmacyData = objectMapper.writeValueAsString(direction);
            kafkaTemplate.send("pharmacy-topic", pharmacyData);
            log.info("Pharmacy data sent to Kafka: {}", pharmacyData);
        } catch (Exception e) {
            log.error("Error sending pharmacy data to Kafka", e);
        }
    }
}