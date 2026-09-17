package com.aurainfo.foodapp.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAddressRequest {

    @Size(max = 255, message = "Address line cannot exceed 255 characters")
    private String addressLine;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @Pattern(
            regexp = "^[1-9][0-9]{5}$",
            message = "Pincode must be a valid 6-digit number"
    )
    private String pincode;

    private Boolean defaultAddress;
}