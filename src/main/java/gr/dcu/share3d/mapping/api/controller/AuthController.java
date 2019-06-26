package gr.dcu.share3d.mapping.api.controller;

import gr.dcu.share3d.mapping.api.resource.auth.LoginResponse;
import gr.dcu.share3d.mapping.api.resource.auth.LogoutResponse;
import gr.dcu.share3d.mapping.api.resource.auth.SignupRequest;
import gr.dcu.share3d.mapping.api.resource.auth.ValidateTokenResponse;
import gr.dcu.share3d.mapping.model.User;
import gr.dcu.share3d.service.AuthService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.security.NoSuchAlgorithmException;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@CrossOrigin
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * 
     * @param request
     * @return
     * @throws java.security.NoSuchAlgorithmException
     */
    @PostMapping("/auth/signup")
    public User signup(@RequestBody SignupRequest request) throws NoSuchAlgorithmException { 
       
       log.info("Signup request...");
       
       return authService.signup(request);
    }
    
    /**
     * 
     * @param requestContext
     * @return 
     */
    @ApiOperation("Login")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = LoginResponse.class)})
    @PostMapping("/auth/login")
    LoginResponse login(HttpServletRequest requestContext) {
        return authService.login(requestContext);
    }
    
    /**
     * 
     * @param requestContext
     * @return 
     */
    @ApiOperation("Logout")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Boolean.class)})
    @PostMapping("/auth/logout")
    LogoutResponse logout(HttpServletRequest requestContext) {
        return authService.logout(requestContext);
    }
    
    @ApiOperation("Validate auth status")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Boolean.class)})
    @PostMapping("/auth/status")
    ValidateTokenResponse validateToken(HttpServletRequest requestContext) {
        return authService.status(requestContext);
    }
}
