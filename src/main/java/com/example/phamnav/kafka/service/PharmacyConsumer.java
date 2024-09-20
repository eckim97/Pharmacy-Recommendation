package com.example.phamnav.kafka.service;

import com.example.phamnav.pharmacy.entity.Pharmacy;
import com.example.phamnav.pharmacy.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PharmacyConsumer {

    private static final Logger log = LoggerFactory.getLogger(PharmacyConsumer.class);
    private final PharmacyRepository pharmacyRepository;

    @KafkaListener(topics = "pharmacy-data", groupId = "pharmacy-group")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            log.info("Received message: key = {}, value = {}", record.key(), record.value());
            String[] values = record.value().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            if (values.length < 5) {
                log.error("Invalid message format: {}", record.value());
                return;
            }

            Pharmacy pharmacy = Pharmacy.builder()
                    .pharmacyName(values[1].replace("\"", "").trim())
                    .pharmacyAddress(values[2].replace("\"", "").trim())
                    .latitude(Double.parseDouble(values[3].trim()))
                    .longitude(Double.parseDouble(values[4].trim()))
                    .build();

            pharmacyRepository.save(pharmacy);
            log.debug("Saved pharmacy to database: {}", pharmacy.getPharmacyName());

        } catch (Exception e) {
            log.error("Error processing message: {}", record.value(), e);
        }
    }
}