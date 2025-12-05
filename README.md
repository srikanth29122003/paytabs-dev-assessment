# paytabs-dev-assessment
# PayTabs Dev Assessment — Secure Banking POC

**Developed by: Srikanth E M — Java Full Stack Developer**

---

###  Overview

This project is a secure microservices-based Proof of Concept (POC) demonstrating:
- Secure card transactions (Withdraw / Top-up)
- PIN hashing using SHA-256
- Card data encryption at rest (AES encryption)
- In-memory Core Banking operations
- Role-based access control with Token Authentication
- Complete UI for Customer & Super Admin

---

##  Architecture
React UI
 ├─ Customer Portal → Initiate transactions / View own history
 └─ Admin Portal → View all transactions

System 1: Transaction Router (Spring Boot)
 ├─ Validates input
 ├─ Routes only cards starting with '4'
 └─ Auth & Role enforcement

System 2: Core Banking (Spring Boot)
 ├─ Validates card & PIN (SHA-256 hash verify)
 ├─ Encrypted card storage
 ├─ Balance update
 └─ Logs transactions per customer
 
 Communication: REST JSON over HTTP
Database: In-Memory (HashMaps)

TECHSTACK

| Layer    | Technology                             |
| -------- | -------------------------------------- |
| Frontend | React.js                               |
| Backend  | Java Spring Boot (System 1 & System 2) |
| Security | SHA-256 (PIN) + AES Encryption (Card)  |
| Data     | In-Memory DB                           |

SECURITY HIGHLIGHT

| Data          | Secured by                                       |
| ------------- | ------------------------------------------------ |
| PIN           | SHA-256 hash (never stored/logged in plain text) |
| Card Number   | AES Encryption at rest + masked in logs          |
| Authorization | Token-based (Admin & Customer roles)             |

RUNNING THE PROJECT
1.Start System 2 (Core Banking)
cd backend-system1/system1
./mvnw.cmd spring-boot:run
Runs on localhost:8081

2.Start System 1 (Router)
cd backend-system1/system1
./mvnw.cmd spring-boot:run
Runs on localhost:8080

3.Start Frontend
cd frontend
npm install
npm start
Runs on localhost:3000

DEMO USERS

| Role     | Username | Password |
| -------- | -------- | -------- |
| Customer | cust1    | cust123  |
| Admin    | admin    | admin123 |

API ENDPOINTS

| Method | Endpoint                 | Purpose                            |
| ------ | ------------------------ | ---------------------------------- |
| POST   | `/auth/login`            | Authenticate user & return token   |
| POST   | `/transactions`          | Route valid card operations        |
| GET    | `/customer/transactions` | Customer's own transaction history |
| GET    | `/admin/transactions`    | All transactions (Admin-only)      |

TESTCASES

| Test Case              | Input                       | Expected Output               |
| ---------------------- | --------------------------- | ----------------------------- |
| Valid Top-up           | 4123..., PIN=1234, amount>0 | Success + updated balance     |
| Valid Withdrawal       | Amount ≤ balance            | Success                       |
| Invalid PIN            | Wrong PIN                   | Failure: `"Invalid PIN"`      |
| Invalid Card           | Card not in DB              | Failure: `"Invalid card"`     |
| Unsupported Card Range | Card not starting with '4'  | `"Card range not supported"`  |
| Insufficient Balance   | Withdraw > balance          | `"Insufficient balance"`      |
| Admin Monitoring       | Login as admin → load data  | Shows **all** transactions    |
| Customer Monitoring    | Login as cust1              | Shows only cust1 transactions |
<img width="1366" height="768" alt="Screenshot (91)" src="https://github.com/user-attachments/assets/8a3f91dc-5c2d-4eb2-a33b-b627cfb026a9" />
<img width="1366" height="768" alt="Screenshot (92)" src="https://github.com/user-attachments/assets/6c75df5b-e616-4bbb-95c2-d865878158bc" />
<img width="1366" height="768" alt="Screenshot (93)" src="https://github.com/user-attachments/assets/28ee79f6-d8b9-4ca5-bf85-476d366dcda8" />
<img width="1366" height="768" alt="Screenshot (94)" src="https://github.com/user-attachments/assets/c909556c-ed90-4032-bc31-1ed4a7b43e62" />


