package gr.dcu.europeana.arch.service;

import gr.dcu.europeana.arch.api.controller.mapper.UserMapper;
import gr.dcu.europeana.arch.api.dto.user.UserUpdateRequest;
import gr.dcu.europeana.arch.api.dto.user.UserViewDto;
import gr.dcu.europeana.arch.domain.entity.UserEntity;
import gr.dcu.europeana.arch.exception.InvalidPasswordException;
import gr.dcu.europeana.arch.exception.NotFoundException;
import gr.dcu.europeana.arch.repository.UserRepository;
import gr.dcu.utils.MySQLUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UserMapper userMapper;

    public List<UserViewDto> getUsers() {
        return userRepo.findAll().stream()
                .map(userMapper::toUserViewDto)
                .collect(Collectors.toList());
    }

    public UserViewDto getUser(int userId) {
        return userMapper.toUserViewDto(userRepo.findById(userId).orElseThrow(() ->
                new NotFoundException("User", (long) userId)));

    }

    public UserViewDto updateUser(int userId, UserUpdateRequest updateRequest) {

        // log.info("{}", newUser);
        return userMapper.toUserViewDto(userRepo.findById(userId)
                .map(user -> {
                    user.setName(updateRequest.getName());
                    user.setEmail(updateRequest.getEmail());
                    user.setOrganization(updateRequest.getOrganization());
                    return userRepo.save(user);
                })
                .orElseThrow(() -> new NotFoundException("User", (long) userId)));
    }

    public UserViewDto changePassword(int userId, String password) throws InvalidPasswordException {

        UserEntity userEntity = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User",  (long) userId));

        if(password == null || password.isEmpty()) {
            throw new InvalidPasswordException((long) userId);
        }

        try {
            String passwordhash = MySQLUtils.toMd5(password);
            userEntity.setPassword(passwordhash);
            log.info("Password changed. User {}", userId);
        } catch (NoSuchAlgorithmException ex) {
            throw new InvalidPasswordException((long) userId);
        }

        return userMapper.toUserViewDto(userRepo.save(userEntity));
    }
}
