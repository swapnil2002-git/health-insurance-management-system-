package com.healthinsurance.customer.dto.response;
import lombok.Data;
import java.util.UUID;

@Data
public class AddressResponse {
    private UUID customerAddressId;
    private String addressType;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}