package org.qbitspark.bishambatipsservice.FarmerMngService.service;

import org.qbitspark.bishambatipsservice.FarmerMngService.payloads.FarmerRegistrationRequest;
import org.qbitspark.bishambatipsservice.FarmerMngService.payloads.FarmerRegistrationResponse;
import org.qbitspark.bishambatipsservice.FarmerMngService.payloads.FarmerResponse;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.ItemNotFoundException;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.RandomExceptions;

import java.util.List;
import java.util.UUID;

public interface FarmerService {

    FarmerRegistrationResponse registerFarmer(FarmerRegistrationRequest request) throws RandomExceptions, ItemNotFoundException;

    List<FarmerResponse> getMyFarmers() throws ItemNotFoundException;

    FarmerResponse getFarmerById(UUID farmerId) throws ItemNotFoundException, RandomExceptions;

    List<FarmerResponse> getAllFarmers() throws ItemNotFoundException, RandomExceptions;

}