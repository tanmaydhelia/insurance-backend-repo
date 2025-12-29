package com.insurance_hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HospitalResponse {
	private Integer id;
    private String name;
    private String address;
    private String contactNumber;
    private String email;
    private Boolean isNetworkHospital;
}
