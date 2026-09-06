package com.healthinsurance.premium.dto.response;

import com.healthinsurance.premium.enums.InstallmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PremiumInstallmentResponse {
    private UUID installmentId;
    private Integer installmentNumber;
    private BigDecimal amount;
    private LocalDate dueDate;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private InstallmentStatus status;
}