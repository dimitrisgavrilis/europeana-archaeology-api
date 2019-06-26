package gr.dcu.share3d.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import gr.dcu.share3d.mapping.api.resource.auth.LoginResponse;
import gr.dcu.share3d.mapping.api.resource.auth.LogoutResponse;
import gr.dcu.share3d.mapping.api.resource.auth.SignupRequest;
import gr.dcu.share3d.mapping.api.resource.UserResource;
import gr.dcu.share3d.mapping.api.resource.auth.ValidateTokenResponse;
import gr.dcu.share3d.mapping.model.User;
import gr.dcu.share3d.mapping.model.UserSession;
import gr.dcu.share3d.exception.AuthorizationException;
import gr.dcu.share3d.exception.BadRequestException;
import gr.dcu.share3d.exception.ForbiddenException;
import gr.dcu.share3d.mapping.repository.UserRepository;
import gr.dcu.share3d.mapping.repository.UserSessionRepository;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
@Service
public class AuthService {
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    UserSessionRepository userSessionRepository;
    
    public static final String HEADER_AUTHORIZATION = "Authorization";
    
    /**
     * Signup a new user
     * @param request
     * @return
     * @throws java.security.NoSuchAlgorithmException 
     */
    public User signup(SignupRequest request) throws NoSuchAlgorithmException, BadRequestException {
        
        // Validate credentials
        String email = request.getEmail();
        String password = request.getPassword(); // Password must be in md5 format
        
        if(email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new BadRequestException("Email or password is missing");
        }
        
        // Check if the email already exists
        List<User> users = userRepository.findByEmail(request.getEmail());
        if(users.size() > 0) {
            throw new BadRequestException("Email already exists.");
        }
        
        // Create user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(email);
        user.setPassword(password);
        
        // String passwordhash = MySQLUtils.toMd5(password);
        // user.setPassword(passwordhash);
        // log.debug("Plain: {} | PasswordHash: {}", request.getPassword(), passwordhash);
        
        user.setOrganization(request.getOrganization());
        user.setActive((short) 1);
        
        return userRepository.save(user);
    }
    
    /**
     * 
     * @param requestContext
     * @return 
     */
    public LoginResponse login(HttpServletRequest requestContext) {
        
        LoginResponse response = new LoginResponse();
       
        String authHeader = requestContext.getHeader(HEADER_AUTHORIZATION);
        String ipAddress = requestContext.getRemoteAddr();
        String requestUrl = requestContext.getRequestURL().toString();
        
        log.debug("Authorize call: {}", requestUrl);
        
        if(authHeader == null || authHeader.isEmpty()) {
            throw new AuthorizationException("Authorization Header is missing or empty.");
        } 
            
        // Parse credentials
        StringTokenizer tokenizer = new StringTokenizer(authHeader, ":");
        String username = tokenizer.nextToken();
        String password = tokenizer.nextToken();

        List<User> users = userRepository.findAllByEmailAndPassword(username, password);
        
        if(users.size() != 1) {
           throw new AuthorizationException("Cannot authorize.");
        } else {
            User user = users.get(0);
            
            // TODO: Save last login
            Instant now = Instant.now();
            // lastLogin = now;
            
            // Create jwt token
            Algorithm algorithm = Algorithm.HMAC512(Instant.now().toString());
            String token = JWT.create()
                    .withIssuer("Datawise.ai")
                    .sign(algorithm);
            
            // Save user session
            UserSession userSession = new UserSession();
            userSession.setUserId(user.getId());
            userSession.setSessionId(token);
            userSession.setIpAddress(ipAddress);
            userSession.setStatus("LOGIN");
            userSession.setLoginAt(LocalDateTime.now());
            userSession.setLogoutAt(LocalDateTime.now());
            userSessionRepository.save(userSession);
            
            // Prepare response
            response.setToken(token);
            
            UserResource userResource = new UserResource();
            userResource.setId(user.getId());
            userResource.setName(user.getName());
            userResource.setEmail(user.getEmail());
            
            response.setUser(userResource);
            
            return response;
        }
        
    }
    
    
    /**
     * 
     * @param requestContext
     * @return 
     */
    public LogoutResponse logout(HttpServletRequest requestContext) {
        
        LogoutResponse response = new LogoutResponse();
        
        String authHeader = requestContext.getHeader(HEADER_AUTHORIZATION);
        String ipAddress = requestContext.getRemoteAddr();
        
        if(authHeader == null || authHeader.isEmpty()) {
            response.setSuccess(false);
            
            return response;
        } 
        
        UserSession userSession = userSessionRepository.findBySessionIdAndIpAddress(authHeader, ipAddress);
        if(userSession != null) {
            userSession.setStatus("LOGOUT");
            userSession.setLogoutAt(LocalDateTime.now());
            userSessionRepository.save(userSession);
            
            response.setSuccess(true);
            return response;
            
        } else {
            response.setSuccess(false);
            return response;
        }
    }
    
    /**
     * 
     * @param requestContext
     * @return 
     */
    public ValidateTokenResponse status(HttpServletRequest requestContext) {
        
        ValidateTokenResponse authStatus = new ValidateTokenResponse();
        
        String authHeader = requestContext.getHeader(HEADER_AUTHORIZATION);
        String ipAddress = requestContext.getRemoteAddr();
        
        if(authHeader == null || authHeader.isEmpty()) {
            
            throw new ForbiddenException("Invalid authorization token.");
            
            // authStatus.setValid(false);
            // return authStatus; 
        } 
        
        UserSession userSession = userSessionRepository.findBySessionIdAndIpAddress(authHeader, ipAddress);
        if(userSession != null) {
            
            if(userSession.getStatus().equals("LOGOUT")) { // Is already logout
                // authStatus.setValid(false);
                throw new ForbiddenException("User has been logged out.");
            } else {
                authStatus.setValid(true);            
            }
            
            return authStatus;
        } else {
            throw new ForbiddenException("Invalid authorization token.");
            
            // authStatus.setValid(false);
            // return authStatus;
        }
    }
    
    /**
     * 
     * @param requestContext
     * @return 
     */
    public int authorize(HttpServletRequest requestContext) {
        
        int userId = -1;
        
            
        String authorizationHeader = requestContext.getHeader(HEADER_AUTHORIZATION);
        String ipAddress = requestContext.getRemoteAddr();
        String requestUrl = requestContext.getRequestURL().toString();

        log.debug("Authorize call: {}", requestUrl);

        if(authorizationHeader == null || authorizationHeader.isEmpty()) {
            throw new AuthorizationException("Authorization Header is missing or empty.");
        } 

        String accessToken = authorizationHeader.replace("Bearer ", "");
        
        UserSession userSession = userSessionRepository.findBySessionIdAndIpAddress(accessToken, ipAddress);
        if(userSession != null) {

            if(userSession.getStatus().equals("LOGOUT")) { // Is already logout
                throw new AuthorizationException("User has been logged out.");
            }

            userId = userSession.getUserId();
        } else {
            throw new AuthorizationException("Invalid user session.");
        }
        
        // Safe guard authorization
        if(userId == -1) {
            throw new AuthorizationException("Invalid user session.");
        }
            
        return userId;  
    }
}
