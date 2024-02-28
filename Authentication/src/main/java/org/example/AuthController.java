package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if(user.getUsername() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username and password required");
        }
        User existingUser = null;
        try {
            existingUser = userRepository.findByUsername(user.getUsername());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error");
        }
        if(existingUser != null) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        try {
            userRepository.save(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error");
        }

        String token = JWTUtil.generateToken(user);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        if(user.getUsername() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username and password required");
        }
        User existingUser = null;
        try {
            existingUser = userRepository.findByUsername(user.getUsername());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error");
        }
        if(existingUser == null) {
            return ResponseEntity.badRequest().body("Invalid username");
        }
        if(!bCryptPasswordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid password");
        }
        Map<String, String> response = new HashMap<>();
        response.put("token", JWTUtil.generateToken(existingUser));
        response.put("username", existingUser.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validate(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        HashMap<String, String> response = new HashMap<>();
        String token = bearerToken.substring(7);
        boolean isValid = false;
        try {
            isValid = JWTUtil.validateToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token validation error");
        }
        if (isValid) {
            response.put("status", "valid");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
