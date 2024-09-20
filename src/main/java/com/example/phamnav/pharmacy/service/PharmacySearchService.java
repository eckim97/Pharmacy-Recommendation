package com.example.phamnav.pharmacy.service;

import com.example.phamnav.pharmacy.cache.PharmacyRedisTemplateService;
import com.example.phamnav.pharmacy.dto.PharmacyDto;
import com.example.phamnav.kafka.service.PharmacyProducer;
import com.example.phamnav.pharmacy.entity.Pharmacy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacySearchService {

    private final PharmacyRepositoryService pharmacyRepositoryService;
    private final PharmacyRedisTemplateService pharmacyRedisTemplateService;

    public List<PharmacyDto> searchPharmacyDtoList() {
        // Redis에서 데이터 조회
        List<PharmacyDto> pharmacyDtoList = pharmacyRedisTemplateService.findAll();
        if(!pharmacyDtoList.isEmpty()){
            log.info("Redis findAll success!");
            return pharmacyDtoList;
        }

        // DB에서 데이터 조회
        pharmacyDtoList = pharmacyRepositoryService.findAll()
                .stream()
                .map(this::convertToPharmacyDto)
                .collect(Collectors.toList());

        // Redis에 데이터 저장
        pharmacyDtoList.forEach(pharmacyRedisTemplateService::save);

        return pharmacyDtoList;
    }

    private PharmacyDto convertToPharmacyDto(Pharmacy pharmacy) {
        return PharmacyDto.builder()
                .id(pharmacy.getId())
                .pharmacyAddress(pharmacy.getPharmacyAddress())
                .pharmacyName(pharmacy.getPharmacyName())
                .latitude(pharmacy.getLatitude())
                .longitude(pharmacy.getLongitude())
                .build();
    }
}