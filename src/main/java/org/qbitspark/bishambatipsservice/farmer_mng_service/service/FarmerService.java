package org.qbitspark.bishambatipsservice.farmer_mng_service.service;

import org.qbitspark.bishambatipsservice.farmer_mng_service.payloads.FarmerRegistrationRequest;
import org.qbitspark.bishambatipsservice.farmer_mng_service.payloads.FarmerRegistrationResponse;
import org.qbitspark.bishambatipsservice.farmer_mng_service.payloads.FarmerResponse;
import org.qbitspark.bishambatipsservice.farmer_mng_service.payloads.ConfirmTermsRequest;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.ItemNotFoundException;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.RandomExceptions;

import java.util.List;
import java.util.UUID;

public interface FarmerService {

    FarmerRegistrationResponse createFarmer(FarmerRegistrationRequest request) throws RandomExceptions, ItemNotFoundException;

    FarmerResponse confirmTermsAgreement(UUID farmerId, ConfirmTermsRequest request) throws RandomExceptions, ItemNotFoundException;

    FarmerResponse resendTermsCode(UUID farmerId) throws RandomExceptions, ItemNotFoundException;

    List<FarmerResponse> getMyFarmers() throws ItemNotFoundException;

    FarmerResponse getFarmerById(UUID farmerId) throws ItemNotFoundException, RandomExceptions;

    List<FarmerResponse> getAllFarmers() throws ItemNotFoundException, RandomExceptions;

}