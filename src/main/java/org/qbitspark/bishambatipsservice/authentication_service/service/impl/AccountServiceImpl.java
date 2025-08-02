package org.qbitspark.bishambatipsservice.authentication_service.service.impl;

import org.qbitspark.bishambatipsservice.authentication_service.entity.AccountEntity;
import org.qbitspark.bishambatipsservice.authentication_service.entity.Roles;
import org.qbitspark.bishambatipsservice.authentication_service.enums.TempTokenPurpose;
import org.qbitspark.bishambatipsservice.authentication_service.enums.VerificationChannels;
import org.qbitspark.bishambatipsservice.authentication_service.payloads.*;
import org.qbitspark.bishambatipsservice.authentication_service.repo.AccountRepo;
import org.qbitspark.bishambatipsservice.authentication_service.repo.RolesRepository;
import org.qbitspark.bishambatipsservice.authentication_service.service.AccountService;
import org.qbitspark.bishambatipsservice.authentication_service.service.TempTokenService;
import org.qbitspark.bishambatipsservice.authentication_service.utils.UsernameGenerationUtils;
import org.qbitspark.bishambatipsservice.emails_service.GlobeMailService;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.*;
import org.qbitspark.bishambatipsservice.globesecurity.JWTProvider;
import lombok.RequiredArgsConstructor;
import org.qbitspark.bishambatipsservice.sms_service.GlobeSmsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepo accountRepo;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTProvider tokenProvider;
    private final UsernameGenerationUtils usernameGenerationUtils;
    private final TempTokenService tempTokenService;
    private final GlobeMailService globeMailService;
    private final GlobeSmsService globeSmsService;

    @Override
    public String registerAccount(CreateAccountRequest createAccountRequest) throws Exception {

        String generatedUsername = usernameGenerationUtils.generateUniqueUsernameFromEmail(createAccountRequest.getEmail());

        // Check the existence of a user by email, phone, or generated username
        if (accountRepo.existsByPhoneNumberOrEmailOrUserName(
                createAccountRequest.getPhoneNumber(),
                createAccountRequest.getEmail(),
                generatedUsername)) {
            throw new ItemReadyExistException("User with provided credentials already exist, please login");
        }

        AccountEntity account = new AccountEntity();
        account.setUserName(generatedUsername);
        account.setCreatedAt(LocalDateTime.now());
        account.setEditedAt(LocalDateTime.now());
        account.setIsVerified(false);
        account.setIsEmailVerified(false);
        account.setIsPhoneVerified(false);
        account.setFirstName(createAccountRequest.getFirstName());
        account.setLastName(createAccountRequest.getLastName());
        account.setMiddleName(createAccountRequest.getMiddleName());
        account.setEmail(createAccountRequest.getEmail());
        account.setPhoneNumber(createAccountRequest.getPhoneNumber());
        account.setPassword(passwordEncoder.encode(createAccountRequest.getPassword()));

        Set<Roles> roles = new HashSet<>();
        Roles userRoles = rolesRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new ItemNotFoundException("Default role not found"));
        roles.add(userRoles);
        account.setRoles(roles);

        AccountEntity savedAccount = accountRepo.save(account);

        String otpCode = generateOtpCode();

        String tempToken = tempTokenService.createTempToken(
                savedAccount,
                TempTokenPurpose.REGISTRATION_OTP,
                createAccountRequest.getEmail(),
                otpCode
        );


        return sendOTPViaChannel(VerificationChannels.SMS, savedAccount, otpCode, tempToken);
    }


    @Override
    public String loginAccount(AccountLoginRequest accountLoginRequest) throws Exception {

        String identifier = accountLoginRequest.getIdentifier();
        String password = accountLoginRequest.getPassword();

        AccountEntity userAccount = accountRepo.findByEmailOrPhoneNumberOrUserName(identifier, identifier, identifier).orElseThrow(() -> new ItemNotFoundException("User not found"));

        String otpCode = generateOtpCode();

        String tempToken = tempTokenService.createTempToken(
                userAccount,
                TempTokenPurpose.LOGIN_OTP,
                userAccount.getEmail(),
                otpCode
        );

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userAccount.getUserName(),
                        password));

        if (!userAccount.getIsVerified()) {
            userAccount.setIsVerified(true);
        }

        return sendOTPViaChannel(VerificationChannels.SMS, userAccount, otpCode, tempToken);

    }

    @Override
    public RefreshTokenResponse refreshToken(String refreshToken) throws TokenInvalidException {
        try {
            // First, validate that this is specifically a refresh token
            if (!tokenProvider.validToken(refreshToken, "REFRESH")) {
                throw new TokenInvalidException("Invalid token");
            }

            // Get username from a token
            String userName = tokenProvider.getUserName(refreshToken);

            // Retrieve user from database
            AccountEntity user = accountRepo.findByUserName(userName)
                    .orElseThrow(() -> new ItemNotFoundException("User not found"));

            // Create authentication with user authorities
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getUserName(),
                    null,
                    mapRolesToAuthorities(user.getRoles())
            );

            // Generate only a new access token, not a new refresh token
            String newAccessToken = tokenProvider.generateAccessToken(authentication);

            // Build response
            RefreshTokenResponse refreshTokenResponse = new RefreshTokenResponse();
            refreshTokenResponse.setNewToken(newAccessToken);

            return refreshTokenResponse;

        } catch (TokenExpiredException e) {
            throw new TokenInvalidException("Refresh token has expired. Please login again");
        } catch (Exception e) {
            throw new TokenInvalidException("Failed to refresh token: " + e.getMessage());
        } finally {
            // Clear security context after token refresh
            SecurityContextHolder.clearContext();
        }
    }

    @Override
    public List<AccountEntity> getAllAccounts() {
        return accountRepo.findAll();
    }

    @Override
    public AccountEntity getAccountById(UUID accountId) throws ItemNotFoundException, RandomExceptions {
        AccountEntity account = accountRepo.findById(accountId)
                .orElseThrow(() -> new ItemNotFoundException("Account not found"));

        AccountEntity currentUser = getAuthenticatedAccount();

        // Check if user is SUPER_ADMIN or owns the account
        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role.getRoleName()));

        boolean isOwner = account.getUserName().equals(currentUser.getUserName());

        if (!isSuperAdmin && !isOwner) {
            throw new RandomExceptions("Access denied: You can only view your own account");
        }

        return account;
    }

    @Override
    public AccountEntity approveUser(UUID accountId) throws ItemNotFoundException {
        AccountEntity account = accountRepo.findById(accountId)
                .orElseThrow(() -> new ItemNotFoundException("Account not found"));

        AccountEntity approver = getAuthenticatedAccount();

        account.setIsVerified(true);
        account.setApprovedBy(approver.getUserName());
        account.setEditedAt(LocalDateTime.now());

        return accountRepo.save(account);
    }

    @Override
    public AccountEntity assignRole(UUID accountId, UUID roleId) throws ItemNotFoundException {
        AccountEntity account = accountRepo.findById(accountId)
                .orElseThrow(() -> new ItemNotFoundException("Account not found"));

        Roles role = rolesRepository.findById(roleId)
                .orElseThrow(() -> new ItemNotFoundException("Role not found"));

        account.getRoles().clear();
        account.getRoles().add(role);
        account.setEditedAt(LocalDateTime.now());

        return accountRepo.save(account);
    }

    @Override
    public AccountEntity updateUserDetails(UUID accountId, UpdateUserRequest request) throws ItemNotFoundException, RandomExceptions {
        AccountEntity account = accountRepo.findById(accountId)
                .orElseThrow(() -> new ItemNotFoundException("Account not found"));

        AccountEntity currentUser = getAuthenticatedAccount();

        // Check if user is SUPER_ADMIN or owns the account
        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role.getRoleName()));

        boolean isOwner = account.getUserName().equals(currentUser.getUserName());

        if (!isSuperAdmin && !isOwner) {
            throw new RandomExceptions("Access denied: You can only edit your own account");
        }

        // Only SUPER_ADMIN can update sensitive fields
        if (!isSuperAdmin) {
            if (request.getEmail() != null || request.getPhoneNumber() != null) {
                throw new RandomExceptions("Access denied: Only admins can update email or phone number");
            }
        }

        // Validate uniqueness for admin updates
        if (isSuperAdmin) {
            if (request.getEmail() != null && !request.getEmail().equals(account.getEmail())) {
                // Check if email is already taken by another user
                Optional<AccountEntity> existingEmailUser = accountRepo.findByEmail(request.getEmail());
                if (existingEmailUser.isPresent() && !existingEmailUser.get().getId().equals(accountId)) {
                    throw new RandomExceptions("Email is already taken by another user");
                }
            }

            if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(account.getPhoneNumber())) {
                // Check if phone number is already taken by another user
                Optional<AccountEntity> existingPhoneUser = accountRepo.findByEmailOrPhoneNumberOrUserName(
                        null, request.getPhoneNumber(), null);
                if (existingPhoneUser.isPresent() && !existingPhoneUser.get().getId().equals(accountId)) {
                    throw new RandomExceptions("Phone number is already taken by another user");
                }
            }
        }

        // Update fields based on permissions
        if (request.getFirstName() != null) {
            account.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            account.setLastName(request.getLastName());
        }
        if (request.getMiddleName() != null) {
            account.setMiddleName(request.getMiddleName());
        }

        // Admin-only fields (now with duplicate validation)
        if (isSuperAdmin) {
            if (request.getEmail() != null) {
                account.setEmail(request.getEmail());
            }
            if (request.getPhoneNumber() != null) {
                account.setPhoneNumber(request.getPhoneNumber());
            }
        }

        account.setEditedAt(LocalDateTime.now());

        return accountRepo.save(account);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        List<Roles> roles = rolesRepository.findAll();

        return roles.stream()
                .map(role -> new RoleResponse(role.getRoleId(), role.getRoleName()))
                .collect(Collectors.toList());
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Set<Roles> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toList());
    }

    private String generateOtpCode() {
        Random random = new Random();
        int otp = random.nextInt(900000) + 100000;
        return String.valueOf(otp);
    }

    private String sendOTPViaChannel(VerificationChannels verificationChannels, AccountEntity savedAccount, String otpCode, String tempToken) throws Exception {
        //Check a selected verification channel
        switch (verificationChannels) {
            case EMAIL -> //Send the OTP via email
                    globeMailService.sendOTPEmail(savedAccount.getEmail(), otpCode, savedAccount.getFirstName(), "Welcome to BuildWise Books Support!", "Please use the following OTP to complete your Authentication: ");
            case SMS -> {
                globeSmsService.sendOTPSms(savedAccount.getPhoneNumber(), otpCode);
            }
            case EMAIL_AND_SMS -> {
                System.out.println("Email and SMS verification is not implemented yet.");
            }

            case SMS_AND_WHATSAPP -> {
                System.out.println("SMS and WhatsApp verification is not implemented yet.");
            }
            case WHATSAPP -> {
                System.out.println("WhatsApp verification is not implemented yet.");
            }
            case VOICE_CALL -> {
                System.out.println("Voice call verification is not implemented yet.");
            }
            case PUSH_NOTIFICATION -> {
                System.out.println("Push notification verification is not implemented yet.");
            }
            case ALL_CHANNELS -> {
                System.out.println("All channels verification is not implemented yet.");
            }

            default ->
                    globeMailService.sendOTPEmail(savedAccount.getEmail(), otpCode, savedAccount.getFirstName(), "Welcome to BuildWise Books Support!", "Please use the following OTP to complete your registration: ");

        }

        return tempToken;
    }

    private AccountEntity getAuthenticatedAccount() throws ItemNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userName = userDetails.getUsername();

        return accountRepo.findByUserName(userName)
                .orElseThrow(() -> new ItemNotFoundException("User not found"));
    }

}
