package com.insurance_hospital.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.insurance_hospital.dto.HospitalRequest;
import com.insurance_hospital.dto.HospitalResponse;
import com.insurance_hospital.model.Hospital;
import com.insurance_hospital.repository.HospitalRepository;

@ExtendWith(MockitoExtension.class)
class HospitalServiceTest {

    @Mock
    private HospitalRepository hospitalRepository;

    @InjectMocks
    private HospitalService hospitalService;

    private HospitalRequest request;
    private Hospital hospital;

    @BeforeEach
    void setup() {
        request = new HospitalRequest();
        request.setName("City Care Hospital");
        request.setAddress("123 Main St");
        request.setContactNumber("9876543210");
        request.setEmail("contact@citycare.com");

        hospital = Hospital.builder()
                .id(1)
                .name("City Care Hospital")
                .address("123 Main St")
                .contactNumber("9876543210")
                .email("contact@citycare.com")
                .isNetworkHospital(true)
                .build();
    }

    @Test
    void registerHospital_success() {
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);

        HospitalResponse response = hospitalService.registerHospital(request);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("City Care Hospital", response.getName());
        assertEquals("123 Main St", response.getAddress());
        assertEquals("9876543210", response.getContactNumber());
        assertEquals("contact@citycare.com", response.getEmail());
        assertTrue(response.getIsNetworkHospital());

        verify(hospitalRepository, times(1)).save(any(Hospital.class));
    }

    @Test
    void getAllActiveHospitals_success() {
        when(hospitalRepository.findByIsNetworkHospitalTrue()).thenReturn(Arrays.asList(hospital));

        List<HospitalResponse> responses = hospitalService.getAllActiveHospitals();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("City Care Hospital", responses.get(0).getName());
        verify(hospitalRepository, times(1)).findByIsNetworkHospitalTrue();
    }

    @Test
    void getHospitalById_success() {
        when(hospitalRepository.findById(1)).thenReturn(Optional.of(hospital));

        HospitalResponse response = hospitalService.getHospitalById(1);

        assertNotNull(response);
        assertEquals(1, response.getId());
        verify(hospitalRepository, times(1)).findById(1);
    }

    @Test
    void getHospitalById_notFound() {
        when(hospitalRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> hospitalService.getHospitalById(99));
        assertTrue(ex.getMessage().contains("Hospital not found"));
        verify(hospitalRepository, times(1)).findById(99);
    }

    @Test
    void searchHospitalsByName_success() {
        when(hospitalRepository.findByNameContainingIgnoreCase("city")).thenReturn(Arrays.asList(hospital));

        List<HospitalResponse> responses = hospitalService.searchHospitalsByName("city");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("City Care Hospital", responses.get(0).getName());
        verify(hospitalRepository, times(1)).findByNameContainingIgnoreCase("city");
    }

    @Test
    void searchHospitalsByName_empty() {
        when(hospitalRepository.findByNameContainingIgnoreCase("unknown")).thenReturn(List.of());

        List<HospitalResponse> responses = hospitalService.searchHospitalsByName("unknown");

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(hospitalRepository, times(1)).findByNameContainingIgnoreCase("unknown");
    }
}
