package com.technican.restservice.controller;

import com.technican.restservice.model.User;
import com.technican.restservice.security.JwtUtil;
import com.technican.restservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");
        var userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty() || !userService.checkPassword(password, userOpt.get().getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        User user = userOpt.get();
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));
        return ResponseEntity.ok(Map.of("token", token));
    }
}
