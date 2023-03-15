package com.example.money_way.controller;

import com.example.money_way.dto.request.LoginRequestDto;
import com.example.money_way.dto.request.PasswordResetDTO;
import com.example.money_way.dto.request.SignUpDto;
import com.example.money_way.dto.request.VerifyTokenDto;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.UserProfileResponse;
import com.example.money_way.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class UserController {
    private final UserService userService;
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto loginRequestdto) {
        return userService.login(loginRequestdto);
    }
    @PutMapping("/verify-link")
    ResponseEntity<ApiResponse>verifyLink(@RequestBody VerifyTokenDto verifyTokenDto){
        ApiResponse response = userService.verifyLink(verifyTokenDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse> signUp(@Valid @RequestBody SignUpDto signUpDto) {
        return userService.signUp(signUpDto);
    }

    @PutMapping("/reset-password")
    ResponseEntity<ApiResponse<String>> resetPassword (@Valid @RequestBody PasswordResetDTO passwordResetDto) {
        ApiResponse<String> apiResponse = userService.updatePassword(passwordResetDto);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(){
        ApiResponse<UserProfileResponse> apiResponse = userService.getUserProfile();
        return ResponseEntity.ok(apiResponse);
    }
}



