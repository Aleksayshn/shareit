package com.ct5121.shareit.auth;

import com.ct5121.shareit.auth.dto.AuthRequest;
import com.ct5121.shareit.auth.dto.AuthResponse;
import com.ct5121.shareit.auth.dto.RegisterRequest;
import com.ct5121.shareit.exception.InvalidCredentialsException;
import com.ct5121.shareit.security.JwtService;
import com.ct5121.shareit.security.ShareItUserDetails;
import com.ct5121.shareit.user.dto.UserRequestDto;
import com.ct5121.shareit.user.dto.UserResponseDto;
import com.ct5121.shareit.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setName(request.getName());
        userRequestDto.setEmail(request.getEmail());
        userRequestDto.setPassword(request.getPassword());

        UserResponseDto user = userService.addUser(userRequestDto);
        return buildAuthResponse(user.getId(), user.getName(), user.getEmail());
    }

    public AuthResponse login(AuthRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            ShareItUserDetails principal = (ShareItUserDetails) authentication.getPrincipal();
            return buildAuthResponse(principal.getId(), principal.getName(), principal.getEmail());
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid email or password", ex);
        }
    }

    public UserResponseDto getCurrentUser(ShareItUserDetails currentUser) {
        return new UserResponseDto(currentUser.getId(), currentUser.getName(), currentUser.getEmail());
    }

    private AuthResponse buildAuthResponse(Long id, String name, String email) {
        String token = jwtService.generateToken(id, email);
        return new AuthResponse(id, name, email, token, jwtService.extractExpiration(token));
    }
}
