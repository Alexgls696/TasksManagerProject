package org.example.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.userservice.controller.payload.UpdateUserPayload;
import org.example.userservice.entity.User;
import org.example.userservice.exception.NoSuchUserException;
import org.example.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("task-manager-api/users/{id:\\d+}")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public User findUserById(@PathVariable("id") int id) {
        return userService.findById(id).orElseThrow(()->new NoSuchUserException("User with id "+id+" not found"));
    }

    @GetMapping("/initials")
    public String getUserInitialsById(@PathVariable("id") int id) {
        var user = userService.findById(id)
                .orElseThrow(()->new NoSuchUserException("User with id "+id+"not found"));
        return user.getName() + " "+user.getSurname();
    }

    @PatchMapping
    public ResponseEntity<Void> updateUser(@PathVariable("id") int id, @Valid @RequestBody UpdateUserPayload payload, BindingResult bindingResult) throws BindException {
        if(bindingResult.hasErrors()) {
            if(bindingResult instanceof BindException exception){
                throw exception;
            }else{
                throw new BindException(bindingResult);
            }
        }else{
            User added = userService.update(id, payload);
            return ResponseEntity
                    .noContent()
                    .build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@PathVariable("id") int id) {
        userService.delete(id);
        return ResponseEntity
                .noContent().build();
    }

}
