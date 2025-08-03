package org.qbitspark.bishambatipsservice.farmer_mng_service.controller;

import org.qbitspark.bishambatipsservice.farmer_mng_service.payloads.ConfirmTermsRequest;
import org.qbitspark.bishambatipsservice.farmer_mng_service.payloads.FarmerRegistrationRequest;
import org.qbitspark.bishambatipsservice.farmer_mng_service.payloads.FarmerRegistrationResponse;
import org.qbitspark.bishambatipsservice.farmer_mng_service.payloads.FarmerResponse;
import org.qbitspark.bishambatipsservice.farmer_mng_service.service.FarmerService;
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

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/farmers")
public class FarmerManagementController {

    private final FarmerService farmerService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ROLE_AGENT') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> registerFarmer(
            @Valid @RequestBody FarmerRegistrationRequest request)
            throws RandomExceptions, ItemNotFoundException {

        FarmerRegistrationResponse response = farmerService.createFarmer(request);

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Farmer registered successfully",
                response
        ));
    }

    @GetMapping("/my-farmers")
    @PreAuthorize("hasRole('ROLE_AGENT') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> getMyFarmers()
            throws ItemNotFoundException {

        List<FarmerResponse> farmers = farmerService.getMyFarmers();

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Farmers retrieved successfully",
                farmers
        ));
    }

    @GetMapping("/{farmerId}")
    public ResponseEntity<GlobeSuccessResponseBuilder> getFarmerById(
            @PathVariable UUID farmerId)
            throws ItemNotFoundException, RandomExceptions {

        FarmerResponse farmer = farmerService.getFarmerById(farmerId);

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Farmer retrieved successfully",
                farmer
        ));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> getAllFarmers()
            throws ItemNotFoundException, RandomExceptions {

        List<FarmerResponse> farmers = farmerService.getAllFarmers();

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "All farmers retrieved successfully",
                farmers
        ));
    }

    @PostMapping("/{farmerId}/resend-terms-code")
    @PreAuthorize("hasRole('ROLE_AGENT') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> resendTermsCode(
            @PathVariable UUID farmerId)
            throws RandomExceptions, ItemNotFoundException {
        FarmerResponse farmer = farmerService.resendTermsCode(farmerId);
        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Terms code resent successfully",
                farmer
        ));
    }


    @PostMapping("/{farmerId}/confirm-terms")
    @PreAuthorize("hasRole('ROLE_AGENT') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> confirmTermsAgreement(
            @PathVariable UUID farmerId,
            @Valid @RequestBody ConfirmTermsRequest request)
            throws RandomExceptions, ItemNotFoundException {
        FarmerResponse farmer = farmerService.confirmTermsAgreement(farmerId, request);
        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Terms confirmed successfully",
                farmer
        ));
    }


}