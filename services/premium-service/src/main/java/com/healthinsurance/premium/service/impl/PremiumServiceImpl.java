package com.healthinsurance.premium.service.impl;

import com.healthinsurance.premium.dto.request.InstallmentPaymentRequest;
import com.healthinsurance.premium.dto.request.PremiumRecalculateRequest;
import com.healthinsurance.premium.dto.request.PremiumScheduleCreateRequest;
import com.healthinsurance.premium.dto.response.PremiumInstallmentResponse;
import com.healthinsurance.premium.dto.response.PremiumOutstandingResponse;
import com.healthinsurance.premium.dto.response.PremiumScheduleResponse;
import com.healthinsurance.premium.entity.PremiumInstallment;
import com.healthinsurance.premium.entity.PremiumSchedule;
import com.healthinsurance.premium.enums.InstallmentStatus;
import com.healthinsurance.premium.enums.PaymentFrequency;
import com.healthinsurance.premium.enums.PremiumStatus;
import com.healthinsurance.premium.exception.DuplicatePremiumScheduleException;
import com.healthinsurance.premium.exception.InvalidPremiumScheduleException;
import com.healthinsurance.premium.exception.PremiumScheduleNotFoundException;
import com.healthinsurance.premium.mapper.PremiumMapper;
import com.healthinsurance.premium.repository.PremiumInstallmentRepository;
import com.healthinsurance.premium.repository.PremiumScheduleRepository;
import com.healthinsurance.premium.service.PremiumCalculationService;
import com.healthinsurance.premium.service.PremiumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PremiumServiceImpl implements PremiumService {

    private final PremiumScheduleRepository scheduleRepository;
    private final PremiumInstallmentRepository installmentRepository;
    private final PremiumCalculationService calculationService;
    private final PremiumMapper mapper;

    @Override
    @Transactional
    public PremiumScheduleResponse createPremiumSchedule(PremiumScheduleCreateRequest request) {
        log.info("Creating premium schedule for policy ID: {}", request.getPolicyId());

        if (scheduleRepository.existsByPolicyId(request.getPolicyId())) {
            log.warn("Premium schedule already exists for policy ID: {}", request.getPolicyId());
            throw new DuplicatePremiumScheduleException("A premium schedule already exists for policy ID: " + request.getPolicyId());
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidPremiumScheduleException("End date cannot be before start date.");
        }

        BigDecimal totalPremium = calculationService.calculateTotalPremium(
                request.getBasePremium(),
                request.getRiderPremium(),
                request.getRiskLoading(),
                request.getDiscount(),
                request.getTax()
        );

        PaymentFrequency frequency = request.getPaymentFrequency() != null ? request.getPaymentFrequency() : PaymentFrequency.ANNUAL;
        int installmentCount = calculationService.getNumberOfInstallments(frequency);

        PremiumSchedule schedule = new PremiumSchedule();
        schedule.setPolicyId(request.getPolicyId());
        schedule.setTotalPremium(totalPremium);
        schedule.setPaymentFrequency(frequency);
        schedule.setNumberOfInstallments(installmentCount);
        schedule.setPaidAmount(BigDecimal.ZERO);
        schedule.setOutstandingAmount(totalPremium);
        schedule.setPremiumStatus(PremiumStatus.PENDING);
        schedule.setStartDate(request.getStartDate());
        schedule.setEndDate(request.getEndDate());
        schedule.setCreatedAt(Instant.now());
        schedule.setUpdatedAt(Instant.now());

        List<PremiumInstallment> installments = calculationService.generateInstallments(
                schedule, totalPremium, frequency, request.getStartDate()
        );

        installments.forEach(schedule::addInstallment);

        PremiumSchedule saved = scheduleRepository.save(schedule);
        log.info("Successfully created premium schedule with ID: {} and {} installments", saved.getScheduleId(), installmentCount);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PremiumScheduleResponse getPremiumByPolicy(UUID policyId) {
        log.info("Fetching premium schedule for policy ID: {}", policyId);
        PremiumSchedule schedule = scheduleRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new PremiumScheduleNotFoundException("Premium schedule not found for policy ID: " + policyId));
        return mapper.toResponse(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public PremiumScheduleResponse getScheduleById(UUID scheduleId) {
        log.info("Fetching premium schedule by schedule ID: {}", scheduleId);
        PremiumSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new PremiumScheduleNotFoundException("Premium schedule not found for schedule ID: " + scheduleId));
        return mapper.toResponse(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public PremiumOutstandingResponse getOutstanding(UUID policyId) {
        log.info("Fetching outstanding amount for policy ID: {}", policyId);
        PremiumSchedule schedule = scheduleRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new PremiumScheduleNotFoundException("Premium schedule not found for policy ID: " + policyId));
        return mapper.toOutstandingResponse(schedule);
    }

    @Override
    @Transactional
    public PremiumScheduleResponse recalculatePremium(UUID policyId, PremiumRecalculateRequest request) {
        log.info("Recalculating premium for policy ID: {}", policyId);

        PremiumSchedule schedule = scheduleRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new PremiumScheduleNotFoundException("Premium schedule not found for policy ID: " + policyId));

        if (schedule.getPremiumStatus() == PremiumStatus.PAID) {
            throw new InvalidPremiumScheduleException("Cannot recalculate a fully paid premium schedule.");
        }

        BigDecimal newTotal = calculationService.calculateTotalPremium(
                request.getBasePremium() != null ? request.getBasePremium() : schedule.getTotalPremium(),
                request.getRiderPremium(),
                request.getRiskLoading(),
                request.getDiscount(),
                request.getTax()
        );

        BigDecimal paidAmount = schedule.getPaidAmount() != null ? schedule.getPaidAmount() : BigDecimal.ZERO;

        if (newTotal.compareTo(paidAmount) < 0) {
            throw new InvalidPremiumScheduleException("New total premium (" + newTotal + ") cannot be less than already paid amount (" + paidAmount + ")");
        }

        BigDecimal remainingOutstanding = newTotal.subtract(paidAmount);

        List<PremiumInstallment> pendingInstallments = schedule.getInstallments().stream()
                .filter(i -> i.getStatus() != InstallmentStatus.PAID)
                .collect(Collectors.toList());

        if (!pendingInstallments.isEmpty()) {
            BigDecimal countPending = BigDecimal.valueOf(pendingInstallments.size());
            BigDecimal splitAmount = remainingOutstanding.divide(countPending, 2, RoundingMode.HALF_UP);
            BigDecimal runningAdjusted = BigDecimal.ZERO;

            for (int idx = 0; idx < pendingInstallments.size(); idx++) {
                PremiumInstallment inst = pendingInstallments.get(idx);
                BigDecimal instAmount;
                if (idx == pendingInstallments.size() - 1) {
                    instAmount = remainingOutstanding.subtract(runningAdjusted);
                } else {
                    instAmount = splitAmount;
                    runningAdjusted = runningAdjusted.add(instAmount);
                }
                inst.setAmount(instAmount);
                inst.setOutstandingAmount(instAmount.subtract(inst.getPaidAmount() != null ? inst.getPaidAmount() : BigDecimal.ZERO));
                inst.setUpdatedAt(Instant.now());
            }
        }

        schedule.setTotalPremium(newTotal);
        schedule.setOutstandingAmount(remainingOutstanding);
        schedule.setUpdatedAt(Instant.now());

        PremiumSchedule updated = scheduleRepository.save(schedule);
        log.info("Successfully recalculated premium for policy ID: {}. New total: {}, remaining outstanding: {}",
                policyId, newTotal, remainingOutstanding);

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public PremiumInstallmentResponse recordInstallmentPayment(UUID installmentId, InstallmentPaymentRequest request) {
        log.info("Recording payment of {} for installment ID: {}", request.getAmount(), installmentId);

        PremiumInstallment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new InvalidPremiumScheduleException("Installment not found with ID: " + installmentId));

        if (installment.getStatus() == InstallmentStatus.PAID) {
            log.info("Installment {} is already fully paid. Skipping.", installmentId);
            return mapper.toInstallmentResponse(installment);
        }

        BigDecimal paymentAmount = request.getAmount();
        BigDecimal currentOutstanding = installment.getOutstandingAmount();

        if (paymentAmount.compareTo(currentOutstanding) > 0) {
            throw new InvalidPremiumScheduleException("Payment amount (" + paymentAmount + ") exceeds installment outstanding amount (" + currentOutstanding + ")");
        }

        BigDecimal newPaidAmount = installment.getPaidAmount().add(paymentAmount);
        BigDecimal newOutstanding = installment.getAmount().subtract(newPaidAmount);

        installment.setPaidAmount(newPaidAmount);
        installment.setOutstandingAmount(newOutstanding);

        if (newOutstanding.compareTo(BigDecimal.ZERO) == 0) {
            installment.setStatus(InstallmentStatus.PAID);
        } else {
            installment.setStatus(InstallmentStatus.PARTIALLY_PAID);
        }
        installment.setUpdatedAt(Instant.now());

        PremiumInstallment savedInstallment = installmentRepository.save(installment);

        // Update parent schedule balances
        PremiumSchedule schedule = installment.getPremiumSchedule();
        schedule.setPaidAmount(schedule.getPaidAmount().add(paymentAmount));
        schedule.setOutstandingAmount(schedule.getTotalPremium().subtract(schedule.getPaidAmount()));

        boolean isPolicyFullyPaid = schedule.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0;
        if (isPolicyFullyPaid) {
            schedule.setPremiumStatus(PremiumStatus.PAID);
        } else {
            schedule.setPremiumStatus(PremiumStatus.PARTIALLY_PAID);
        }
        schedule.setUpdatedAt(Instant.now());
        scheduleRepository.save(schedule);

        log.info("Updated Schedule ID: {}. Schedule paid: {}, schedule outstanding: {}, status: {}",
                schedule.getScheduleId(), schedule.getPaidAmount(), schedule.getOutstandingAmount(), schedule.getPremiumStatus());

        return mapper.toInstallmentResponse(savedInstallment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PremiumScheduleResponse> getAllSchedules() {
        log.info("Fetching all premium schedules");
        return scheduleRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}