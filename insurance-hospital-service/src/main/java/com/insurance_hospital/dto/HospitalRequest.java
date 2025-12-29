package com.insurance_hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalRequest {
	private String name;
    private String address;
    private String contactNumber;
    private String email;
}
