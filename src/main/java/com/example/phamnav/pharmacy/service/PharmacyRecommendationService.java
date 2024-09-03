package com.example.phamnav.pharmacy.service;

import com.example.phamnav.api.dto.DocumentDto;
import com.example.phamnav.api.dto.KakaoApiResponseDto;
import com.example.phamnav.api.service.KakaoAddressSearchService;
import com.example.phamnav.direction.entity.Direction;
import com.example.phamnav.direction.service.DirectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyRecommendationService {

    private final KakaoAddressSearchService kakaoAddressSearchService;
    private final DirectionService directionService;

    public void recommendPharmacy(String address) {

        KakaoApiResponseDto kakaoApiResponseDto = kakaoAddressSearchService.requestAddressSearch(address);

            if (Objects.isNull(kakaoApiResponseDto) || CollectionUtils.isEmpty(kakaoApiResponseDto.getDocumentList())) {
                log.error("[PharmacyRecommendationService] Input address: {}", address);
            return;
        }

        DocumentDto documentDto = kakaoApiResponseDto.getDocumentList().get(0);

//        List<Direction> directionList = directionService.buildDriectionList(documentDto);
        List<Direction> directionsList1 = directionService.buildDirectionListByCategoryApi(documentDto);
        directionService.saveAll(directionsList1);

    }
}
