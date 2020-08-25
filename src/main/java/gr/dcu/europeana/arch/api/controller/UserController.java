package gr.dcu.europeana.arch.api.controller;

import gr.dcu.europeana.arch.api.dto.user.ChangePasswordRequest;
import gr.dcu.europeana.arch.api.dto.user.UserUpdateRequest;
import gr.dcu.europeana.arch.api.dto.user.UserViewDto;
import gr.dcu.europeana.arch.service.AuthService;
import gr.dcu.europeana.arch.service.UserService;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.core.MediaType;
import java.util.List;

@CrossOrigin
@RestController
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    public UserController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<UserViewDto> getUsers(HttpServletRequest requestContext) {
        int requesterId = authService.authorize(requestContext);
        return userService.getUsers();
    }

    @GetMapping("/users/{id}")
    public UserViewDto getUser(HttpServletRequest requestContext,
                               @PathVariable Integer id) {
        int requesterId = authService.authorize(requestContext);
        return userService.getUser(id);
    }

    @PutMapping(value = "/users/{id}", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public UserViewDto updateUser(HttpServletRequest requestContext,
                                  @PathVariable Integer id,
                                  @RequestBody @Valid UserUpdateRequest updateRequest) {

        int requesterId = authService.authorize(requestContext);
        return userService.updateUser(id, updateRequest);
    }

    @PostMapping("/users/{id}/passwd")
    public UserViewDto changePassword(HttpServletRequest requestContext,
                                      @PathVariable Integer id,
                                      @RequestBody @Valid ChangePasswordRequest request) {

        int requesterId = authService.authorize(requestContext);
        return userService.changePassword(id, request.getPassword());
    }
}
