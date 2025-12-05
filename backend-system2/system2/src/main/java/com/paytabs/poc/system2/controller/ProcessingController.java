package com.paytabs.poc.system2.controller;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;
import com.paytabs.poc.system2.crypto.CardCryptoUtil;
import java.security.MessageDigest;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.*;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class ProcessingController {
	static class Card {
        public String encryptedCardNumber;
        public String pinHash;
        public String customerId;
        public double balance;
        public Card(String encCard, String pinHash, String customerId, double balance){
            this.encryptedCardNumber = encCard;
            this.pinHash = pinHash;
            this.customerId = customerId;
            this.balance = balance;
        }
}
	private Map<String, Card> cards = new HashMap<>();
    private List<Map<String,Object>> txlog = new ArrayList<>();
    @PostConstruct
    public void init(){
    	// Seed a card: plain cardNumber, but stored ENCRYPTED
        String cardNumber = "4123456789012345";
        String encrypted = CardCryptoUtil.encrypt(cardNumber);
        cards.put(encrypted,
                new Card(encrypted, sha256("1234"), "cust-1", 500.0));
    }

    @PostMapping("/process")
    public Map<String,Object> process(@RequestBody Map<String,Object> req){
        String card = (String)req.get("cardNumber");
        String pin = (String)req.get("pin");
        Double amount = Double.valueOf(req.get("amount").toString());
        String type = (String)req.get("type");

        Map<String,Object> resp = new HashMap<>();
        Map<String,Object> tx = new HashMap<>();
        tx.put("transactionId", UUID.randomUUID().toString());
        tx.put("cardNumber", mask(card));
        tx.put("type", type);
        tx.put("amount", amount);
        tx.put("timestamp", new Date().toString());

        // NEW: customerId may come from System1
        String customerIdFromReq = req.get("customerId") != null ? req.get("customerId").toString() : null;

        // Look up card by ENCRYPTED cardNumber
        String encryptedCard = CardCryptoUtil.encrypt(card);
        Card c = cards.get(encryptedCard);
        if(c == null){
            resp.put("status","failed"); resp.put("reason","Invalid card");
            tx.put("status","failed"); tx.put("reason","Invalid card");
            txlog.add(tx);
            return resp;
        }

        // Attach customerId (from card or from request)
        String effectiveCustomerId = customerIdFromReq != null ? customerIdFromReq : c.customerId;
        tx.put("customerId", effectiveCustomerId);

        if(!c.pinHash.equals(sha256(pin))){
            resp.put("status","failed"); resp.put("reason","Invalid PIN");
            tx.put("status","failed"); tx.put("reason","Invalid PIN"); txlog.add(tx);
            return resp;
        }

        if("withdraw".equalsIgnoreCase(type)){
            if(c.balance < amount){
                resp.put("status","failed"); resp.put("reason","Insufficient balance");
                tx.put("status","failed"); tx.put("reason","Insufficient balance"); txlog.add(tx);
                return resp;
            }
            c.balance -= amount;
            resp.put("status","success"); resp.put("balance", c.balance);
            tx.put("status","success");
        }
        else if("topup".equalsIgnoreCase(type)){
            c.balance += amount;
            resp.put("status","success"); resp.put("balance", c.balance);
            tx.put("status","success");
        }
        else{
            resp.put("status","failed"); resp.put("reason","Invalid type");
            tx.put("status","failed"); tx.put("reason","Invalid type");
        }

        txlog.add(tx);
        return resp;
    }

    @GetMapping("/cards")
    public Collection<Card> listCards(){ return cards.values(); }

    @GetMapping("/txlog")
    public List<Map<String,Object>> txlog(){ return txlog; }
    
    @GetMapping("/txlog/customer/{customerId}")
    public List<Map<String,Object>> txlogForCustomer(@PathVariable String customerId){
        return txlog.stream()
                .filter(tx -> customerId.equals(tx.get("customerId")))
                .collect(Collectors.toList());
    }


    private static String sha256(String s){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for(byte x: b) sb.append(String.format("%02x", x));
            return sb.toString();
        }catch(Exception e){ throw new RuntimeException(e); }
    }

    private static String mask(String card){
        if(card==null || card.length()<4) return "****";
        return "****-****-****-" + card.substring(card.length()-4);
    }
}
