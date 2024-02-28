package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

@org.springframework.stereotype.Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${service.auth.url}")
    private String authUrl;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(shouldNotFilter(request)){
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession();
        String token = (String) session.getAttribute("token");
        if (token == null) {
            token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }
        if (token == null) {
            token = request.getParameter("token");
        }

        if (token == null) {
            response.sendRedirect("./login");
            return;
        }

        JsonReader jsonReader = Json.createReader(new StringReader(token));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();
        token = jsonObject.getString("token");

        // Get the username from the token


        if(token == null || validateToken(token) == null){
            response.sendRedirect("./login");
            return;
        }

        String username = jsonObject.getString("username");
        session.setAttribute("username", username);

        System.out.println("Username: " + username);

        filterChain.doFilter(request, response);
    }

    public String validateToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    authUrl + "/validate",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return null;
            }
            throw e;
        }
        if (response.getStatusCodeValue() == 200) {
            return response.getBody();
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/login") || path.startsWith("/register");
    }
}