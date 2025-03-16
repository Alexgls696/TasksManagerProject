package org.example.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.userservice.controller.payload.NewUserPayload;
import org.example.userservice.entity.User;
import org.example.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("task-manager-api/users")
@RequiredArgsConstructor
public class UsersController {
    private final UserService userService;

    @GetMapping
    public Iterable<User> getUsers() {
        return userService.findAll();
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody NewUserPayload payload, BindingResult bindingResult, UriComponentsBuilder builder)  throws BindException {
        if(bindingResult.hasErrors()) {
            if(bindingResult instanceof BindException exception) {
                throw exception;
            }else{
                throw new BindException(bindingResult);
            }
        }else{
            User added = userService.save(new User(payload));
            return ResponseEntity
                    .created(builder
                            .replacePath("task-manager-api/users/{id}")
                            .build(Map.of("id",added.getId())))
                    .body(added);
        }
    }
}
