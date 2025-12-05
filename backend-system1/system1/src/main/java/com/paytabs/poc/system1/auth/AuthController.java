package com.paytabs.poc.system1.auth;

import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/auth")
public class AuthController {

    public static class User {
        public String username;
        public String passwordHash;
        public String role;       // "ROLE_ADMIN" or "ROLE_CUSTOMER"
        public String customerId; // null for admin
        public User(String u, String ph, String r, String cid){
            this.username = u;
            this.passwordHash = ph;
            this.role = r;
            this.customerId = cid;
        }
    }

    public static class Session {
        public String token;
        public String username;
        public String role;
        public String customerId;
        public Session(String t, String u, String r, String cid){
            this.token = t; this.username = u; this.role = r; this.customerId = cid;
        }
    }

    private static Map<String, User> users = new HashMap<>();
    private static Map<String, Session> sessions = new HashMap<>();

    @PostConstruct
    public void init(){
        // Passwords: admin123, cust123
        users.put("admin", new User("admin", sha256("admin123"), "ROLE_ADMIN", null));
        users.put("cust1", new User("cust1", sha256("cust123"), "ROLE_CUSTOMER", "cust-1"));
    }

    public static Session validateToken(String authHeader){
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return null;
        }
        String token = authHeader.substring("Bearer ".length()).trim();
        return sessions.get(token);
    }

    @PostMapping("/login")
    public Map<String,Object> login(@RequestBody Map<String,String> body){
        String username = body.get("username");
        String password = body.get("password");
        Map<String,Object> resp = new HashMap<>();

        if(username == null || password == null){
            resp.put("error","Username and password required");
            return resp;
        }
        User u = users.get(username);
        if(u == null){
            resp.put("error","Invalid credentials");
            return resp;
        }
        if(!u.passwordHash.equals(sha256(password))){
            resp.put("error","Invalid credentials");
            return resp;
        }

        String token = UUID.randomUUID().toString();
        Session s = new Session(token, u.username, u.role, u.customerId);
        sessions.put(token, s);

        resp.put("token", token);
        resp.put("role", u.role);
        resp.put("customerId", u.customerId);
        return resp;
    }

    private static String sha256(String s){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for(byte x : b){
                sb.append(String.format("%02x", x));
            }
            return sb.toString();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
