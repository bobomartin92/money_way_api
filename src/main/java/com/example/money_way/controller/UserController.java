package com.example.money_way.controller;
        import com.example.money_way.dto.request.LoginRequestDto;
        import com.example.money_way.dto.request.VerifyTokenDto;
        import com.example.money_way.dto.request.SignUpDto;
        import com.example.money_way.dto.response.ApiResponse;
        import com.example.money_way.exception.ValidationException;
        import com.example.money_way.service.UserService;
        import lombok.RequiredArgsConstructor;
        import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.*;
        import org.springframework.web.bind.annotation.PostMapping;
        import org.springframework.web.bind.annotation.RequestBody;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RestController;
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
    @GetMapping("/verify-link")
    ResponseEntity<ApiResponse>verifyLink(@RequestParam VerifyTokenDto verifyTokenDto){
        ApiResponse response = userService.verifyLink(verifyTokenDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse> signUp(@Valid @RequestBody SignUpDto signUpDto) {
        return userService.signUp(signUpDto);
    }
}



