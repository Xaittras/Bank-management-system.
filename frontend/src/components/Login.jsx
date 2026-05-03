import React, { useState } from "react";
import { authApi } from "../api/api";
import { useNavigate } from "react-router-dom";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleLogin = async () => {
  try {
    const res = await authApi.post("/login", { email, password });

    localStorage.setItem("token", res.data.token);
console.log(res.data);
    navigate("/bank");
  }catch (err) {
  const message =
    err.response?.data || "Помилка входу";

  alert(message);
}
};

  return (
    <div className="box">
      <h3>🔐 Вхід</h3>
      <input placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} />
      <input type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} />
      <button onClick={handleLogin}>Увійти</button>
      <p>
        Немає акаунту? <button onClick={() => navigate("/register")}>Реєстрація</button>
      </p>
    </div>
  );
}

export default Login;