package com.paytabs.poc.system1.controller;
import com.paytabs.poc.system1.auth.AuthController;
import com.paytabs.poc.system1.auth.AuthController.Session;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Map;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class TransactionController {

    private final RestTemplate rt = new RestTemplate();
    private final String SYSTEM2_URL = "http://localhost:8081";

    @PostMapping("/transactions")
    public ResponseEntity<?> transact(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String,Object> req) {

        Session session = AuthController.validateToken(authHeader);
        if(session == null){
            return ResponseEntity.status(401).body(Map.of("error","Unauthorized"));
        }

        // basic validation
        if(!req.containsKey("cardNumber")||!req.containsKey("pin")
                ||!req.containsKey("amount")||!req.containsKey("type")){
            return ResponseEntity.badRequest().body(Map.of("error","Missing required fields"));
        }

        String card = req.get("cardNumber").toString();
        double amount;
        try {
            amount = Double.parseDouble(req.get("amount").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error","Invalid amount"));
        }
        String type = req.get("type").toString();
        if(amount <= 0) return ResponseEntity.badRequest().body(Map.of("error","Amount must be > 0"));
        if(!("withdraw".equalsIgnoreCase(type) || "topup".equalsIgnoreCase(type)))
            return ResponseEntity.badRequest().body(Map.of("error","Invalid type"));

        if(!card.startsWith("4")){
            return ResponseEntity.status(422).body(Map.of("error","Card range not supported"));
        }

        // Attach customerId so System2 can log correctly
        if(session.customerId != null){
            req.put("customerId", session.customerId);
        }

        // forward to system2
        String url = SYSTEM2_URL + "/process";
        try{
            ResponseEntity<Map> resp = rt.postForEntity(url, req, Map.class);
            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());
        }catch(Exception e){
            return ResponseEntity.status(500).body(Map.of("error","Failed to contact system2","detail", e.getMessage()));
        }
    }
    @GetMapping("/admin/transactions")
    public ResponseEntity<?> getAllTransactions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Session session = AuthController.validateToken(authHeader);
        if(session == null){
            return ResponseEntity.status(401).body(Map.of("error","Unauthorized"));
        }
        if(!"ROLE_ADMIN".equals(session.role)){
            return ResponseEntity.status(403).body(Map.of("error","Forbidden"));
        }

        String url = SYSTEM2_URL + "/txlog";
        try{
            ResponseEntity<Object> resp = rt.getForEntity(url, Object.class);
            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());
        }catch(Exception e){
            return ResponseEntity.status(500).body(Map.of("error","Failed to contact system2","detail", e.getMessage()));
        }
    }

    @GetMapping("/customer/transactions")
    public ResponseEntity<?> getCustomerTransactions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Session session = AuthController.validateToken(authHeader);
        if(session == null){
            return ResponseEntity.status(401).body(Map.of("error","Unauthorized"));
        }
        if(!"ROLE_CUSTOMER".equals(session.role)){
            return ResponseEntity.status(403).body(Map.of("error","Forbidden"));
        }
        if(session.customerId == null){
            return ResponseEntity.status(400).body(Map.of("error","No customerId associated with this user"));
        }

        String url = SYSTEM2_URL + "/txlog/customer/" + session.customerId;
        try{
            ResponseEntity<Object> resp = rt.getForEntity(url, Object.class);
            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());
        }catch(Exception e){
            return ResponseEntity.status(500).body(Map.of("error","Failed to contact system2","detail", e.getMessage()));
        }
    }

}

