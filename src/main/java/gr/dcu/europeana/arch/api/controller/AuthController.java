package gr.dcu.europeana.arch.api.controller;

import gr.dcu.europeana.arch.api.dto.auth.*;
import gr.dcu.europeana.arch.model.UserEntity;
import gr.dcu.europeana.arch.service.AuthService;
import java.security.NoSuchAlgorithmException;
import javax.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@CrossOrigin
public class AuthController {
    
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Signup")
    @PostMapping("/auth/signup")
    public UserEntity signup(@RequestBody SignupRequest request) throws NoSuchAlgorithmException {
       return authService.signup(request);
    }
    
    @Operation(summary = "Login")
    @PostMapping("/auth/login")
    public LoginResponse login(HttpServletRequest requestContext) {
        return authService.login(requestContext);
    }
    
    @Operation(summary = "Logout")
    @PostMapping("/auth/logout")
    public LogoutResponse logout(HttpServletRequest requestContext) {
        return authService.logout(requestContext);
    }
    
    @Operation(summary = "Validate auth status")
    @PostMapping("/auth/status")
    public ValidateTokenResponse validateToken(HttpServletRequest requestContext) {
        return authService.status(requestContext);
    }

    @Operation(summary = "Reset password")
    @PostMapping("/auth/reset_password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest,
                                           HttpServletRequest requestContext) {

        authService.resetPassword(resetPasswordRequest, requestContext);

        return new ResponseEntity<>("Password reset successfully.", HttpStatus.OK);
    }
}
