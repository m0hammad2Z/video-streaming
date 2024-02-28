package org.example;

import ch.qos.logback.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Controller
public class VideoFileController {

    @Autowired
    private Environment environment;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadVideo(@RequestParam("video") MultipartFile file, @RequestParam("token") String token, HttpSession session) {
        if(file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload");
        }

        if(token.isEmpty()) {
            return ResponseEntity.badRequest().body("Unauthorized");
        }

        if (!file.getContentType().equals("video/mp4")) {
            return ResponseEntity.badRequest().body("Only mp4 videos are allowed");
        }

        if (file.getSize() > 100000000) {
            return ResponseEntity.badRequest().body("File size too large. Maximum file size is 100MB");
        }

        session.setAttribute("token", token);

        String videoUrl = "";
        try {
           videoUrl = saveVideoFile(file);
           if (videoUrl == null) {
               return ResponseEntity.badRequest().body("Video upload failed 3");
           }
            ResponseEntity<String> response = saveVideoDetails(file.getOriginalFilename(), videoUrl, token);
            if (response.getStatusCodeValue() != 200) {
                try {
                    Files.deleteIfExists(Paths.get(videoUrl));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return ResponseEntity.badRequest().body("Video upload failed 2");
            }

            return ResponseEntity.ok("Video uploaded successfully");

        } catch (Exception e) {
           System.out.println("Exception: " + e.getMessage());
           try {
               assert videoUrl != null;
               Files.deleteIfExists(Paths.get(videoUrl));
           } catch (IOException ex) {
               throw new RuntimeException(ex);
           }
           return ResponseEntity.badRequest().body("Video upload failed");
       }
    }


//    @PostMapping
    // Make a request to the video database to save the video details
    private ResponseEntity<String> saveVideoDetails(String title, String url, String token) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", title);
        map.add("url", url);
        map.add("token", token);

        headers.add("Authorization", "Bearer " + token);

        return restTemplate.postForEntity(environment.getProperty("service.upload.url") + "/videos", map, String.class);
    }

    private String saveVideoFile(MultipartFile file) {
        //create a folder in the project's base directory if it does not exist
        String dirName = "videos";
        String projectBaseDir = System.getProperty("user.dir");
        Path folderPath = Paths.get(projectBaseDir, dirName);
        if (!Files.exists(folderPath)) {
            try {
                Files.createDirectory(folderPath);
            } catch (IOException e) {
                return null;
            }
        }

        Path path = Paths.get(folderPath.toString(), UUID.randomUUID() + Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".")));
        try {
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path.getFileName().toString();
    }
}

