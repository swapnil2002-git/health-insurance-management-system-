package com.healthinsurance.product.config;

import com.healthinsurance.product.entity.*;
import com.healthinsurance.product.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductDataInitializer implements CommandLineRunner {

    private final CoverageRepository coverageRepo;
    private final DeductibleRepository deductibleRepo;
    private final CopaymentRepository copaymentRepo;
    private final ExclusionRepository exclusionRepo;
    private final RiderRepository riderRepo;
    private final InsuranceProductRepository productRepo;
    private final InsurancePlanRepository planRepo;
    private final PlanCoverageRepository planCoverageRepo;
    private final PlanDeductibleRepository planDeductibleRepo;
    private final PlanCopaymentRepository planCopaymentRepo;
    private final PlanExclusionRepository planExclusionRepo;
    private final PlanRiderRepository planRiderRepo;

    @Override
    public void run(String... args) {
        log.info("SEED: Initializing standard Product & Plan Master Rules...");

        // 1. Seed Coverages
        Coverage inPatient = seedCoverage("In-Patient Hospitalization", "Covers room rent, nursing fees, and doctor fees during hospital stay");
        Coverage prePost = seedCoverage("Pre & Post Hospitalization", "Covers 30 days prior and 60 days following hospitalization");
        Coverage dayCare = seedCoverage("Day Care Procedures", "Medical treatments taken in a hospital that require less than 24 hours stay");
        Coverage ambulance = seedCoverage("Emergency Ambulance Cover", "Road ambulance transit expenses to the nearest hospital");
        seedCoverage("AYUSH Treatment", "In-patient treatment under Ayurveda, Yoga, Unani, Siddha and Homeopathy");

        // 2. Seed Deductibles
        Deductible zeroDed = seedDeductible("Zero Deductible", "No out-of-pocket deductible required before insurance pays");
        Deductible ded5000 = seedDeductible("Standard Deductible INR 5000", "Customer pays initial 5,000 INR per policy year");
        seedDeductible("Voluntary Deductible INR 10000", "Higher voluntary deductible to reduce base premium");

        // 3. Seed Copayments
        Copayment zeroCopay = seedCopayment("Zero Copayment", "100% covered expenses paid by insurer without member copay");
        Copayment copay10 = seedCopayment("10% Senior Citizen Copay", "10% copay applicable only for members aged 60 and above");
        seedCopayment("20% Tier-2 Zone Copay", "20% copay if treatment is taken outside base hospital tier network");

        // 4. Seed Exclusions
        Exclusion cosmetic = seedExclusion("Cosmetic & Aesthetic Surgery", "Plastic or cosmetic surgery unless necessitated by accidental injury");
        Exclusion pedWaiting = seedExclusion("Pre-Existing Disease Waiting Period", "24-month waiting period for declared pre-existing diseases");
        seedExclusion("Adventure Sports Injuries", "Injuries arising out of high-risk adventure or hazardous sports activities");
        seedExclusion("Substance Abuse & Self-Harm", "Treatments directly or indirectly caused by alcohol/drug abuse or intentional self-injury");

        // 5. Seed Riders
        Rider critIllness = seedRider("Critical Illness Lump-Sum Rider", "Provides lump-sum payout upon diagnosis of 36 critical illnesses");
        Rider dailyCash = seedRider("Hospital Daily Cash Benefit", "Daily allowance of INR 2,000 for each day of hospital confinement");
        seedRider("Maternity & Newborn Care Rider", "Covers normal and cesarean deliveries plus baby care for 90 days");

        // 6. Seed Default Product & Plan
        if (productRepo.count() == 0) {
            InsuranceProduct product = new InsuranceProduct();
            product.setName("Comprehensive Health Shield");
            product.setDescription("Our flagship comprehensive medical health insurance covering individuals and families.");
            product.setStatus("ACTIVE");
            InsuranceProduct savedProduct = productRepo.save(product);
            log.info("SEED: Created default product [{}] with ID: {}", savedProduct.getName(), savedProduct.getProductId());

            InsurancePlan goldPlan = new InsurancePlan();
            goldPlan.setName("Gold Shield Plan");
            goldPlan.setDescription("Comprehensive Gold Tier coverage with INR 10 Lakh sum insured.");
            goldPlan.setProduct(savedProduct);
            InsurancePlan savedPlan = planRepo.save(goldPlan);
            log.info("SEED: Created default plan [{}] with ID: {}", savedPlan.getName(), savedPlan.getPlanId());

            // Link standard rules to Gold Plan
            linkCoverage(savedPlan, inPatient);
            linkCoverage(savedPlan, prePost);
            linkCoverage(savedPlan, dayCare);
            linkCoverage(savedPlan, ambulance);

            linkDeductible(savedPlan, zeroDed);
            linkCopayment(savedPlan, zeroCopay);
            linkExclusion(savedPlan, cosmetic);
            linkExclusion(savedPlan, pedWaiting);

            linkRider(savedPlan, critIllness);
            linkRider(savedPlan, dailyCash);
            log.info("SEED: Successfully linked coverages, deductibles, and riders to Gold Shield Plan.");
        }

        log.info("SEED: Product & Plan data initialization complete.");
    }

    private Coverage seedCoverage(String name, String desc) {
        return coverageRepo.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Coverage c = new Coverage();
                    c.setName(name);
                    c.setDescription(desc);
                    return coverageRepo.save(c);
                });
    }

    private Deductible seedDeductible(String name, String desc) {
        return deductibleRepo.findAll().stream()
                .filter(d -> d.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Deductible d = new Deductible();
                    d.setName(name);
                    d.setDescription(desc);
                    return deductibleRepo.save(d);
                });
    }

    private Copayment seedCopayment(String name, String desc) {
        return copaymentRepo.findAll().stream()
                .filter(cp -> cp.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Copayment cp = new Copayment();
                    cp.setName(name);
                    cp.setDescription(desc);
                    return copaymentRepo.save(cp);
                });
    }

    private Exclusion seedExclusion(String name, String desc) {
        return exclusionRepo.findAll().stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Exclusion e = new Exclusion();
                    e.setName(name);
                    e.setDescription(desc);
                    return exclusionRepo.save(e);
                });
    }

    private Rider seedRider(String name, String desc) {
        return riderRepo.findAll().stream()
                .filter(r -> r.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Rider r = new Rider();
                    r.setName(name);
                    r.setDescription(desc);
                    return riderRepo.save(r);
                });
    }

    private void linkCoverage(InsurancePlan plan, Coverage coverage) {
        PlanCoverage pc = new PlanCoverage();
        pc.setPlan(plan);
        pc.setCoverage(coverage);
        planCoverageRepo.save(pc);
    }

    private void linkDeductible(InsurancePlan plan, Deductible deductible) {
        PlanDeductible pd = new PlanDeductible();
        pd.setPlan(plan);
        pd.setDeductible(deductible);
        planDeductibleRepo.save(pd);
    }

    private void linkCopayment(InsurancePlan plan, Copayment copayment) {
        PlanCopayment pc = new PlanCopayment();
        pc.setPlan(plan);
        pc.setCopayment(copayment);
        planCopaymentRepo.save(pc);
    }

    private void linkExclusion(InsurancePlan plan, Exclusion exclusion) {
        PlanExclusion pe = new PlanExclusion();
        pe.setPlan(plan);
        pe.setExclusion(exclusion);
        planExclusionRepo.save(pe);
    }

    private void linkRider(InsurancePlan plan, Rider rider) {
        PlanRider pr = new PlanRider();
        pr.setPlan(plan);
        pr.setRider(rider);
        planRiderRepo.save(pr);
    }
}