package com.example.phamnav.pharmacy.service;

import com.example.phamnav.pharmacy.cache.PharmacyRedisTemplateService;
import com.example.phamnav.pharmacy.dto.PharmacyDto;
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
        long startRequestTime = System.currentTimeMillis();

        try {
            // Redis에서 데이터 조회 시간 측정
            long startRedisTime = System.currentTimeMillis();
            List<PharmacyDto> pharmacyDtoList = pharmacyRedisTemplateService.findAll();
            long endRedisTime = System.currentTimeMillis();
            log.info("레디스 조회 시간: {} ms, 조회된 항목 수: {}", endRedisTime - startRedisTime, pharmacyDtoList.size());

            if (!pharmacyDtoList.isEmpty()) {
                log.info("Redis findAll success!");
                return pharmacyDtoList;
            }

            // DB에서 데이터 조회 시간 측정
            long startDbTime = System.currentTimeMillis();
            List<Pharmacy> pharmacyList = pharmacyRepositoryService.findAll();
            long endDbTime = System.currentTimeMillis();
            log.info("DB 조회 시간: {} ms, 조회된 항목 수: {}", endDbTime - startDbTime, pharmacyList.size());

            pharmacyDtoList = pharmacyList.stream()
                    .map(this::convertToPharmacyDto)
                    .collect(Collectors.toList());

            // Redis에 데이터 저장 시간 측정
            long startRedisSaveTime = System.currentTimeMillis();
            pharmacyRedisTemplateService.saveAll(pharmacyDtoList);
            long endRedisSaveTime = System.currentTimeMillis();
            log.info("Redis 저장 시간: {} ms, 저장된 항목 수: {}", endRedisSaveTime - startRedisSaveTime, pharmacyDtoList.size());

            return pharmacyDtoList;
        } catch (Exception e) {
            log.error("데이터 조회 중 오류 발생", e);
            throw e;
        } finally {
            long endRequestTime = System.currentTimeMillis();
            log.info("전체 요청 처리 시간: {} ms", endRequestTime - startRequestTime);
        }
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