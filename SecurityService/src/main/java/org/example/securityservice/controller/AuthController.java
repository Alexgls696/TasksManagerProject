package org.example.securityservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.securityservice.controller.payload.NewUserPayload;
import org.example.securityservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/registration")
    @ResponseBody
    public String registration(@RequestBody NewUserPayload payload){
        userService.registerUser(payload);
        return "http://localhost:8080/security/login/oauth2/code/keycloak";
    }


    @GetMapping("/manager")
    public String manager() {
        return "managers";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        String username = oidcUser.getPreferredUsername();
        String firstName = oidcUser.getGivenName();
        String lastName = oidcUser.getFamilyName();

        Map<String, Object> claims = oidcUser.getClaims();

        List<String> roles = oidcUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "email", email,
                "username", username,
                "firstName", firstName,
                "lastName", lastName,
                "roles", roles
        ));
    }
}