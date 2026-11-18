import React, { useState, useEffect, useRef, useCallback } from "react";
import { Routes, Route, Navigate, useLocation, useNavigate } from "react-router-dom";
import { adminApi, userApi, newIdempotencyKey, errorMessage } from "../api/api";
import { getAuthData } from "./auth";
import Layout from "./Layout";
import AdminPanel from "./AdminPanel";
import { useToast } from "./Toast";
import Dashboard from "../pages/Dashboard";
import TransactionsPage from "../pages/TransactionsPage";
import AccountsPage from "../pages/AccountsPage";
import CardsPage from "../pages/CardsPage";
import SettingsPage from "../pages/SettingsPage";
import InvestmentsPage from "../pages/InvestmentsPage";
import LoansPage from "../pages/LoansPage";
import ServicesPage from "../pages/ServicesPage";
import PrivilegesPage from "../pages/PrivilegesPage";
import { parseAmount } from "../lib/stats";
import { tierFor } from "../lib/tiers";

// Усі пункти меню з макета
const ROUTES = [
  { path: "", label: "Dashboard", title: "Overview", icon: "home" },
  { path: "transactions", label: "Transactions", title: "Transactions", icon: "swap" },
  { path: "accounts", label: "Accounts", title: "Accounts", icon: "user" },
  { path: "investments", label: "Investments", title: "Investments", icon: "chart" },
  { path: "credit-cards", label: "Credit Cards", title: "Credit Cards", icon: "card" },
  { path: "loans", label: "Loans", title: "Loans", icon: "coins" },
  { path: "services", label: "Services", title: "Services", icon: "tool" },
  { path: "privileges", label: "My Privileges", title: "My Privileges", icon: "star" },
  { path: "settings", label: "Setting", title: "Setting", icon: "gear" },
];
const NAV = ROUTES.map((r) => ({ to: r.path ? `/bank/${r.path}` : "/bank", end: !r.path, label: r.label, icon: r.icon }));

function Bank() {
  const [transactions, setTransactions] = useState([]);
  const [amount, setAmount] = useState("");
  const [newAccountName, setNewAccountName] = useState("");
  const [selectedAccountId, setSelectedAccountId] = useState(null);
  const [users, setUsers] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [auth, setAuth] = useState(null);
  const [busy, setBusy] = useState(false);
  const [toast, notify] = useToast();
  const navigate = useNavigate();
  const { pathname } = useLocation();

  // Ключі ідемпотентності незавершених операцій. Якщо відповідь загубилась (мережа),
  // повторне натискання використає ТОЙ САМИЙ ключ, і гроші не зарахуються двічі.
  const pendingKeys = useRef({});

  const role = auth?.role;

  // Перехід між вкладками: починаємо нову сторінку зверху
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);

  useEffect(() => {
    const data = getAuthData();
    if (!data) {
      navigate("/");
      return;
    }
    setAuth(data);
  }, [navigate]);

  const getUserAccounts = useCallback(async () => {
    try {
      const res = await userApi.get("/accounts");
      setAccounts(res.data);
    } catch (err) {
      notify(errorMessage(err, "Помилка завантаження акаунтів"), "error");
    }
  }, [notify]);

  const getAllUsers = useCallback(async () => {
    try {
      const res = await adminApi.get("/users");
      setUsers(res.data);
    } catch (err) {
      notify(errorMessage(err, "Помилка завантаження користувачів"), "error");
    }
  }, [notify]);

  const getTransactions = useCallback(async (accountId) => {
    try {
      const res = await userApi.get(`/accounts/${accountId}/transactions`);
      setTransactions(res.data);
    } catch (err) {
      notify(errorMessage(err, "Помилка завантаження транзакцій"), "error");
    }
  }, [notify]);

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

  const createAccount = async (e) => {
    e?.preventDefault();
    try {
      const res = await userApi.post("/accounts", { name: newAccountName });
      setAccounts((prev) => [...prev, res.data]);
      setSelectedAccountId(res.data.id);
      setNewAccountName("");
      notify("Рахунок створено");
    } catch (err) {
      notify(errorMessage(err, "Помилка створення акаунта"), "error");
    }
  };

  // Єдина точка грошових операцій для всіх вкладок (швидка операція, інвестиції, кредити, платежі).
  // tag відокремлює ключі ідемпотентності різних сценаріїв.
  const transact = useCallback(async (kind, accountId, value, tag = "op") => {
    const opId = `${tag}:${kind}:${accountId}:${value}`;
    if (!pendingKeys.current[opId]) pendingKeys.current[opId] = newIdempotencyKey();

    setBusy(true);
    try {
      await userApi.post(
        `/${kind}`,
        { accountId, amount: value },
        { headers: { "Idempotency-Key": pendingKeys.current[opId] } }
      );
      delete pendingKeys.current[opId]; // операція завершена: наступна матиме новий ключ
      await getUserAccounts();
      await getTransactions(accountId);
      return true;
    } catch (err) {
      if (err.response) {
        // Сервер дав остаточну відповідь (помилка валідації/бізнес-логіки): ключ більше не потрібен
        delete pendingKeys.current[opId];
        notify(errorMessage(err, "Помилка операції"), "error");
      } else {
        // Відповіді немає: невідомо, чи виконалась операція. Ключ лишається; повтор безпечний.
        notify("Немає зв'язку з сервером. Натисніть ще раз: повторний запит не задублює операцію.", "error");
      }
      return false;
    } finally {
      setBusy(false);
    }
  }, [getUserAccounts, getTransactions, notify]);

  const runBalanceOperation = async (kind) => {
    const value = parseAmount(amount);
    if (!value) {
      notify("Введіть додатну суму (максимум 2 знаки після коми)", "error");
      return;
    }
    const ok = await transact(kind, selectedAccountId, value, "quick");
    if (ok) {
      notify(kind === "deposit" ? "Поповнення успішне" : "Зняття успішне");
      setAmount("");
    }
  };

  const logout = () => {
    localStorage.removeItem("token");
    pendingKeys.current = {};
    navigate("/");
  };

  const selected = accounts.find((a) => a.id === selectedAccountId);
  const holder = auth?.email?.split("@")[0] ?? "";

  // Рівень клієнта залежить від сумарного балансу: його бачать Privileges і Loans
  const tierInfo = tierFor(accounts.reduce((sum, a) => sum + Number(a.balance), 0));

  if (!auth) return null;

  if (role === "ADMIN") {
    return (
      <>
        <AdminPanel users={users} logout={logout} email={auth.email} onRefresh={getAllUsers} />
        {toast}
      </>
    );
  }

  const ctx = {
    auth, holder, logout, accounts, selected, selectedAccountId, setSelectedAccountId, transactions,
    amount, setAmount, busy, runBalanceOperation, newAccountName, setNewAccountName, createAccount,
    transact, notify, tierInfo,
  };

  const current = ROUTES.find((r) => pathname.replace(/\/$/, "") === (r.path ? `/bank/${r.path}` : "/bank")) ?? ROUTES[0];

  return (
    <Layout title={current.title} nav={NAV} email={auth.email} onLogout={logout} onRefresh={getUserAccounts}>
      <Routes>
        <Route index element={<Dashboard ctx={ctx} />} />
        <Route path="transactions" element={<TransactionsPage ctx={ctx} />} />
        <Route path="accounts" element={<AccountsPage ctx={ctx} />} />
        <Route path="credit-cards" element={<CardsPage ctx={ctx} />} />
        <Route path="settings" element={<SettingsPage ctx={ctx} />} />
        <Route path="investments" element={<InvestmentsPage ctx={ctx} />} />
        <Route path="loans" element={<LoansPage ctx={ctx} />} />
        <Route path="services" element={<ServicesPage ctx={ctx} />} />
        <Route path="privileges" element={<PrivilegesPage ctx={ctx} />} />
        <Route path="*" element={<Navigate to="/bank" replace />} />
      </Routes>
      {toast}
    </Layout>
  );
}

export default Bank;
