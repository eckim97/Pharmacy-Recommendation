package com.example.phamnav.kafka.service;

import com.example.phamnav.direction.entity.Direction;
import com.example.phamnav.direction.repository.DirectionRepository;
import com.example.phamnav.pharmacy.entity.Pharmacy;
import com.example.phamnav.pharmacy.repository.PharmacyRepository;
import com.example.phamnav.pharmacy.cache.PharmacyRedisTemplateService;
import com.example.phamnav.pharmacy.dto.PharmacyDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PharmacyConsumer {

    private static final Logger log = LoggerFactory.getLogger(PharmacyConsumer.class);
    private final DirectionRepository directionRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PharmacyRedisTemplateService pharmacyRedisTemplateService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "pharmacy-data", groupId = "pharmacy-group")
    public void consume(String message) {
        try {
            log.info("Received message: {}", message);
            Direction direction = objectMapper.readValue(message, Direction.class);

            // Direction 저장
            directionRepository.save(direction);
            log.info("Direction saved to database: {}", direction.getTargetPharmacyName());

            // Pharmacy 저장
            Pharmacy pharmacy = convertToPharmacy(direction);
            pharmacyRepository.save(pharmacy);
            log.info("Pharmacy saved to database: {}", pharmacy.getPharmacyName());

            // Redis에 저장
            PharmacyDto pharmacyDto = convertToPharmacyDto(direction);
            pharmacyRedisTemplateService.save(pharmacyDto);
            log.info("Pharmacy saved to Redis: {}", pharmacyDto.getPharmacyName());

        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }

    private Pharmacy convertToPharmacy(Direction direction) {
        return Pharmacy.builder()
                .pharmacyName(direction.getTargetPharmacyName())
                .pharmacyAddress(direction.getTargetAddress())
                .latitude(direction.getTargetLatitude())
                .longitude(direction.getTargetLongitude())
                .build();
    }

    private PharmacyDto convertToPharmacyDto(Direction direction) {
        return PharmacyDto.builder()
                .id(direction.getId())
                .pharmacyName(direction.getTargetPharmacyName())
                .pharmacyAddress(direction.getTargetAddress())
                .latitude(direction.getTargetLatitude())
                .longitude(direction.getTargetLongitude())
                .build();
    }
}