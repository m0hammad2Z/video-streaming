package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

@Controller
public class UploadVideoController {
    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Environment environment;

    private RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/uploadvideo")
    public String uploadVideoPage() {
        return "upload";
    }

    @PostMapping("/videos")
    public ResponseEntity<String> uploadVideo(@RequestParam("title") String title, @RequestParam("url") String url, HttpServletRequest request) {
        if (title == null || title.isEmpty() || url == null || url.isEmpty()) {
            return ResponseEntity.badRequest().body("Title and url required");
        }

        // Get the username from the session
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");

        System.out.println("Username: " + username);
        // Check if the user exists
        User user;
        try {
            user = userRepository.findByUsername(username);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error");
        }
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Video video = new Video();
        video.setTitle(title);
        video.setUrl(url);
        video.setUser(user);
        try {
            videoRepository.save(video);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error");
        }
        return ResponseEntity.ok("Video uploaded successfully");
    }

        @GetMapping("/login")
        public String loginPage() {
            return "login";
        }

        @GetMapping("/register")
        public String registerPage() {
            return "register";
        }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam("username") String username, @RequestParam("password") String password, HttpSession session) {
        Map<String, String> response = new HashMap<>() ;
        response.put("username", username);
        response.put("password", password);
        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(environment.getProperty("service.auth.url") + "/login", response, String.class);
            if (responseEntity.getStatusCodeValue() == 200) {
                String token = responseEntity.getBody();
                session.setAttribute("token", token);
                session.setAttribute("username", username);
                return ResponseEntity.ok(token);
            }
        } catch (HttpClientErrorException.BadRequest ex) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
        return ResponseEntity.badRequest().body("An error occurred");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam("username") String username, @RequestParam("password") String password, HttpSession session) {
        Map<String, String> response = new HashMap<>() ;
        response.put("username", username);
        response.put("password", password);
        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(environment.getProperty("service.auth.url") + "/register", response, String.class);
            if (responseEntity.getStatusCodeValue() == 200) {
                String token = responseEntity.getBody();
                session.setAttribute("token", token);
                session.setAttribute("username", username);
                return ResponseEntity.ok(token);
            }
        } catch (HttpClientErrorException.BadRequest ex) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
        return ResponseEntity.badRequest().body("An error occurred");
    }
}