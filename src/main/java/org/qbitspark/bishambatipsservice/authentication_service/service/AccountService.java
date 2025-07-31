package org.qbitspark.bishambatipsservice.authentication_service.service;

import org.qbitspark.bishambatipsservice.authentication_service.entity.AccountEntity;
import org.qbitspark.bishambatipsservice.authentication_service.payloads.AccountLoginRequest;
import org.qbitspark.bishambatipsservice.authentication_service.payloads.CreateAccountRequest;
import org.qbitspark.bishambatipsservice.authentication_service.payloads.RefreshTokenResponse;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.*;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    String registerAccount(CreateAccountRequest createAccountRequest) throws Exception;

    String loginAccount(AccountLoginRequest accountLoginRequest) throws Exception;

    RefreshTokenResponse refreshToken(String refreshToken) throws TokenInvalidException;

    List<AccountEntity> getAllAccounts();

    AccountEntity getAccountByID(UUID uuid) throws ItemNotFoundException;

}
