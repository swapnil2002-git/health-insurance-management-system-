package com.healthinsurance.customer.dto.response;
import lombok.Data;
import java.util.UUID;

@Data
public class ContactResponse {
    private UUID contactId;
    private String contactType;
    private String contactValue;
    private boolean isPrimary;
}