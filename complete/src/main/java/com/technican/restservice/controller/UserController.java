package com.technican.restservice.controller;

import com.technican.restservice.model.User;
import com.technican.restservice.model.Role;
import com.technican.restservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;


    // Register a new user
    @PostMapping
    public ResponseEntity<?> register(@RequestBody Map<String, Object> req, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Accept roles as List<String> or String[] and convert to Set<Role>
        String username = (String) req.get("username");
        String password = (String) req.get("password");
        Object rolesObj = req.get("roles");
        Set<Role> roles = new HashSet<>();
        if (rolesObj instanceof List<?> list) {
            for (Object r : list) roles.add(Role.valueOf(r.toString()));
        } else if (rolesObj != null && rolesObj.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(rolesObj);
            for (int i = 0; i < len; i++) {
                Object r = java.lang.reflect.Array.get(rolesObj, i);
                roles.add(Role.valueOf(r.toString()));
            }
        }
        System.out.println("[DEBUG] Registering user '" + username + "' with roles: " + roles);
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRoles(roles);
        // Enforce first admin creation logic and role check for other users
        boolean isFirstAdmin = userService.findAll().isEmpty() && roles.contains(Role.ADMIN);
        if (!isFirstAdmin) {
            // All other users require an authenticated admin
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Admin authentication required"));
            }
            String token = authHeader.substring(7);
            // Validate token and check admin role
            try {
                var jwtUtil = userService.getJwtUtil();
                var jwtRoles = jwtUtil.getRolesFromToken(token);
                if (!jwtRoles.contains("ADMIN")) {
                    return ResponseEntity.status(403).body(Map.of("error", "Admin role required"));
                }
            } catch (Exception e) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
            }
        }
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Duplicate username"));
        }
        try {
            User created = userService.registerUser(user);
            Map<String, Object> response = Map.of(
                "id", created.getId(),
                "username", created.getUsername(),
                "roles", created.getRoles()
            );
            System.out.println("[DEBUG] Registered user response: " + response);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    // Delete a user (admin only)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        // Enforce admin role check
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Admin authentication required"));
        }
        String token = authHeader.substring(7);
        try {
            var jwtUtil = userService.getJwtUtil();
            var roles = jwtUtil.getRolesFromToken(token);
            if (!roles.contains("ADMIN")) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin role required"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}
