package com.insurance_hospital.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.insurance_hospital.dto.HospitalRequest;
import com.insurance_hospital.dto.HospitalResponse;
import com.insurance_hospital.model.Hospital;
import com.insurance_hospital.repository.HospitalRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    public HospitalResponse registerHospital(HospitalRequest requestDTO) {
        Hospital hospital = Hospital.builder()
                .name(requestDTO.getName())
                .address(requestDTO.getAddress())
                .contactNumber(requestDTO.getContactNumber())
                .email(requestDTO.getEmail())
                .isNetworkHospital(true)
                .build();

        Hospital savedHospital = hospitalRepository.save(hospital);
        return mapToResponse(savedHospital);
    }

    public List<HospitalResponse> getAllActiveHospitals() {
        return hospitalRepository.findByIsNetworkHospitalTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public HospitalResponse getHospitalById(Integer id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital not found with id: " + id));
        return mapToResponse(hospital);
    }

    public List<HospitalResponse> searchHospitalsByName(String name) {
        return hospitalRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private HospitalResponse mapToResponse(Hospital hospital) {
        return HospitalResponse.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .contactNumber(hospital.getContactNumber())
                .email(hospital.getEmail())
                .isNetworkHospital(hospital.getIsNetworkHospital())
                .build();
    }
}