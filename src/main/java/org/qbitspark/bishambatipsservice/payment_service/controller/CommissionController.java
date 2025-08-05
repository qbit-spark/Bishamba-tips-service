package org.qbitspark.bishambatipsservice.payment_service.controller;

import lombok.RequiredArgsConstructor;
import org.qbitspark.bishambatipsservice.authentication_service.entity.AccountEntity;
import org.qbitspark.bishambatipsservice.authentication_service.repo.AccountRepo;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.ItemNotFoundException;
import org.qbitspark.bishambatipsservice.globeresponsebody.GlobeSuccessResponseBuilder;
import org.qbitspark.bishambatipsservice.payment_service.entity.CommissionEntity;
import org.qbitspark.bishambatipsservice.payment_service.service.CommissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import org.qbitspark.bishambatipsservice.payment_service.paylaod.MarkCommissionPaidRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/commissions")
@RequiredArgsConstructor
public class CommissionController {

    private final CommissionService commissionService;
    private final AccountRepo accountRepo;

    @GetMapping("/my-commissions")
    @PreAuthorize("hasRole('ROLE_AGENT') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> getMyCommissions() throws ItemNotFoundException {

        AccountEntity agent = getAuthenticatedAccount();
        List<CommissionEntity> commissions = commissionService.getAgentCommissions(agent.getId());

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Commissions retrieved", commissions));
    }

    @GetMapping("/due")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> getDueCommissions() {

        List<CommissionEntity> dueCommissions = commissionService.getDueCommissions();

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Due commissions retrieved", dueCommissions));
    }

    @PostMapping("/{commissionId}/mark-paid")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> markCommissionAsPaid(
            @PathVariable UUID commissionId,
            @Valid @RequestBody MarkCommissionPaidRequest request) {

        commissionService.markCommissionAsPaid(commissionId, request.getPayoutTransactionId());

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Commission marked as paid", null));
    }

    @GetMapping("/agent/{agentId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> getAgentCommissions(
            @PathVariable UUID agentId) {

        List<CommissionEntity> commissions = commissionService.getAgentCommissions(agentId);

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Agent commissions retrieved", commissions));
    }

    private AccountEntity getAuthenticatedAccount() throws ItemNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userName = userDetails.getUsername();

        return accountRepo.findByUserName(userName)
                .orElseThrow(() -> new ItemNotFoundException("User not found"));
    }
}