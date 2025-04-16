package org.example.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.userservice.controller.payload.NewUserPayload;
import org.example.userservice.entity.User;
import org.example.userservice.exception.NoSuchUserException;
import org.example.userservice.exception.NotAuthorizedException;
import org.example.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("task-manager-api/users")
@RequiredArgsConstructor
public class UsersController {
    private final UserService userService;

    @GetMapping
    public Iterable<User> getUsers() {
        return userService.findAll();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/current-user")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) throws NotAuthorizedException, NoSuchUserException {
        if (authentication == null) {
            throw new NotAuthorizedException("Authentication is null");
        }
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        Map<String, Object> attributes = jwtAuthenticationToken.getTokenAttributes();
        String username = (String) attributes.get("preferred_username");
        Optional<User> user = userService.findByUsername(username);
        if (!user.isPresent()) {
            throw new NoSuchUserException("User with username " + username + " not found");
        }
        return ResponseEntity
                .ok()
                .body(user.get());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody NewUserPayload payload, BindingResult bindingResult, UriComponentsBuilder builder) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            User added = userService.save(new User(payload));
            return ResponseEntity
                    .created(builder
                            .replacePath("task-manager-api/users/{id}")
                            .build(Map.of("id", added.getId())))
                    .body(added);
        }
    }

    @GetMapping("/by-username/{username}")
    public User findByUsername(@PathVariable("username") String username) {
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new NoSuchUserException("User with username " + username + " not found");
        }
    }

    @GetMapping("/id-by-username/{username}")
    public Integer findIdByUsername(@PathVariable("username") String username) {
        return userService.findUserIdByUsername(username);
    }
}
