import React, { useState } from "react";

function Admin({ token }) {
  const [transactions, setTransactions] = useState([]);

  const fetchTransactions = async () => {
    try {
      const res = await fetch("http://localhost:8080/admin/transactions", {
        headers: {
          Authorization: "Bearer " + token,
        },
      });
      const data = await res.json();
      setTransactions(data);
    } catch (err) {
      alert("Failed to fetch transactions");
    }
  };

  return (
    <div style={{ padding: "20px", maxWidth: "800px", margin: "auto" }}>
      <h2>Admin - Transaction Logs</h2>

      <button
        onClick={fetchTransactions}
        style={{
          padding: "10px 15px",
          background: "green",
          color: "white",
          border: "none",
        }}
      >
        Load Transactions
      </button>

      <table
        style={{
          width: "100%",
          marginTop: "20px",
          borderCollapse: "collapse",
        }}
      >
        <thead>
          <tr style={{ background: "#eee" }}>
            <th>ID</th>
            <th>Card</th>
            <th>Type</th>
            <th>Amount</th>
            <th>Status</th>
            <th>Customer</th>
            <th>Time</th>
          </tr>
        </thead>
        <tbody>
          {Array.isArray(transactions) &&
            transactions.map((tx, index) => (
              <tr key={index} style={{ borderBottom: "1px solid #ccc" }}>
                <td>{tx.transactionId}</td>
                <td>{tx.cardNumber}</td>
                <td>{tx.type}</td>
                <td>{tx.amount}</td>
                <td>{tx.status}</td>
                <td>{tx.customerId}</td>
                <td>{tx.timestamp}</td>
              </tr>
            ))}
        </tbody>
      </table>
    </div>
  );
}

export default Admin;
