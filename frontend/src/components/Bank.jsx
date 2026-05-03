import React, { useState, useEffect } from "react";
import { adminApi, userApi } from "../api/api";
import BankCard from "./BankCard";
import { useNavigate } from "react-router-dom";
import AdminPanel from "./AdminPanel";
function Bank() {
  const [transactions, setTransactions] = useState([]);
  
  const [amounts, setAmounts] = useState({}); // 👈 окремий amount для кожного acc
  const navigate = useNavigate();
const [newAccountName, setNewAccountName] = useState("");
  const [userId, setUserId] = useState(null);
const [selectedAccountId, setSelectedAccountId] = useState(null);
const [users, setUsers] = useState([]);
const [accounts, setAccounts] = useState([]);
const [role, setRole] = useState(null); 
useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) navigate("/");
  }, [navigate]);

  useEffect(() => {
  const token = localStorage.getItem("token");

  if (token) {
    try {
     const token = localStorage.getItem("token");

const base64Url = token.split(".")[1];

const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");

const payload = JSON.parse(atob(base64));

console.log(payload);
      setUserId(payload.userId || payload.sub);
    setRole(payload.role?.[0]?.replace("ROLE_", "")); // 👈 залежить від backend

    } catch (e) {
      console.error("Invalid token");
    }
  }
}, []);

const getUserAccounts = async () => {
  try {
    const res = await userApi.get("/accounts");
    setAccounts(res.data);
  } catch (err) {
    alert("Помилка завантаження акаунтів");
  }
};
  const getAllUsers = async () => {
  const res = await adminApi.get("/users");
  setUsers(res.data);
console.log(res.data);
};
useEffect(() => {
  if (selectedAccountId) {
    getTransactions(selectedAccountId);
  }
}, [selectedAccountId]);
  useEffect(() => {
  if (accounts.length > 0) {
    setSelectedAccountId(accounts[0].id);
  }
}, [accounts]);

useEffect(() => {
  if (role === "ADMIN") getAllUsers();
  else if (role) getUserAccounts();
}, [role]);
 

  const createAccount = async () => {
  try {
    const res = await userApi.post("/accounts", {
      name: newAccountName,
    });

    setAccounts(prev => [...prev, res.data]);
    setNewAccountName("");
  } catch (err) {
    console.error(err);
    alert("Помилка завантаження акаунтів");
  }
};
const setAmountForAccount = (id, value) => {
  setAmounts(prev => ({
    ...prev,
    [id]: value
  }));
};

const deposit = async (accountId) => {
  const amount = Number(amounts[accountId]);

if (!amount || amount <= 0) return;
  try {
    await userApi.post("/deposit", {
      accountId,
      amount: Number(amounts[accountId] || 0)
    });

    alert("Поповнення успішне");
    await getUserAccounts();
  } catch (err) {
    alert("Помилка депозиту");
  }
};
  const withdraw = async (accountId) => {
    try {
      await userApi.post("/withdraw", {
        accountId,
        amount: Number(amounts[accountId] || 0)
      });

      alert("Зняття успішне");
      await getUserAccounts();
    } catch (err) {
      alert("Помилка зняття");
    }
  };

  const logout = () => {
    localStorage.removeItem("token");
    navigate("/");
  };
const getTransactions = async (accountId) => {
  const res = await userApi.get(`/accounts/${accountId}/transactions`);
  setTransactions(res.data);
};
if (role === "ADMIN") {
  return (
    <div className="box">
      <h3>🛠 Admin Panel</h3>

      <button onClick={logout}>🚪 Logout</button>

      <hr />

      <h4>All users</h4>

      {users.map(user => (
        <div key={user.id} style={{ padding: "10px", border: "1px solid gray" }}>
          <b>{user.email}</b>
        </div>
      ))}
    </div>
  );
}
return (
  <div className="box">
    <h3>💳 My Bank</h3>

    <button onClick={getUserAccounts}>🔄 Refresh</button>
    <button onClick={logout}>🚪 Logout</button>

    <hr />

    <h4>User ID: {userId}</h4>

    <div className="layout" style={{ display: "flex", gap: "20px" }}>

      {/* LEFT SIDE - ACCOUNTS */}
      <div className="left" style={{ width: "40%" }}>
        {accounts.length > 0 ? (
          accounts.map(acc => (
            <div
              key={acc.id}
              onClick={() => {
                setSelectedAccountId(acc.id);
                getTransactions(acc.id);
              }}
              style={{
                padding: "10px",
                marginBottom: "10px",
                border: selectedAccountId === acc.id
                  ? "2px solid green"
                  : "1px solid gray",
                cursor: "pointer"
              }}
            >
              <BankCard account={acc} />

              <input
                placeholder="Amount"
                value={amounts[acc.id] || ""}
                onChange={(e) =>
                  setAmountForAccount(acc.id, e.target.value)
                }
              />

              <div style={{ display: "flex", gap: "10px", marginTop: "5px" }}>
                <button onClick={() => deposit(acc.id)}>
                  ➕ Deposit
                </button>

                <button onClick={() => withdraw(acc.id)}>
                  ➖ Withdraw
                </button>
              </div>
            </div>
          ))
        ) : (
          <div className="card">
            <h3>Create your first account</h3>

            <input
              placeholder="Account name"
              value={newAccountName}
              onChange={(e) => setNewAccountName(e.target.value)}
            />

            <button onClick={createAccount}>
              ➕ Create Account
            </button>
          </div>
        )}
      </div>

      {/* RIGHT SIDE - TRANSACTIONS */}
      <div className="right" style={{ width: "60%" }}>
        <h3>Transactions</h3>

        {transactions.length > 0 ? (
          transactions.map(tx => (
            <div key={tx.id} style={{ marginBottom: "10px" }}>
              <b>{tx.type}</b> - {tx.amount}
              <br />
              <small>{tx.createdAt}</small>
            </div>
          ))
        ) : (
          <p>No transactions</p>
        )}
      </div>

    </div>
  </div>
);
}

export default Bank;