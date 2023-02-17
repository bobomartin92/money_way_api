package com.example.money_way.controller;
        import com.example.money_way.dto.request.LoginRequestDto;
        import com.example.money_way.dto.request.VerifyTokenDto;
        import com.example.money_way.dto.response.ApiResponse;
        import com.example.money_way.service.UserService;
        import lombok.RequiredArgsConstructor;
        import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.*;

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
}
