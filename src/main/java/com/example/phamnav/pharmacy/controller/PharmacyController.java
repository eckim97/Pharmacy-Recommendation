package com.example.phamnav.pharmacy.controller;

import com.example.phamnav.kafka.service.PharmacyProducer;
import com.example.phamnav.pharmacy.cache.PharmacyRedisTemplateService;
import com.example.phamnav.pharmacy.dto.PharmacyDto;
import com.example.phamnav.pharmacy.service.PharmacyRepositoryService;
import com.example.phamnav.pharmacy.service.PharmacySearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PharmacyController {

    private final PharmacyProducer pharmacyProducer;
    private final PharmacyRedisTemplateService pharmacyRedisTemplateService;
    private final PharmacySearchService pharmacySearchService;

    @PostMapping("/pharmacy/kafka/send")
    public String sendPharmacyDataToKafka() {
        int count = pharmacyProducer.sendPharmacyDataFromCsv();
        return "Pharmacy data sent to Kafka. Total messages sent: " + count;
    }

    @GetMapping("/pharmacy/search")
    public List<PharmacyDto> searchPharmacy() {
        return pharmacySearchService.searchPharmacyDtoList();
    }
}