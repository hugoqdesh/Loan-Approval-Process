package com.example.loanapprovalprocess.api;

import com.example.loanapprovalprocess.api.dto.CreateLoanApplicationRequest;
import com.example.loanapprovalprocess.api.dto.LoanApplicationResponse;
import com.example.loanapprovalprocess.api.dto.RejectLoanApplicationRequest;
import com.example.loanapprovalprocess.services.LoanApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loan-applications")
@Tag(name = "Loan Applications")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a new loan application")
    public LoanApplicationResponse submit(@Valid @RequestBody CreateLoanApplicationRequest request) {
        return loanApplicationService.submit(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get loan application details together with the generated payment schedule")
    public LoanApplicationResponse getById(@PathVariable Long id) {
        return loanApplicationService.getById(id);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a loan application that is currently in review")
    public LoanApplicationResponse approve(@PathVariable Long id) {
        return loanApplicationService.approve(id);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a loan application that is currently in review")
    public LoanApplicationResponse reject(@PathVariable Long id, @Valid @RequestBody RejectLoanApplicationRequest request) {
        return loanApplicationService.reject(id, request.rejectionReason());
    }
}
