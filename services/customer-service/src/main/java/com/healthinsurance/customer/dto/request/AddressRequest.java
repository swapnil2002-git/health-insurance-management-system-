package com.healthinsurance.customer.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank(message = "Street 1 is required")
    private String street1;
    private String street2;
    @NotBlank(message = "City is required")
    private String city;
    @NotBlank(message = "State is required")
    private String state;
    @NotBlank(message = "Zip code is required")
    private String zipCode;
    @NotBlank(message = "Country is required")
    private String country;
    @NotBlank(message = "Address type is required (e.g. HOME, BILLING)")
    private String addressType;
}