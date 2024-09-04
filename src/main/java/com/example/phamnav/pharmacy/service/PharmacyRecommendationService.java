package com.example.phamnav.pharmacy.service;

import com.example.phamnav.api.dto.DocumentDto;
import com.example.phamnav.api.dto.KakaoApiResponseDto;
import com.example.phamnav.api.service.KakaoAddressSearchService;
import com.example.phamnav.direction.entity.Direction;
import com.example.phamnav.direction.service.DirectionService;
import com.example.phamnav.dto.OutputDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyRecommendationService {

    private final KakaoAddressSearchService kakaoAddressSearchService;
    private final DirectionService directionService;

    public List<OutputDto> recommendPharmacyList(String address) {

        KakaoApiResponseDto kakaoApiResponseDto = kakaoAddressSearchService.requestAddressSearch(address);

            if (Objects.isNull(kakaoApiResponseDto) || CollectionUtils.isEmpty(kakaoApiResponseDto.getDocumentList())) {
                log.error("[PharmacyRecommendationService] Input address: {}", address);
            return Collections.emptyList();
        }

        DocumentDto documentDto = kakaoApiResponseDto.getDocumentList().get(0);
        // 공공기관 약국 데이터
        List<Direction> directionList = directionService.buildDriectionList(documentDto);

        // kakao api
//        List<Direction> directionsList1 = directionService.buildDirectionListByCategoryApi(documentDto);

        return directionService.saveAll(directionList)
                .stream()
                .map(this::convertToOutputDto)
                .collect(Collectors.toList());
    }
    private OutputDto convertToOutputDto(Direction direction) {
        return OutputDto.builder()
                .pharmacyName(direction.getTargetPharmacyName())
                .pharmacyAddress(direction.getTargetAddress())
                .directionUrl("todo") //TODO
                .roadViewUrl("todo")
                .distance(String.format("%.2f", direction.getDistance()))
                .build();
    }

}
