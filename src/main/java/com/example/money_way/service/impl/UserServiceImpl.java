package com.example.money_way.service.impl;

import com.example.money_way.configuration.mail.EmailService;
import com.example.money_way.configuration.security.CustomUserDetailService;
import com.example.money_way.configuration.security.JwtUtils;
import com.example.money_way.dto.request.CreateWalletRequest;
import com.example.money_way.dto.request.LoginRequestDto;
import com.example.money_way.dto.request.PasswordResetDTO;
import com.example.money_way.dto.request.SignUpDto;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.enums.Role;
import com.example.money_way.exception.InvalidCredentialsException;
import com.example.money_way.exception.UserNotFoundException;
import com.example.money_way.exception.ValidationException;
import com.example.money_way.model.User;
import com.example.money_way.repository.UserRepository;
import com.example.money_way.service.UserService;
import com.example.money_way.service.WalletService;
import com.example.money_way.utils.AppUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Data
@RequiredArgsConstructor

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppUtil appUtil;
    private final EmailService emailService;
    private final WalletService walletService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailService customUserDetailService;
    private final JwtUtils jwtUtils;


    @Override
    public ApiResponse<String> updatePassword(PasswordResetDTO passwordResetDTO) {

        String currentPassword = passwordResetDTO.getCurrentPassword();
        String newPassword = passwordResetDTO.getNewPassword();

        User user = appUtil.getLoggedInUser();

        String savedPassword = user.getPassword();

        if(!passwordEncoder.matches(currentPassword, savedPassword))
            throw new InvalidCredentialsException("Credentials must match");

        else
            passwordResetDTO.setNewPassword(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        emailService.sendEmail(user.getEmail(), "Update Password", "Your password has been updated  successfully. Ensure to keep it a secret. Never disclose your password to a third party.");
        return new ApiResponse<>( "Success", "Password reset successful", null);

    }
    
    @Override
    public ResponseEntity<String> login(LoginRequestDto request) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserDetails user = customUserDetailService.loadUserByUsername(request.getEmail());

        if (user != null) {
            return ResponseEntity.ok(jwtUtils.generateToken(user));
        }
        return ResponseEntity.status(400).body("Some Error Occurred");
    }

    @Override
    public ApiResponse verifyLink(String email, String token) {

        String tokenEmail = jwtUtils.extractUsername(token);

      if (jwtUtils.isTokenExpired(token))
            throw new ValidationException("Token expired. Resend token and try again");

        if (email.equalsIgnoreCase(tokenEmail)) {
            User existingUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User does not exist"));
                existingUser.setConfirmationToken(null);
                existingUser.setActive(true);
                CreateWalletRequest request = new CreateWalletRequest();
                request.setEmail(existingUser.getEmail());
                request.setBvn(existingUser.getBvn());
                request.setTx_ref(appUtil.generateSerialNumber("MNW_"));
                request.setIs_permanent("true");
                walletService.createWallet(request);
                return ApiResponse.builder().message("Success").status("Account created successfully").build();
            }
            throw new UserNotFoundException("Error: No Account found! or Invalid Token");
    }

    @Override
    public ResponseEntity<ApiResponse> signUp(SignUpDto signUpDto) throws ValidationException {

        Boolean isUserExist = userRepository.existsByEmail(signUpDto.getEmail());
        if (isUserExist)
            throw new ValidationException("User Already Exists!");

        User user = new User();
        user.setFirstName(signUpDto.getFirstName());
        user.setLastName(signUpDto.getLastName());
        user.setEmail(signUpDto.getEmail());
        user.setPhoneNumber(signUpDto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        user.setBvn(signUpDto.getBvn());
        user.setRole(Role.ROLE_USER);
        user.setPin(passwordEncoder.encode(signUpDto.getPin()));
        String token = jwtUtils.generateSignUpConfirmationToken(signUpDto.getEmail());
        userRepository.save(user);

        String URL = "http://localhost:8080/api/v1/auth/verify-link/?token=" + token +"&email="+user.getEmail();
        String link = "<h3>Hello "  + signUpDto.getFirstName()  +"<br> Click the link below to activate your account <a href=" + URL + "><br>Activate</a></h3>";

        emailService.sendEmail(signUpDto.getEmail(),"MoneyWay: Verify Your Account", link);

        return ResponseEntity.ok(new ApiResponse<>("Successful", "SignUp Successful. Check your mail to activate your account", null));
    }
}
