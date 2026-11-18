import React, { useState } from "react";
import { authApi, errorMessage } from "../api/api";
import { useNavigate } from "react-router-dom";
import AuthShell from "./AuthShell";
import { useToast } from "./Toast";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [toast, notify] = useToast();
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await authApi.post("/login", { email, password });
      localStorage.setItem("token", res.data.token);
      navigate("/bank");
    } catch (err) {
      notify(errorMessage(err, "Помилка входу"), "error");
    }
  };

  return (
    <AuthShell
      title="Вхід"
      subtitle="Увійдіть, щоб побачити свої рахунки"
      footer={<>Немає акаунту? <button type="button" className="link" onClick={() => navigate("/register")}>Реєстрація</button></>}
    >
      <form onSubmit={handleLogin}>
        <label>Email
          <input type="email" autoComplete="username" placeholder="you@example.com" value={email} onChange={(e) => setEmail(e.target.value)} />
        </label>
        <label>Password
          <input type="password" autoComplete="current-password" placeholder="••••••••" value={password} onChange={(e) => setPassword(e.target.value)} />
        </label>
        <button className="btn primary" type="submit">Увійти</button>
      </form>
      {toast}
    </AuthShell>
  );
}

export default Login;
