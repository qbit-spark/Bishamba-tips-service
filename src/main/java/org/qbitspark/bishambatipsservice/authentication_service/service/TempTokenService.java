package org.qbitspark.bishambatipsservice.authentication_service.service;

import org.qbitspark.bishambatipsservice.authentication_service.entity.AccountEntity;
import org.qbitspark.bishambatipsservice.authentication_service.enums.TempTokenPurpose;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.ItemNotFoundException;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.RandomExceptions;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.VerificationException;

import java.time.LocalDateTime;

public interface TempTokenService {

    String createTempToken(AccountEntity account, TempTokenPurpose purpose, String identifier, String otpCode) throws RandomExceptions;


    String resendOTP(String tempToken) throws VerificationException, ItemNotFoundException, RandomExceptions;


    AccountEntity validateTempTokenAndOTP(String tempToken, String otpCode) throws VerificationException, ItemNotFoundException, RandomExceptions;


    String sendPSWDResetOTP(String email) throws VerificationException, ItemNotFoundException, RandomExceptions;

    void invalidateAllTokensForPurpose(AccountEntity account, TempTokenPurpose purpose);

    void cleanupExpiredTokens();

    boolean isWithinRateLimit(AccountEntity account, TempTokenPurpose purpose);

    LocalDateTime getNextResendAllowedTime(String userIdentifier, TempTokenPurpose purpose);

    int getRemainingResendAttempts(String userIdentifier, TempTokenPurpose purpose);

    boolean canResendOTP(String userIdentifier, TempTokenPurpose purpose);

    boolean canResendByEmail(String email, TempTokenPurpose purpose);
}