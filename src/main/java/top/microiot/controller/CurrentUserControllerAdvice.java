package top.microiot.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import top.microiot.security.CustomUserDetails;

@ControllerAdvice
public class CurrentUserControllerAdvice {
    @ModelAttribute("currentUser")
    public CustomUserDetails getCurrentUser(Authentication authentication) {
        return (authentication == null) ? null : (CustomUserDetails) authentication.getPrincipal();
    }
}
