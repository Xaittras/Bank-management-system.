import React, { useState } from "react";
import { authApi, errorMessage } from "../api/api";
import { useNavigate } from "react-router-dom";
import AuthShell from "./AuthShell";
import { useToast } from "./Toast";

function Register() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [toast, notify] = useToast();
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!name || !email || !password) {
      notify("Заповніть усі поля", "error");
      return;
    }
    if (password.length < 8) {
      notify("Пароль має містити щонайменше 8 символів", "error");
      return;
    }
    try {
      await authApi.post("/register", { password, name, email });
      notify("Реєстрація успішна!");
      setTimeout(() => navigate("/"), 800);
    } catch (err) {
      notify(errorMessage(err, "Помилка реєстрації"), "error");
    }
  };

  return (
    <AuthShell
      title="Реєстрація"
      subtitle="Створіть акаунт за хвилину"
      footer={<>Вже є акаунт? <button type="button" className="link" onClick={() => navigate("/")}>Увійти</button></>}
    >
      <form onSubmit={handleRegister}>
        <label>Name
          <input autoComplete="name" placeholder="Eddy Cusuma" value={name} onChange={(e) => setName(e.target.value)} />
        </label>
        <label>Email
          <input type="email" autoComplete="username" placeholder="you@example.com" value={email} onChange={(e) => setEmail(e.target.value)} />
        </label>
        <label>Password
          <input type="password" autoComplete="new-password" placeholder="Мінімум 8 символів" value={password} onChange={(e) => setPassword(e.target.value)} />
        </label>
        <button className="btn primary" type="submit">Зареєструватися</button>
      </form>
      {toast}
    </AuthShell>
  );
}

export default Register;
