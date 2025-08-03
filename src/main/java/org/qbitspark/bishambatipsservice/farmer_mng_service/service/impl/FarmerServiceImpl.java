package org.qbitspark.bishambatipsservice.farmer_mng_service.service.impl;

import org.qbitspark.bishambatipsservice.farmer_mng_service.entities.FarmerEntity;
import org.qbitspark.bishambatipsservice.farmer_mng_service.payloads.FarmerRegistrationRequest;
import org.qbitspark.bishambatipsservice.farmer_mng_service.payloads.FarmerRegistrationResponse;
import org.qbitspark.bishambatipsservice.farmer_mng_service.payloads.ConfirmTermsRequest;
import org.qbitspark.bishambatipsservice.farmer_mng_service.payloads.FarmerResponse;
import org.qbitspark.bishambatipsservice.farmer_mng_service.repo.FarmerRepository;
import org.qbitspark.bishambatipsservice.farmer_mng_service.service.FarmerService;
import org.qbitspark.bishambatipsservice.authentication_service.entity.AccountEntity;
import org.qbitspark.bishambatipsservice.authentication_service.repo.AccountRepo;
import org.qbitspark.bishambatipsservice.sms_service.GlobeSmsService;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.ItemNotFoundException;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.RandomExceptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FarmerServiceImpl implements FarmerService {

    private final FarmerRepository farmerRepository;
    private final AccountRepo accountRepo;
    private final GlobeSmsService globeSmsService;

    @Override
    public FarmerRegistrationResponse createFarmer(FarmerRegistrationRequest request) throws RandomExceptions, ItemNotFoundException {

        // Check if a phone already exists
        if (farmerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RandomExceptions("Farmer with this phone number already exists");
        }

        // Get the current agent
        AccountEntity agent = getAuthenticatedAccount();

        String termsCode = generateTermsCode();
        LocalDateTime codeExpiresAt = LocalDateTime.now().plusMinutes(30);

        // Create farmer
        FarmerEntity farmer = new FarmerEntity();
        farmer.setPhoneNumber(request.getPhoneNumber());
        farmer.setFirstName(request.getFirstName());
        farmer.setLastName(request.getLastName());
        farmer.setMiddleName(request.getMiddleName());
        farmer.setRegion(request.getRegion());
        farmer.setCrops(request.getCrops());
        farmer.setLivestock(request.getLivestock());
        farmer.setFarmSize(request.getFarmSize());
        farmer.setAgent(agent);
        farmer.setTermsAgreementCode(termsCode);
        farmer.setTermsCodeExpiresAt(codeExpiresAt);
        farmer.setTermsAgreementAccepted(false);
        farmer.setCreatedAt(LocalDateTime.now());
        farmer.setEditedAt(LocalDateTime.now());

        FarmerEntity savedFarmer = farmerRepository.save(farmer);

        String smsMessage = String.format(
                "Hujambo %s! Karibu Bishamba. Namba yako ya makubaliano: %s. Namba hii itaisha baada ya dakika 30. Toa namba hii kwa wakala wako kukamilisha usajili.",
                savedFarmer.getFirstName(), termsCode
        );


        globeSmsService.sendSimpleSms(savedFarmer.getPhoneNumber(), smsMessage);


        return new FarmerRegistrationResponse(
                savedFarmer.getId().toString(),
                savedFarmer.getFirstName() + " " + savedFarmer.getLastName(),
                savedFarmer.getPhoneNumber(),
                savedFarmer.getRegion(),
                "Farmer created successfully. Terms agreement code sent via SMS."
        );
    }

    @Override
    public FarmerResponse confirmTermsAgreement(UUID farmerId, ConfirmTermsRequest request) throws RandomExceptions, ItemNotFoundException {

        FarmerEntity farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new ItemNotFoundException("Farmer not found"));

        AccountEntity currentUser = getAuthenticatedAccount();

        // Check permissions
        boolean isSuperAdmin = isCurrentUserAdmin();
        boolean isOwner = farmer.getAgent().getId().equals(currentUser.getId());

        if (!isSuperAdmin && !isOwner) {
            throw new RandomExceptions("Access denied: You can only confirm terms for your own farmers");
        }

        // Check if already confirmed
        if (farmer.isTermsAgreementAccepted()) {
            throw new RandomExceptions("Terms agreement already confirmed for this farmer");
        }

        // Check if code has expired
        if (LocalDateTime.now().isAfter(farmer.getTermsCodeExpiresAt())) {
            throw new RandomExceptions("Terms agreement code has expired. Please request a new code.");
        }

        // Validate the code
        if (!request.getTermsAgreementCode().equals(farmer.getTermsAgreementCode())) {
            throw new RandomExceptions("Invalid terms agreement code");
        }

        // Confirm terms agreement
        farmer.setTermsAgreementAccepted(true);
        farmer.setTermsAgreementAcceptedAt(LocalDateTime.now());
        farmer.setEditedAt(LocalDateTime.now());

        FarmerEntity savedFarmer = farmerRepository.save(farmer);

        return mapToFarmerResponse(savedFarmer, isSuperAdmin);
    }

    @Override
    public FarmerResponse resendTermsCode(UUID farmerId) throws RandomExceptions, ItemNotFoundException {

        FarmerEntity farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new ItemNotFoundException("Farmer not found"));

        AccountEntity currentUser = getAuthenticatedAccount();

        // Check permissions
        boolean isSuperAdmin = isCurrentUserAdmin();
        boolean isOwner = farmer.getAgent().getId().equals(currentUser.getId());

        if (!isSuperAdmin && !isOwner) {
            throw new RandomExceptions("Access denied: You can only resend code for your own farmers");
        }

        // Check if already confirmed
        if (farmer.isTermsAgreementAccepted()) {
            throw new RandomExceptions("Terms agreement already confirmed for this farmer");
        }

        // Generate new code with fresh 30-minute expiry
        String newTermsCode = generateTermsCode();
        LocalDateTime newExpiresAt = LocalDateTime.now().plusMinutes(30);

        farmer.setTermsAgreementCode(newTermsCode);
        farmer.setTermsCodeExpiresAt(newExpiresAt);
        farmer.setEditedAt(LocalDateTime.now());

        FarmerEntity savedFarmer = farmerRepository.save(farmer);

        // Send new SMS with updated code
        String smsMessage = String.format(
                "Hujambo %s! Namba mpya ya makubaliano: %s. Namba hii itaisha baada ya dakika 30. Toa namba hii kwa wakala wako.",
                savedFarmer.getFirstName(), newTermsCode
        );

        try {
            globeSmsService.sendOTPSms(savedFarmer.getPhoneNumber(), smsMessage);
            log.info("New terms agreement code sent to farmer: {}", savedFarmer.getPhoneNumber());
        } catch (Exception e) {
            log.error("Failed to send SMS to farmer: {}", savedFarmer.getPhoneNumber(), e);
        }

        return mapToFarmerResponse(savedFarmer, isSuperAdmin);
    }

    @Override
    public List<FarmerResponse> getMyFarmers() throws ItemNotFoundException {
        AccountEntity agent = getAuthenticatedAccount();
        List<FarmerEntity> farmers = farmerRepository.findByAgent(agent);

        boolean isAdmin = isCurrentUserAdmin();

        return farmers.stream()
                .map(farmer -> mapToFarmerResponse(farmer, isAdmin))
                .collect(Collectors.toList());
    }

    @Override
    public FarmerResponse getFarmerById(UUID farmerId) throws ItemNotFoundException, RandomExceptions {
        FarmerEntity farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new ItemNotFoundException("Farmer not found"));

        AccountEntity currentUser = getAuthenticatedAccount();

        // Check permissions
        boolean isSuperAdmin = isCurrentUserAdmin();
        boolean isOwner = farmer.getAgent().getId().equals(currentUser.getId());

        if (!isSuperAdmin && !isOwner) {
            throw new RandomExceptions("Access denied: You can only view your own farmers");
        }

        return mapToFarmerResponse(farmer, isSuperAdmin);
    }

    @Override
    public List<FarmerResponse> getAllFarmers() throws ItemNotFoundException, RandomExceptions {
        AccountEntity currentUser = getAuthenticatedAccount();
        boolean isAdmin = isCurrentUserAdmin();

        if (!isAdmin) {
            throw new RandomExceptions("Access denied: Only admins can view all farmers");
        }

        List<FarmerEntity> farmers = farmerRepository.findAll();

        return farmers.stream()
                .map(farmer -> mapToFarmerResponse(farmer, true))
                .collect(Collectors.toList());
    }

    private String generateTermsCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(7);

        for (int i = 0; i < 6; i++) {
            if (i == 3) {
                code.append("-");
            }
            code.append(random.nextInt(10));
        }

        return code.toString();
    }

    private FarmerResponse mapToFarmerResponse(FarmerEntity farmer, boolean isAdmin) {
        return FarmerResponse.builder()
                .id(farmer.getId())
                .phoneNumber(isAdmin ? farmer.getPhoneNumber() : FarmerResponse.maskPhoneNumber(farmer.getPhoneNumber()))
                .firstName(farmer.getFirstName())
                .lastName(farmer.getLastName())
                .middleName(farmer.getMiddleName())
                .region(farmer.getRegion())
                .crops(farmer.getCrops())
                .livestock(farmer.getLivestock())
                .farmSize(farmer.getFarmSize())
                .termsAgreementCode(farmer.getTermsAgreementCode())
                .termsAgreementAccepted(farmer.isTermsAgreementAccepted())
                .termsAgreementAcceptedAt(farmer.getTermsAgreementAcceptedAt())
                .isActive(farmer.isActive())
                .hasRegistrationPaid(farmer.isHasRegistrationPaid())
                .registrationPaidAt(farmer.getRegistrationPaidAt())
                .createdAt(farmer.getCreatedAt())
                .build();
    }

    private boolean isCurrentUserAdmin() throws ItemNotFoundException {
        AccountEntity currentUser = getAuthenticatedAccount();
        return currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role.getRoleName()));
    }

    private AccountEntity getAuthenticatedAccount() throws ItemNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userName = userDetails.getUsername();

        return accountRepo.findByUserName(userName)
                .orElseThrow(() -> new ItemNotFoundException("User not found"));
    }
}