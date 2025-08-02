package org.qbitspark.bishambatipsservice.FarmerMngService.service.impl;

import org.qbitspark.bishambatipsservice.FarmerMngService.entities.FarmerEntity;
import org.qbitspark.bishambatipsservice.FarmerMngService.payloads.FarmerRegistrationRequest;
import org.qbitspark.bishambatipsservice.FarmerMngService.payloads.FarmerRegistrationResponse;
import org.qbitspark.bishambatipsservice.FarmerMngService.payloads.FarmerResponse;
import org.qbitspark.bishambatipsservice.FarmerMngService.repo.FarmerRepository;
import org.qbitspark.bishambatipsservice.FarmerMngService.service.FarmerService;
import org.qbitspark.bishambatipsservice.authentication_service.entity.AccountEntity;
import org.qbitspark.bishambatipsservice.authentication_service.repo.AccountRepo;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.ItemNotFoundException;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.RandomExceptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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

    @Override
    public FarmerRegistrationResponse registerFarmer(FarmerRegistrationRequest request) throws RandomExceptions, ItemNotFoundException {


        if (farmerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RandomExceptions("Farmer with this phone number already exists");
        }

        // Get the current agent
        AccountEntity agent = getAuthenticatedAccount();

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
        farmer.setCreatedAt(LocalDateTime.now());
        farmer.setEditedAt(LocalDateTime.now());

        FarmerEntity savedFarmer = farmerRepository.save(farmer);

        return new FarmerRegistrationResponse(
                savedFarmer.getId().toString(),
                savedFarmer.getFirstName() + " " + savedFarmer.getLastName(),
                savedFarmer.getPhoneNumber(),
                savedFarmer.getRegion()
        );
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

        // Check if the user is SUPER_ADMIN or owns the farmer
        boolean isSuperAdmin = isCurrentUserAdmin();
        boolean isOwner = farmer.getAgent().getId().equals(currentUser.getId());

        if (!isSuperAdmin && !isOwner) {
            throw new RandomExceptions("Access denied: You can only view your own farmers");
        }

        return mapToFarmerResponse(farmer, isSuperAdmin);
    }

    @Override
    public List<FarmerResponse> getAllFarmers() throws ItemNotFoundException, RandomExceptions {

        boolean isAdmin = isCurrentUserAdmin();

        if (!isAdmin) {
            throw new RandomExceptions("Access denied: Only admins can view all farmers");
        }

        List<FarmerEntity> farmers = farmerRepository.findAll();

        return farmers.stream()
                .map(farmer -> mapToFarmerResponse(farmer, true)) // Admin sees all
                .collect(Collectors.toList());
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