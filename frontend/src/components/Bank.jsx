import React, { useState, useEffect, useRef, useCallback } from "react";
import { adminApi, userApi, newIdempotencyKey, errorMessage } from "../api/api";
import BankCard from "./BankCard";
import { useNavigate } from "react-router-dom";
import AdminPanel from "./AdminPanel";
import { getAuthData } from "./auth";

// Сума: додатне число максимум з 2 знаками після коми (узгоджено з валідацією на бекенді)
const AMOUNT_RE = /^\d+([.,]\d{1,2})?$/;

function Bank() {
  const [transactions, setTransactions] = useState([]);
  const [amounts, setAmounts] = useState({});
  const [newAccountName, setNewAccountName] = useState("");
  const [userId, setUserId] = useState(null);
  const [selectedAccountId, setSelectedAccountId] = useState(null);
  const [users, setUsers] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [role, setRole] = useState(null);
  const [busy, setBusy] = useState(false);
  const navigate = useNavigate();

  // Ключі ідемпотентності незавершених операцій. Якщо відповідь загубилась (мережа),
  // повторне натискання використає ТОЙ САМИЙ ключ, і гроші не зарахуються двічі.
  const pendingKeys = useRef({});

  useEffect(() => {
    const auth = getAuthData();
    if (!auth) {
      navigate("/");
      return;
    }
    setUserId(auth.userId);
    setRole(auth.role);
  }, [navigate]);

  const getUserAccounts = useCallback(async () => {
    try {
      const res = await userApi.get("/accounts");
      setAccounts(res.data);
    } catch (err) {
      alert(errorMessage(err, "Помилка завантаження акаунтів"));
    }
  }, []);

  const getAllUsers = useCallback(async () => {
    try {
      const res = await adminApi.get("/users");
      setUsers(res.data);
    } catch (err) {
      alert(errorMessage(err, "Помилка завантаження користувачів"));
    }
  }, []);

  const getTransactions = useCallback(async (accountId) => {
    try {
      const res = await userApi.get(`/accounts/${accountId}/transactions`);
      setTransactions(res.data);
    } catch (err) {
      alert(errorMessage(err, "Помилка завантаження транзакцій"));
    }
  }, []);

  useEffect(() => {
    if (selectedAccountId) getTransactions(selectedAccountId);
  }, [selectedAccountId, getTransactions]);

  useEffect(() => {
    if (accounts.length > 0 && !selectedAccountId) setSelectedAccountId(accounts[0].id);
  }, [accounts, selectedAccountId]);

  useEffect(() => {
    if (role === "ADMIN") getAllUsers();
    else if (role) getUserAccounts();
  }, [role, getAllUsers, getUserAccounts]);

  const createAccount = async () => {
    try {
      const res = await userApi.post("/accounts", { name: newAccountName });
      setAccounts((prev) => [...prev, res.data]);
      setNewAccountName("");
    } catch (err) {
      alert(errorMessage(err, "Помилка створення акаунта"));
    }
  };

  const setAmountForAccount = (id, value) => setAmounts((prev) => ({ ...prev, [id]: value }));

  const runBalanceOperation = async (kind, accountId) => {
    const raw = String(amounts[accountId] ?? "").trim();
    if (!AMOUNT_RE.test(raw) || Number(raw.replace(",", ".")) <= 0) {
      alert("Введіть додатну суму (максимум 2 знаки після коми)");
      return;
    }
    const amount = Number(raw.replace(",", "."));

    const opId = `${kind}:${accountId}:${amount}`;
    if (!pendingKeys.current[opId]) pendingKeys.current[opId] = newIdempotencyKey();

    setBusy(true);
    try {
      await userApi.post(
        `/${kind}`,
        { accountId, amount },
        { headers: { "Idempotency-Key": pendingKeys.current[opId] } }
      );
      delete pendingKeys.current[opId]; // операція завершена: наступна матиме новий ключ
      alert(kind === "deposit" ? "Поповнення успішне" : "Зняття успішне");
      setAmounts((prev) => ({ ...prev, [accountId]: "" }));
      await getUserAccounts();
      await getTransactions(accountId);
    } catch (err) {
      if (err.response) {
        // Сервер дав остаточну відповідь (помилка валідації/бізнес-логіки): ключ більше не потрібен
        delete pendingKeys.current[opId];
        alert(errorMessage(err, kind === "deposit" ? "Помилка депозиту" : "Помилка зняття"));
      } else {
        // Відповіді немає: невідомо, чи виконалась операція. Ключ лишається; повтор безпечний.
        alert("Немає зв'язку з сервером. Натисніть ще раз: повторний запит не задублює операцію.");
      }
    } finally {
      setBusy(false);
    }
  };

  const logout = () => {
    localStorage.removeItem("token");
    pendingKeys.current = {};
    navigate("/");
  };

  if (role === "ADMIN") {
    return <AdminPanel users={users} logout={logout} />;
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
            accounts.map((acc) => (
              <div
                key={acc.id}
                onClick={() => setSelectedAccountId(acc.id)}
                style={{
                  padding: "10px",
                  marginBottom: "10px",
                  border: selectedAccountId === acc.id ? "2px solid green" : "1px solid gray",
                  cursor: "pointer",
                }}
              >
                <BankCard account={acc} />

                <input
                  placeholder="Amount"
                  inputMode="decimal"
                  value={amounts[acc.id] || ""}
                  onClick={(e) => e.stopPropagation()}
                  onChange={(e) => setAmountForAccount(acc.id, e.target.value)}
                />

                <div style={{ display: "flex", gap: "10px", marginTop: "5px" }}>
                  <button
                    disabled={busy}
                    onClick={(e) => {
                      e.stopPropagation();
                      runBalanceOperation("deposit", acc.id);
                    }}
                  >
                    ➕ Deposit
                  </button>

                  <button
                    disabled={busy}
                    onClick={(e) => {
                      e.stopPropagation();
                      runBalanceOperation("withdraw", acc.id);
                    }}
                  >
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
              <button onClick={createAccount}>➕ Create Account</button>
            </div>
          )}
        </div>

        {/* RIGHT SIDE - TRANSACTIONS */}
        <div className="right" style={{ width: "60%" }}>
          <h3>Transactions</h3>

          {transactions.length > 0 ? (
            transactions.map((tx) => (
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
