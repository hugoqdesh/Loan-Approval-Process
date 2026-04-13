package com.example.loanapprovalprocess.entities;

import com.example.loanapprovalprocess.enums.LoanApplicationStatus;
import com.example.loanapprovalprocess.enums.RejectionReason;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loan_applications")
public class LoanApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 32)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 32)
    private String lastName;

    @Column(name = "personal_code", nullable = false, length = 11)
    private String personalCode;

    @Column(name = "active_personal_code", length = 11, unique = true)
    private String activePersonalCode;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "customer_age", nullable = false)
    private Integer customerAge;

    @Column(name = "loan_term_months", nullable = false)
    private Integer loanTermMonths;

    @Column(name = "interest_margin", nullable = false, precision = 8, scale = 3)
    private BigDecimal interestMargin;

    @Column(name = "base_interest_rate", nullable = false, precision = 8, scale = 3)
    private BigDecimal baseInterestRate;

    @Column(name = "total_annual_interest_rate", nullable = false, precision = 8, scale = 3)
    private BigDecimal totalAnnualInterestRate;

    @Column(name = "loan_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "first_payment_date", nullable = false)
    private LocalDate firstPaymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private LoanApplicationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "rejection_reason", length = 64)
    private RejectionReason rejectionReason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("installmentNumber ASC")
    private List<PaymentScheduleEntryEntity> paymentScheduleEntries = new ArrayList<>();

    @PrePersist
    void onCreate() {
        syncActivePersonalCode();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        syncActivePersonalCode();
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    private void syncActivePersonalCode() {
        activePersonalCode = status == LoanApplicationStatus.IN_REVIEW ? personalCode : null;
    }

    public void replacePaymentSchedule(List<PaymentScheduleEntryEntity> entries) {
        paymentScheduleEntries.clear();
        for (PaymentScheduleEntryEntity entry : entries) {
            addPaymentScheduleEntry(entry);
        }
    }

    public void addPaymentScheduleEntry(PaymentScheduleEntryEntity entry) {
        paymentScheduleEntries.add(entry);
        entry.setLoanApplication(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPersonalCode() {
        return personalCode;
    }

    public void setPersonalCode(String personalCode) {
        this.personalCode = personalCode;
    }

    public String getActivePersonalCode() {
        return activePersonalCode;
    }

    public void setActivePersonalCode(String activePersonalCode) {
        this.activePersonalCode = activePersonalCode;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Integer getCustomerAge() {
        return customerAge;
    }

    public void setCustomerAge(Integer customerAge) {
        this.customerAge = customerAge;
    }

    public Integer getLoanTermMonths() {
        return loanTermMonths;
    }

    public void setLoanTermMonths(Integer loanTermMonths) {
        this.loanTermMonths = loanTermMonths;
    }

    public BigDecimal getInterestMargin() {
        return interestMargin;
    }

    public void setInterestMargin(BigDecimal interestMargin) {
        this.interestMargin = interestMargin;
    }

    public BigDecimal getBaseInterestRate() {
        return baseInterestRate;
    }

    public void setBaseInterestRate(BigDecimal baseInterestRate) {
        this.baseInterestRate = baseInterestRate;
    }

    public BigDecimal getTotalAnnualInterestRate() {
        return totalAnnualInterestRate;
    }

    public void setTotalAnnualInterestRate(BigDecimal totalAnnualInterestRate) {
        this.totalAnnualInterestRate = totalAnnualInterestRate;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public LocalDate getFirstPaymentDate() {
        return firstPaymentDate;
    }

    public void setFirstPaymentDate(LocalDate firstPaymentDate) {
        this.firstPaymentDate = firstPaymentDate;
    }

    public LoanApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(LoanApplicationStatus status) {
        this.status = status;
    }

    public RejectionReason getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(RejectionReason rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OffsetDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(OffsetDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public List<PaymentScheduleEntryEntity> getPaymentScheduleEntries() {
        return paymentScheduleEntries;
    }

    public void setPaymentScheduleEntries(List<PaymentScheduleEntryEntity> paymentScheduleEntries) {
        this.paymentScheduleEntries = paymentScheduleEntries;
    }
}
