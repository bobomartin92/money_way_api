package com.example.money_way.controller;

import com.example.money_way.dto.request.LoginRequestDto;
import com.example.money_way.dto.request.SignUpDto;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/verify-link/")
    ResponseEntity<ApiResponse>verifyLink(@RequestParam String token, @RequestParam String email){
        ApiResponse response = userService.verifyLink(email, token);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse> signUp(@Valid @RequestBody SignUpDto signUpDto) {
        return userService.signUp(signUpDto);
    }
}



