package gr.dcu.europeana.arch.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import gr.dcu.europeana.arch.api.controller.mapper.UserMapper;
import gr.dcu.europeana.arch.api.dto.auth.*;
import gr.dcu.europeana.arch.config.AppConfig;
import gr.dcu.europeana.arch.domain.entity.SettingEntity;
import gr.dcu.europeana.arch.domain.entity.UserEntity;
import gr.dcu.europeana.arch.domain.entity.UserSessionEntity;
import gr.dcu.europeana.arch.exception.AuthorizationException;
import gr.dcu.europeana.arch.exception.BadRequestException;
import gr.dcu.europeana.arch.exception.ForbiddenException;
import gr.dcu.europeana.arch.repository.UserRepository;
import gr.dcu.europeana.arch.repository.UserSessionRepository;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

import gr.dcu.europeana.arch.service.email.EmailBuilderService;
import gr.dcu.europeana.arch.service.email.MailgunService;
import gr.dcu.utils.MySQLUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private AppConfig appConfig;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private SettingService settingService;

    @Autowired
    private EmailBuilderService emailBuilderService;

    @Autowired
    private MailgunService mailgunService;

    @Autowired
    private UserMapper userMapper;
    
    public static final String HEADER_AUTHORIZATION = "Authorization";
    
    /**
     * Signup a new user
     */
    public UserEntity signup(SignupRequest request) throws NoSuchAlgorithmException, BadRequestException {
        
        // Validate credentials
        String email = request.getEmail();
        String password = request.getPassword(); // Password must be in md5 format
        
        if(email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new BadRequestException("Email or password is missing");
        }
        
        // Check if the email already exists
        List<UserEntity> userEntities = userRepository.findAllByEmail(request.getEmail());
        if(userEntities.size() > 0) {
            throw new BadRequestException("Email already exists.");
        }
        
        // Create user
        UserEntity userEntity = new UserEntity();
        userEntity.setName(request.getName());
        userEntity.setEmail(email);
        userEntity.setPassword(password);
        
        // String passwordhash = MySQLUtils.toMd5(password);
        // user.setPassword(passwordhash);
        // log.debug("Plain: {} | PasswordHash: {}", request.getPassword(), passwordhash);
        
        userEntity.setOrganization(request.getOrganization());
        userEntity.setAdmin(false);
        userEntity.setActive((short) 1);

        userEntity = userRepository.save(userEntity);

        // Notify user & admins by email
        try {

            // Add user email
            List<String> recipients = new LinkedList<>();
            recipients.add(email);

            List<String> adminRecipients = settingService.getRecipientList(SettingEntity.MAILGUN_RECIPIENTS_SIGN_UP);
            recipients.addAll(adminRecipients);
            log.info("Notify users. User: {} Admins: {}", email, adminRecipients);

            String from = appConfig.getMailgunSender();
            String subject = AppConfig.PROJECT_NAME + " - " + AppConfig.SERVICE_NAME + ": New account";

            Map<String, String> variables = new HashMap<>();
            variables.put(EmailBuilderService.VAR_USER_NAME, userEntity.getName());
            variables.put(EmailBuilderService.VAR_PROJECT_NAME, AppConfig.PROJECT_NAME);
            variables.put(EmailBuilderService.VAR_SERVICE_NAME, AppConfig.SERVICE_NAME);

            String body = emailBuilderService.buildSignupEmail("A new account created.", variables);
            //log.info(body);

            mailgunService.configure(appConfig.getMailgunDomainName(), appConfig.getMailgunApiKey());
            mailgunService.sendMessage(from, recipients, subject, body, false);

        } catch(Exception ex) {
            log.error("Create account. Send email failed. Email: {}", email);
        }



        return userEntity;
    }

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

        List<UserEntity> userEntities = userRepository.findAllByEmailAndPassword(username, password);
        
        if(userEntities.size() != 1) {
           throw new AuthorizationException("Cannot authorize.");
        } else {
            UserEntity userEntity = userEntities.get(0);
            
            // TODO: Save last login
            Instant now = Instant.now();
            // lastLogin = now;
            
            // Create jwt token
            Algorithm algorithm = Algorithm.HMAC512(Instant.now().toString());
            String token = JWT.create()
                    .withIssuer("Datawise.ai")
                    .sign(algorithm);
            
            // Save user session
            UserSessionEntity userSessionEntity = new UserSessionEntity();
            userSessionEntity.setUserId(userEntity.getId());
            userSessionEntity.setSessionId(token);
            userSessionEntity.setIpAddress(ipAddress);
            userSessionEntity.setStatus("LOGIN");
            userSessionEntity.setLoginAt(LocalDateTime.now());
            userSessionEntity.setLogoutAt(LocalDateTime.now());
            userSessionRepository.save(userSessionEntity);
            
            // Prepare response
            response.setToken(token);
            response.setUser(userMapper.toUserViewDto(userEntity));

            return response;
        }
        
    }
    

    public LogoutResponse logout(HttpServletRequest requestContext) {
        
        LogoutResponse response = new LogoutResponse();
        
        String authHeader = requestContext.getHeader(HEADER_AUTHORIZATION);
        String ipAddress = requestContext.getRemoteAddr();
        
        if(authHeader == null || authHeader.isEmpty()) {
            response.setSuccess(false);
            
            return response;
        } 
        
        UserSessionEntity userSessionEntity = userSessionRepository.findBySessionIdAndIpAddress(authHeader, ipAddress);
        if(userSessionEntity != null) {
            userSessionEntity.setStatus("LOGOUT");
            userSessionEntity.setLogoutAt(LocalDateTime.now());
            userSessionRepository.save(userSessionEntity);
            
            response.setSuccess(true);
            return response;
            
        } else {
            response.setSuccess(false);
            return response;
        }
    }
    
    public ValidateTokenResponse status(HttpServletRequest requestContext) {
        
        ValidateTokenResponse authStatus = new ValidateTokenResponse();
        
        String authHeader = requestContext.getHeader(HEADER_AUTHORIZATION);
        String ipAddress = requestContext.getRemoteAddr();
        
        if(authHeader == null || authHeader.isEmpty()) {
            
            throw new ForbiddenException("Invalid authorization token.");
            
            // authStatus.setValid(false);
            // return authStatus; 
        } 
        
        UserSessionEntity userSessionEntity = userSessionRepository.findBySessionIdAndIpAddress(authHeader, ipAddress);
        if(userSessionEntity != null) {
            
            if(userSessionEntity.getStatus().equals("LOGOUT")) { // Is already logout
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
        
        UserSessionEntity userSessionEntity = userSessionRepository.findBySessionIdAndIpAddress(accessToken, ipAddress);
        if(userSessionEntity != null) {

            if(userSessionEntity.getStatus().equals("LOGOUT")) { // Is already logout
                throw new AuthorizationException("User has been logged out.");
            }

            userId = userSessionEntity.getUserId();
        } else {
            throw new AuthorizationException("Invalid user session.");
        }
        
        // Safe guard authorization
        if(userId == -1) {
            throw new AuthorizationException("Invalid user session.");
        }
            
        return userId;  
    }

    public boolean resetPassword(ResetPasswordRequest resetPasswordRequest,
                                 HttpServletRequest requestContext) {

        String authHeader = requestContext.getHeader(HEADER_AUTHORIZATION);
        String ipAddress = requestContext.getRemoteAddr();
        String requestUrl = requestContext.getRequestURL().toString();

        String email = resetPasswordRequest.getUsername();

        log.info("Reset password. IP:{} - Username: {}", ipAddress, email);

        if(email == null || email.isEmpty()) {
            throw new BadRequestException("Username is missing or empty.");
        }

        List<UserEntity> userEntities = userRepository.findAllByEmail(email);

        if(userEntities.size() != 1) {
            throw new AuthorizationException("Cannot reset password. Cannot identify user.");
        } else {
            try {
                UserEntity userEntity = userEntities.get(0);

                LocalDate today = LocalDate.now();
                String todayInString = today.toString().replace("-", "");

                // String password = 1 + todayInString + "!";
                String password = generateRandomString(10, true, true);
                String passwordHash = MySQLUtils.toMd5(password);
                userEntity.setPassword(passwordHash);
                userRepository.save(userEntity);

                log.info("Password reset successfully. Password: {}", password);

                // Notify user & admins by email
                try {

                    // Add user email
                    List<String> recipients = new LinkedList<>();
                    recipients.add(email);

                    List<String> adminRecipients = settingService.getRecipientList(SettingEntity.MAILGUN_RECIPIENTS_RESET_PASSWORD);
                    recipients.addAll(adminRecipients);
                    log.info("Notify users. User: {} Admins: {}", email, adminRecipients);

                    String from = appConfig.getMailgunSender();
                    String subject = AppConfig.PROJECT_NAME + " - " + AppConfig.SERVICE_NAME + " : Reset password";

                    Map<String, String> variables = new HashMap<>();
                    variables.put(EmailBuilderService.VAR_USER_NAME, userEntity.getName());
                    variables.put(EmailBuilderService.VAR_SERVICE_NAME, AppConfig.SERVICE_NAME);
                    variables.put(EmailBuilderService.VAR_PASSWORD, password);
                    variables.put(EmailBuilderService.VAR_PROJECT_NAME, "Europeana Archaelogy");

                    String body = emailBuilderService.buildResetPasswordTemplate(
                            "Reset password request.", variables);
                    //log.info(body);

                    mailgunService.configure(appConfig.getMailgunDomainName(), appConfig.getMailgunApiKey());
                    mailgunService.sendMessage(from, recipients, subject, body, false);

                } catch(Exception ex) {
                    log.error("Reset password. Send email failed. Email: {}", email);
                }

                return true;
            } catch(NoSuchAlgorithmException ex) {
                log.error("",ex);
                return false;
            }
        }
    }

    public String generateRandomString(int length, boolean useLetters, boolean useNumbers) {

        return RandomStringUtils.random(length, useLetters, useNumbers);
    }
}
