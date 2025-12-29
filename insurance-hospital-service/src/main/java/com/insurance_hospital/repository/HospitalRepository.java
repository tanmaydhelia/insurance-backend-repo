package com.insurance_hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance_hospital.model.Hospital;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Integer> {
    List<Hospital> findByNameContainingIgnoreCase(String name);
    List<Hospital> findByIsNetworkHospitalTrue();
}