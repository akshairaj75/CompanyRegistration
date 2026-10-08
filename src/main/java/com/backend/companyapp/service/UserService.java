package com.backend.companyapp.service;

import com.backend.companyapp.dto.auth.AuthResponseDto;
import com.backend.companyapp.dto.auth.LoginRequestDto;
import com.backend.companyapp.dto.auth.RegisterRequestDto;
import com.backend.companyapp.security.CustomUserPrincipal;

public interface UserService {

    public AuthResponseDto register(RegisterRequestDto dto);

    public AuthResponseDto login(LoginRequestDto dto);

    public AuthResponseDto getCurrentLoggedInUser(CustomUserPrincipal principal);

}
