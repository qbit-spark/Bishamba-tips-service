package org.qbitspark.bishambatipsservice.authentication_service.controller;

import org.qbitspark.bishambatipsservice.authentication_service.entity.AccountEntity;
import org.qbitspark.bishambatipsservice.authentication_service.entity.Roles;
import org.qbitspark.bishambatipsservice.authentication_service.payloads.*;
import org.qbitspark.bishambatipsservice.authentication_service.service.AccountService;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.ItemNotFoundException;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.RandomExceptions;
import org.qbitspark.bishambatipsservice.globeresponsebody.GlobeSuccessResponseBuilder;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/accounts")
public class AccountManagementController {

    private final AccountService accountService;


    @PostMapping("/approve/{accountId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> approveUser(
            @PathVariable UUID accountId) throws ItemNotFoundException {

        AccountEntity account = accountService.approveUser(accountId);
        AccountResponse response = mapToAccountResponse(account);

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "User approved successfully",
                response
        ));
    }


    @PostMapping("/assign-role")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> assignRole(
            @Valid @RequestBody AssignRoleRequest request) throws ItemNotFoundException {

        AccountEntity account = accountService.assignRole(request.getAccountId(), request.getRoleId());
        AccountResponse response = mapToAccountResponse(account);

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Role assigned successfully",
                response
        ));
    }


    @PutMapping("/{accountId}")
    public ResponseEntity<GlobeSuccessResponseBuilder> updateUserDetails(
            @PathVariable UUID accountId,
            @Valid @RequestBody UpdateUserRequest request) throws ItemNotFoundException, RandomExceptions {

        AccountEntity account = accountService.updateUserDetails(accountId, request);
        AccountResponse response = mapToAccountResponse(account);

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "User details updated successfully",
                response
        ));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> getAllAccounts() {

        List<AccountEntity> accounts = accountService.getAllAccounts();

        List<AccountResponse> accountResponses = accounts.stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Accounts retrieved successfully",
                accountResponses
        ));
    }



    @GetMapping("/pending-approval")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> getPendingApprovalAccounts() {

        List<AccountEntity> accounts = accountService.getAllAccounts();

        List<AccountResponse> pendingAccounts = accounts.stream()
                .filter(account -> !account.getIsApproved())
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Pending approval accounts retrieved successfully",
                pendingAccounts
        ));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<GlobeSuccessResponseBuilder> getAccountById(
            @PathVariable UUID accountId) throws ItemNotFoundException, RandomExceptions {

        AccountEntity account = accountService.getAccountById(accountId);
        AccountResponse response = mapToAccountResponse(account);

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Account retrieved successfully",
                response
        ));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> getAllRoles() {

        List<RoleResponse> roles = accountService.getAllRoles();

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Roles retrieved successfully",
                roles
        ));
    }

    private AccountResponse mapToAccountResponse(AccountEntity account) {
        return AccountResponse.builder()
                .id(account.getId())
                .userName(account.getUserName())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .middleName(account.getMiddleName())
                .email(account.getEmail())
                .phoneNumber(account.getPhoneNumber())
                .isApproved(account.getIsApproved())
                .isVerified(account.getIsVerified())
                .isEmailVerified(account.getIsEmailVerified())
                .isPhoneVerified(account.getIsPhoneVerified())
                .createdAt(account.getCreatedAt())
                .editedAt(account.getEditedAt())
                .roles(account.getRoles().stream()
                        .map(Roles::getRoleName)
                        .collect(Collectors.toSet()))
                .build();
    }
}