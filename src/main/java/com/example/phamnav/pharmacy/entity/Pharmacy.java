package com.example.phamnav.pharmacy.entity;

import com.example.phamnav.BaseTimeEntity;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.example.phamnav.pharmacy.dto.PharmacyDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "pharmacy")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pharmacy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pharmacyName;
    private String pharmacyAddress;
    private double latitude;
    private double longitude;

    // PharmacyDto를 받는 새로운 생성자
    public Pharmacy(PharmacyDto pharmacyDto) {
        this.pharmacyName = pharmacyDto.getPharmacyName();
        this.pharmacyAddress = pharmacyDto.getPharmacyAddress();
        this.latitude = pharmacyDto.getLatitude();
        this.longitude = pharmacyDto.getLongitude();
    }

    public void changePharmacyAddress(String address){
        this.pharmacyAddress = address;
    }
}
