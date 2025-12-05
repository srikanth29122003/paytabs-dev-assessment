import React, { useState } from "react";
import Admin from "./Admin";
import "./App.css"; 

function App() {
  const [page, setPage] = useState("customer");
  const [token, setToken] = useState(null);
  const [role, setRole] = useState(null);
  // eslint-disable-next-line no-unused-vars
  const [customerId, setCustomerId] = useState(null);

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  
  const [cardNumber, setCardNumber] = useState("");
  const [pin, setPin] = useState("");
  const [amount, setAmount] = useState("");
  const [type, setType] = useState("topup");
  const [response, setResponse] = useState(null);
  const [history, setHistory] = useState([]);

  const handleLogin = async () => {
    setResponse(null);
    try {
      const res = await fetch("http://localhost:8080/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });
      const data = await res.json();
      if (data.error) {
        alert(data.error);
        return;
      }
      setToken(data.token);
      setRole(data.role);
      setCustomerId(data.customerId);
      setPassword("");
      if (data.role === "ROLE_ADMIN") {
        setPage("admin");
      } else {
        setPage("customer");
      }
    } catch (err) {
      alert("Login failed");
    }
  };

  const handleLogout = () => {
    setToken(null);
    setRole(null);
    setCustomerId(null);
    setUsername("");
    setPassword("");
    setResponse(null);
    setHistory([]);
    setPage("customer");
  };

  const handleSubmit = async () => {
    setResponse(null);
    const body = {
      cardNumber,
      pin,
      amount: parseFloat(amount),
      type,
    };

    try {
      const res = await fetch("http://localhost:8080/transactions", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + token,
        },
        body: JSON.stringify(body),
      });
      const data = await res.json();
      setResponse(data);
    } catch (err) {
      setResponse({ error: "Backend unreachable" });
    }
  };

  const loadHistory = async () => {
    try {
      const res = await fetch("http://localhost:8080/customer/transactions", {
        headers: {
          Authorization: "Bearer " + token,
        },
      });
      const data = await res.json();
      setHistory(data);
    } catch (err) {
      alert("Failed to load history");
    }
  };

  
  if (!token) {
    return (
      <div className="login-wrapper">
        <div className="card">
          <h2 style={{ textAlign: "center" }}>Welcome Back</h2>
          
          <div className="form-group">
            <label>Username</label>
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Enter username"
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter password"
            />
          </div>

          <button className="btn btn-primary" onClick={handleLogin}>
            Sign In
          </button>

          <div className="demo-info">
            <strong>Demo Credentials:</strong>
            <br />
            Admin: admin / admin123
            <br />
            Customer: cust1 / cust123
          </div>
        </div>
      </div>
    );
  }

  
  return (
    <div className="app-container">
      {}
      <nav className="navbar">
        <span style={{ fontWeight: "bold", fontSize: "1.1rem" }}>
          Bank Portal <span style={{ opacity: 0.7, fontWeight: "normal", fontSize: "0.9rem", marginLeft: "10px" }}> | {username} ({role})</span>
        </span>
        <button className="btn btn-danger" onClick={handleLogout}>
          Logout
        </button>
      </nav>

      {}
      <div className="nav-tabs">
        {role === "ROLE_CUSTOMER" && (
          <button
            className={`tab-btn ${page === "customer" ? "active" : ""}`}
            onClick={() => setPage("customer")}
          >
            Customer Dashboard
          </button>
        )}

        {role === "ROLE_ADMIN" && (
          <button
            className={`tab-btn ${page === "admin" ? "active" : ""}`}
            onClick={() => setPage("admin")}
          >
            Admin Dashboard
          </button>
        )}
      </div>

      {}
      {role === "ROLE_CUSTOMER" && page === "customer" && (
        <div className="content-container">
          <h2>New Transaction</h2>

          <div className="form-group">
            <label>Card Number</label>
            <input
              type="text"
              value={cardNumber}
              onChange={(e) => setCardNumber(e.target.value)}
              placeholder="0000 0000 0000 0000"
            />
          </div>

          <div style={{ display: "flex", gap: "15px" }}>
            <div className="form-group" style={{ flex: 1 }}>
              <label>PIN</label>
              <input
                type="password"
                value={pin}
                onChange={(e) => setPin(e.target.value)}
                placeholder="****"
              />
            </div>

            <div className="form-group" style={{ flex: 1 }}>
              <label>Amount</label>
              <input
                type="number"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="0.00"
              />
            </div>
          </div>

          <div className="form-group">
            <label>Transaction Type</label>
            <select value={type} onChange={(e) => setType(e.target.value)}>
              <option value="topup">Top-up</option>
              <option value="withdraw">Withdraw</option>
            </select>
          </div>

          <button className="btn btn-primary" onClick={handleSubmit}>
            Submit Transaction
          </button>

          {response && (
  <div className={`status-card ${response.error ? "status-error" : "status-success"}`}>
    
    {}
    <div className="status-title">
      {response.error ? "❌ Transaction Failed" : "✅ Transaction Successful"}
    </div>

    {}
    {response.error && (
      <div>
        <p style={{ margin: 0 }}>{response.error}</p>
      </div>
    )}

    {}
    {!response.error && (
      <div>
        <p style={{ margin: "5px 0" }}>
          Your transaction has been processed securely.
        </p>
        <div style={{ 
            marginTop: "10px", 
            padding: "10px", 
            background: "rgba(255,255,255,0.5)", 
            borderRadius: "6px",
            fontSize: "0.9rem" 
          }}>
          <strong>Transaction ID:</strong> {response.transactionId || "N/A"} <br />
          <strong>Current Status:</strong> {response.status} <br />
          {response.balance && (
             <span><strong>New Balance:</strong> ${response.balance}</span>
          )}
        </div>
      </div>
    )}
  </div>
)}

          <hr style={{ margin: "30px 0", border: 0, borderTop: "1px solid #e2e8f0" }} />

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
            <h3>Transaction History</h3>
            <button className="btn btn-success" onClick={loadHistory}>
              Refresh History
            </button>
          </div>

          <div style={{ overflowX: "auto" }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Card</th>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th>Time</th>
                </tr>
              </thead>
              <tbody>
                {Array.isArray(history) && history.length > 0 ? (
                  history.map((tx, index) => (
                    <tr key={index}>
                      <td>#{tx.transactionId}</td>
                      <td>{tx.cardNumber}</td>
                      <td>
                        <span style={{ 
                          textTransform: 'capitalize', 
                          padding: '4px 8px', 
                          borderRadius: '4px',
                          background: tx.type === 'topup' ? '#dbeafe' : '#fef3c7',
                          color: tx.type === 'topup' ? '#1e40af' : '#92400e',
                          fontWeight: 'bold',
                          fontSize: '0.8rem'
                        }}>
                          {tx.type}
                        </span>
                      </td>
                      <td style={{ fontWeight: 'bold' }}>${tx.amount}</td>
                      <td>{tx.status}</td>
                      <td style={{ color: '#64748b', fontSize: '0.85rem' }}>{tx.timestamp}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="6" style={{ textAlign: "center", padding: "20px" }}>
                      No history loaded. Click refresh.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {}
      {role === "ROLE_ADMIN" && page === "admin" && (
        <div className="content-container">
           <Admin token={token} />
        </div>
      )}
    </div>
  );
}

export default App;