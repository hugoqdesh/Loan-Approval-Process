package com.example.loanapprovalprocess.repositories;

import com.example.loanapprovalprocess.entities.LoanApplicationEntity;
import com.example.loanapprovalprocess.enums.LoanApplicationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LoanApplicationRepository extends JpaRepository<LoanApplicationEntity, Long> {

    boolean existsByPersonalCodeAndStatus(String personalCode, LoanApplicationStatus status);

    @EntityGraph(attributePaths = "paymentScheduleEntries")
    @Query("select loanApplication from LoanApplicationEntity loanApplication where loanApplication.id = :id")
    Optional<LoanApplicationEntity> findDetailedById(Long id);
}
