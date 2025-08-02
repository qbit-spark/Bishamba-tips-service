package org.qbitspark.bishambatipsservice.authentication_service.service;

import org.qbitspark.bishambatipsservice.authentication_service.entity.AccountEntity;
import org.qbitspark.bishambatipsservice.authentication_service.payloads.*;
import org.qbitspark.bishambatipsservice.globeadvice.exceptions.*;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    String registerAccount(CreateAccountRequest createAccountRequest) throws Exception;

    String loginAccount(AccountLoginRequest accountLoginRequest) throws Exception;

    RefreshTokenResponse refreshToken(String refreshToken) throws TokenInvalidException;

    List<AccountEntity> getAllAccounts();

    AccountEntity getAccountById(UUID accountId) throws ItemNotFoundException, RandomExceptions;

    AccountEntity approveUser(UUID accountId) throws ItemNotFoundException;
    AccountEntity assignRole(UUID accountId, UUID roleId) throws ItemNotFoundException;
    AccountEntity updateUserDetails(UUID accountId, UpdateUserRequest request) throws ItemNotFoundException, RandomExceptions;

    List<RoleResponse> getAllRoles();

}
