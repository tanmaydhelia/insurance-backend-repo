package com.insurance_hospital.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.insurance_hospital.dto.HospitalRequest;
import com.insurance_hospital.dto.HospitalResponse;
import com.insurance_hospital.service.HospitalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
public class HospitalController {
	private final HospitalService hospitalService;

    @PostMapping
    public ResponseEntity<HospitalResponse> registerHospital(@RequestBody HospitalRequest requestDTO) {
        return new ResponseEntity<>(hospitalService.registerHospital(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<HospitalResponse>> getAllHospitals() {
        return ResponseEntity.ok(hospitalService.getAllActiveHospitals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HospitalResponse> getHospitalById(@PathVariable Integer id) {
        return ResponseEntity.ok(hospitalService.getHospitalById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<HospitalResponse>> searchHospitals(@RequestParam String name) {
        return ResponseEntity.ok(hospitalService.searchHospitalsByName(name));
    }
}
